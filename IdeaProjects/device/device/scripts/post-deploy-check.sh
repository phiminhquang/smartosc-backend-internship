#!/usr/bin/env bash

set -euo pipefail

show_usage() {
    printf '%s\n' \
        'Usage: bash scripts/post-deploy-check.sh --backend-url https://backend.example --frontend-url https://frontend.example [--max-wait-seconds 900]' \
        '' \
        'For a local-only test, add --allow-http-local; non-loopback HTTP is always rejected.'
}

backend_url=''
frontend_url=''
max_wait_seconds=900
allow_http_local=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --backend-url)
            backend_url="${2:-}"
            shift 2
            ;;
        --frontend-url)
            frontend_url="${2:-}"
            shift 2
            ;;
        --max-wait-seconds)
            max_wait_seconds="${2:-}"
            shift 2
            ;;
        --allow-http-local)
            allow_http_local=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            printf 'Unknown argument: %s\n' "$1" >&2
            show_usage >&2
            exit 2
            ;;
    esac
done

backend_url="${backend_url%/}"
frontend_url="${frontend_url%/}"
[[ "${max_wait_seconds}" =~ ^[1-9][0-9]*$ ]] || { printf '%s\n' 'Invalid max wait.' >&2; exit 2; }

validate_origin() {
    local name="$1"
    local value="$2"
    local local_flag="$3"

    python3 -c '
import sys
from urllib.parse import urlsplit
name, value, allow_local = sys.argv[1:]
parsed = urlsplit(value)
loopback = parsed.hostname in {"localhost", "127.0.0.1", "::1"}
valid_scheme = parsed.scheme == "https" or (allow_local == "true" and parsed.scheme == "http" and loopback)
if not valid_scheme or not parsed.hostname or parsed.username or parsed.password or parsed.query or parsed.fragment or parsed.path not in {"", "/"}:
    raise SystemExit(f"{name} must be an exact HTTPS origin (or HTTP loopback with --allow-http-local).")
' "${name}" "${value}" "${local_flag}"
}

validate_origin 'Backend URL' "${backend_url}" "${allow_http_local}"
validate_origin 'Frontend URL' "${frontend_url}" "${allow_http_local}"
[[ "${backend_url}" != "${frontend_url}" ]] || { printf '%s\n' 'Backend and frontend origins must differ.' >&2; exit 2; }

temp_dir="$(mktemp -d "${TMPDIR:-/tmp}/device-post-deploy.XXXXXX")"
trap 'rm -rf -- "${temp_dir}"' EXIT
chmod 700 "${temp_dir}"

curl_common=(--silent --show-error --connect-timeout 10 --max-time 30)
if [[ "${allow_http_local}" != true ]]; then
    curl_common+=(--proto '=https' --tlsv1.2)
fi

read_header() {
    local headers_file="$1"
    local header_name="$2"
    awk -F ':' -v wanted="${header_name}" '
        BEGIN { wanted = tolower(wanted) }
        tolower($1) == wanted {
            sub(/^[^:]*:[[:space:]]*/, "")
            sub(/\r$/, "")
            value = $0
        }
        END { print value }
    ' "${headers_file}"
}

health_body="${temp_dir}/health.json"
deadline=$((SECONDS + max_wait_seconds))
health_ready=false
while (( SECONDS < deadline )); do
    health_code="$(curl "${curl_common[@]}" --output "${health_body}" --write-out '%{http_code}' \
        "${backend_url}/actuator/health/readiness" || true)"
    if [[ "${health_code}" == "200" ]] && python3 -c '
import json, sys
with open(sys.argv[1], encoding="utf-8") as handle:
    payload = json.load(handle)
assert payload.get("status") == "UP"
assert "components" not in payload and "details" not in payload
' "${health_body}" >/dev/null 2>&1; then
        health_ready=true
        break
    fi
    sleep 10
done
[[ "${health_ready}" == true ]] || { printf '%s\n' 'Backend readiness did not become UP in time.' >&2; exit 1; }

frontend_headers="${temp_dir}/frontend.headers"
frontend_body="${temp_dir}/frontend.html"
frontend_code="$(curl "${curl_common[@]}" --location --dump-header "${frontend_headers}" \
    --output "${frontend_body}" --write-out '%{http_code}' "${frontend_url}/")"
[[ "${frontend_code}" == "200" ]] || { printf 'Frontend root returned HTTP %s.\n' "${frontend_code}" >&2; exit 1; }
grep -Fq '<div id="root"></div>' "${frontend_body}" || { printf '%s\n' 'Frontend root is not the expected Vite application.' >&2; exit 1; }

[[ "$(read_header "${frontend_headers}" 'X-Content-Type-Options')" == 'nosniff' ]] || { printf '%s\n' 'Frontend nosniff header is missing.' >&2; exit 1; }
[[ "$(read_header "${frontend_headers}" 'X-Frame-Options')" == 'DENY' ]] || { printf '%s\n' 'Frontend frame-deny header is missing.' >&2; exit 1; }
[[ "$(read_header "${frontend_headers}" 'Referrer-Policy')" == 'no-referrer' ]] || { printf '%s\n' 'Frontend no-referrer header is missing.' >&2; exit 1; }

deep_link_body="${temp_dir}/deep-link.html"
deep_link_code="$(curl "${curl_common[@]}" --location --output "${deep_link_body}" \
    --write-out '%{http_code}' "${frontend_url}/forgot-password")"
[[ "${deep_link_code}" == "200" ]] && grep -Fq '<div id="root"></div>' "${deep_link_body}" || {
    printf '%s\n' 'Frontend SPA deep-link rewrite failed.' >&2
    exit 1
}

asset_path="$(sed -nE 's#.*src="([^"]*/assets/[^"]+\.js)".*#\1#p' "${frontend_body}" | head -n 1)"
[[ "${asset_path}" == /* ]] || { printf '%s\n' 'Could not locate the frontend JavaScript asset.' >&2; exit 1; }
asset_body="${temp_dir}/frontend.js"
curl "${curl_common[@]}" --fail --location --output "${asset_body}" "${frontend_url}${asset_path}"
if grep -Fq 'placeholder.invalid' "${asset_body}"; then
    printf '%s\n' 'Frontend bundle still contains the placeholder API URL.' >&2
    exit 1
fi
grep -Fq "${backend_url}/api" "${asset_body}" || {
    printf '%s\n' 'Frontend bundle does not contain the expected backend API base URL.' >&2
    exit 1
}

unauthorized_code="$(curl "${curl_common[@]}" --output /dev/null --write-out '%{http_code}' \
    "${backend_url}/api/devices")"
[[ "${unauthorized_code}" == "401" ]] || { printf 'Protected API returned HTTP %s instead of 401.\n' "${unauthorized_code}" >&2; exit 1; }

prometheus_code="$(curl "${curl_common[@]}" --output /dev/null --write-out '%{http_code}' \
    "${backend_url}/actuator/prometheus")"
[[ "${prometheus_code}" == "401" ]] || { printf 'Public Prometheus endpoint returned HTTP %s instead of 401.\n' "${prometheus_code}" >&2; exit 1; }

cors_headers="${temp_dir}/cors.headers"
cors_code="$(curl "${curl_common[@]}" --request OPTIONS --dump-header "${cors_headers}" --output /dev/null \
    --write-out '%{http_code}' -H "Origin: ${frontend_url}" \
    -H 'Access-Control-Request-Method: POST' -H 'Access-Control-Request-Headers: Content-Type' \
    "${backend_url}/api/auth/token")"
[[ "${cors_code}" == "200" || "${cors_code}" == "204" ]] || { printf 'Allowed CORS preflight returned HTTP %s.\n' "${cors_code}" >&2; exit 1; }
[[ "$(read_header "${cors_headers}" 'Access-Control-Allow-Origin')" == "${frontend_url}" ]] || {
    printf '%s\n' 'Allowed CORS preflight did not return the exact frontend origin.' >&2
    exit 1
}
[[ -z "$(read_header "${cors_headers}" 'Access-Control-Allow-Credentials')" ]] || {
    printf '%s\n' 'CORS unexpectedly allows credentials.' >&2
    exit 1
}

rejected_origin='https://invalid.example'
rejected_code="$(curl "${curl_common[@]}" --request OPTIONS --output /dev/null --write-out '%{http_code}' \
    -H "Origin: ${rejected_origin}" -H 'Access-Control-Request-Method: POST' \
    "${backend_url}/api/auth/token")"
[[ "${rejected_code}" == "403" ]] || { printf 'Foreign CORS origin returned HTTP %s instead of 403.\n' "${rejected_code}" >&2; exit 1; }

printf '%s\n' 'Post-deploy checks passed: readiness, frontend bundle/routing/headers, API auth, management boundary and exact-origin CORS.'
