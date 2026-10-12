#!/usr/bin/env bash
# Runs only newly created local containers. Never reads .env or an external DB URL.
set -euo pipefail

if [[ "${1:-}" == --help ]]; then
    printf '%s\n' 'Usage: bash scripts/smoke-demo.sh --confirm-isolated' \
        'Requires the locally built device-demo:prd602 image, Docker, OpenSSL and Node.'
    exit 0
fi
if [[ "$#" != 1 || "$1" != --confirm-isolated ]]; then
    printf '%s\n' 'Refusing to run without --confirm-isolated.' >&2
    exit 1
fi
for tool in docker openssl node; do
    command -v "$tool" >/dev/null
done
docker image inspect device-demo:prd602 >/dev/null

smoke_dir=$(mktemp -d /tmp/device-demo-smoke.XXXXXX)
smoke_id=$(openssl rand -hex 6)
network="device-demo-smoke-$smoke_id"
mysql_container="$network-mysql"
backend_container="$network-backend"
cleanup() {
    docker rm -fv "$backend_container" "$mysql_container" >/dev/null 2>&1 || true
    docker network rm "$network" >/dev/null 2>&1 || true
    # Only the exact mktemp directory owned by this invocation is eligible.
    if [[ "$smoke_dir" == /tmp/device-demo-smoke.* && -d "$smoke_dir" ]]; then
        rm -rf -- "$smoke_dir"
    fi
}
trap cleanup EXIT
umask 077
mkdir "$smoke_dir/tls"
openssl req -x509 -newkey rsa:2048 -nodes -days 1 -subj '/CN=Device smoke CA' \
    -keyout "$smoke_dir/ca-key.pem" -out "$smoke_dir/ca.pem" >/dev/null 2>&1
openssl req -newkey rsa:2048 -nodes -subj '/CN=demo-mysql' \
    -addext 'subjectAltName=DNS:demo-mysql' \
    -keyout "$smoke_dir/tls/server-key.pem" -out "$smoke_dir/server.csr" >/dev/null 2>&1
openssl x509 -req -in "$smoke_dir/server.csr" -CA "$smoke_dir/ca.pem" \
    -CAkey "$smoke_dir/ca-key.pem" -CAcreateserial -days 1 -copy_extensions copy \
    -out "$smoke_dir/tls/server-cert.pem" >/dev/null 2>&1
cp "$smoke_dir/ca.pem" "$smoke_dir/tls/ca.pem"
# MySQL's unprivileged user reads only these disposable generated TLS files.
chmod 755 "$smoke_dir/tls"
chmod 644 "$smoke_dir/tls/"*.pem

db_password=$(openssl rand -hex 24)
admin_password=$(openssl rand -hex 24)
printf 'MYSQL_DATABASE=device_demo_smoke\nMYSQL_USER=device_demo\nMYSQL_PASSWORD=%s\nMYSQL_ROOT_PASSWORD=%s\n' \
    "$db_password" "$(openssl rand -hex 24)" > "$smoke_dir/mysql.env"
printf '%s\n' \
    'DB_URL=jdbc:mysql://demo-mysql:3306/device_demo_smoke' \
    'DB_USERNAME=device_demo' \
    "DB_PASSWORD=$db_password" \
    "DB_CA_CERT_BASE64=$(base64 -w 0 "$smoke_dir/ca.pem")" \
    'ADMIN_EMAIL=admin@example.test' \
    "ADMIN_PASSWORD=$admin_password" \
    "JWT_SIGNER_KEY=$(openssl rand -hex 64)" \
    "EMAIL_JOB_SECRET_KEY_BASE64=$(openssl rand -base64 32)" \
    'FRONTEND_ORIGIN=https://device-demo.onrender.com' \
    'PASSWORD_RESET_BASE_URL=https://device-demo.onrender.com/reset-password' \
    'RESEND_API_KEY=re_smoke_not_sent' \
    'RESEND_FROM=Device Management <onboarding@resend.dev>' \
    'RESEND_ALLOWED_RECIPIENTS=admin@example.test' > "$smoke_dir/backend.env"

docker network create "$network" >/dev/null
docker run -d --name "$mysql_container" --network "$network" --network-alias demo-mysql \
    --env-file "$smoke_dir/mysql.env" --mount "type=bind,src=$smoke_dir/tls,dst=/tls,readonly" \
    mysql:8.4 --require-secure-transport=ON --ssl-ca=/tls/ca.pem \
    --ssl-cert=/tls/server-cert.pem --ssl-key=/tls/server-key.pem >/dev/null
mysql_ready=false
for ((attempt=0; attempt<90; attempt++)); do
    if docker exec "$mysql_container" sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -N -e "SELECT 1"' \
            >/dev/null 2>&1; then
        mysql_ready=true
        break
    fi
    sleep 2
done
[[ "$mysql_ready" == true ]] || { printf '%s\n' 'Isolated MySQL did not start.' >&2; exit 1; }

started=$SECONDS
docker run -d --name "$backend_container" --network "$network" \
    --memory=512m --memory-swap=512m --cpus=0.1 --env-file "$smoke_dir/backend.env" \
    device-demo:prd602 >/dev/null
ready=false
for ((attempt=0; attempt<180; attempt++)); do
    if [[ $(docker inspect --format '{{.State.Running}}' "$backend_container") != true ]]; then
        printf '%s\n' 'Demo container exited before readiness. Relevant startup diagnostics:' >&2
        docker logs --tail 240 "$backend_container" 2>&1 \
            | grep -E 'APPLICATION FAILED|Description:|Action:|ERROR|Exception|Caused by:|Flyway|HikariPool|Tomcat|Started' \
            | sed -E 's#jdbc:mysql://[^ ]+#jdbc:mysql://[REDACTED]#g' >&2 || true
        exit 1
    fi
    if docker exec "$backend_container" curl --max-time 4 -fsS \
            http://127.0.0.1:8080/actuator/health/readiness > "$smoke_dir/health.json" 2>/dev/null; then
        ready=true
        break
    fi
    sleep 5
done
[[ "$ready" == true ]] || { printf '%s\n' 'Demo readiness timed out.' >&2; exit 1; }
printf 'Demo ready after %s seconds with 512 MiB and 0.1 CPU.\n' "$((SECONDS-started))"
node -e 'const h=JSON.parse(require("fs").readFileSync(process.argv[1])); if(h.status!=="UP" || h.components) process.exit(1)' \
    "$smoke_dir/health.json"

request() { docker exec "$backend_container" curl --max-time 30 -sS "$@"; }
[[ $(request -o /dev/null -w '%{http_code}' http://127.0.0.1:8080/api/devices) == 401 ]]
[[ $(request -o /dev/null -w '%{http_code}' http://127.0.0.1:8080/actuator/prometheus) == 401 ]]
[[ $(request -o /dev/null -w '%{http_code}' -X OPTIONS \
    -H 'Origin: https://attacker.example' -H 'Access-Control-Request-Method: GET' \
    http://127.0.0.1:8080/api/devices) == 403 ]]
request -D - -o /dev/null -X OPTIONS -H 'Origin: https://device-demo.onrender.com' \
    -H 'Access-Control-Request-Method: GET' -H 'Access-Control-Request-Headers: authorization,x-request-id' \
    http://127.0.0.1:8080/api/devices > "$smoke_dir/cors.headers"
grep -Fiq 'Access-Control-Allow-Origin: https://device-demo.onrender.com' "$smoke_dir/cors.headers"

SMOKE_ADMIN_PASSWORD="$admin_password" node -e \
    'process.stdout.write(JSON.stringify({email:"admin@example.test",password:process.env.SMOKE_ADMIN_PASSWORD}))' \
    > "$smoke_dir/login.json"
docker exec -i "$backend_container" curl --max-time 30 -fsS -H 'Content-Type: application/json' \
    --data-binary @- http://127.0.0.1:8080/api/auth/token \
    < "$smoke_dir/login.json" > "$smoke_dir/login-response.json"
token=$(node -e 'const r=JSON.parse(require("fs").readFileSync(process.argv[1])); if(!r.result?.token) process.exit(1); process.stdout.write(r.result.token)' \
    "$smoke_dir/login-response.json")
request -f -H "Authorization: Bearer $token" -H 'Origin: https://device-demo.onrender.com' \
    http://127.0.0.1:8080/api/devices > "$smoke_dir/devices.json"
node -e 'const r=JSON.parse(require("fs").readFileSync(process.argv[1])); if(r.code!==1000 || !Array.isArray(r.result.content)) process.exit(1)' \
    "$smoke_dir/devices.json"
migration_count=$(docker exec "$mysql_container" sh -c \
    'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -N device_demo_smoke -e "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1"')
[[ "$migration_count" == 8 ]]
[[ $(docker inspect --format '{{.State.OOMKilled}}' "$backend_container") == false ]]
docker stats --no-stream --format 'Demo memory: {{.MemUsage}}' "$backend_container"
printf '%s\n' 'PASS: MySQL VERIFY_IDENTITY TLS, Resend demo configuration, Flyway V1-V8, readiness, login, paginated devices, CORS and management boundary.'
