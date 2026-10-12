#!/bin/sh
set -eu
umask 077

if [ "${SPRING_PROFILES_ACTIVE:-}" != demo ]; then
    printf '%s\n' 'The demo image requires only the demo profile.' >&2
    exit 1
fi
: "${DB_CA_CERT_BASE64:?Provide the demo MySQL CA certificate as Base64}"
tls_dir=$(mktemp -d /tmp/device-demo-tls.XXXXXX)
printf '%s' "$DB_CA_CERT_BASE64" | base64 -d > "$tls_dir/ca.pem"
DB_TLS_TRUSTSTORE_PASSWORD=$(head -c 32 /dev/urandom | base64)
export DB_TLS_TRUSTSTORE_PASSWORD
keytool -importcert -noprompt -alias demo-mysql-ca -storetype JKS \
    -file "$tls_dir/ca.pem" -keystore "$tls_dir/truststore.jks" \
    -storepass:env DB_TLS_TRUSTSTORE_PASSWORD
export DB_TLS_TRUSTSTORE_URL="file:$tls_dir/truststore.jks"
unset DB_CA_CERT_BASE64
exec java -jar /app/device.jar
