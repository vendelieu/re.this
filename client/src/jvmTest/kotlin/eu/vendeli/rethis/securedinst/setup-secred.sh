#!/usr/bin/env bash
set -e

REDIS_PASSWORD="yourStrongRedisPassword"
CERT_DIR="./certs"

echo "=== Creating certificate directory ==="
mkdir -p "$CERT_DIR"

echo "=== Generating private CA ==="
openssl genrsa -out $CERT_DIR/ca.key 4096
openssl req -x509 -new -nodes -key $CERT_DIR/ca.key -sha256 -days 3650 \
  -out $CERT_DIR/ca.crt -subj "/CN=redis-ca"

echo "=== Generating server key ==="
openssl genrsa -out $CERT_DIR/redis.key 4096

echo "=== Generating CSR ==="
openssl req -new -key $CERT_DIR/redis.key -out $CERT_DIR/redis.csr -subj "/CN=redis"

echo "=== Signing server certificate ==="
openssl x509 -req -in $CERT_DIR/redis.csr -CA $CERT_DIR/ca.crt -CAkey $CERT_DIR/ca.key -CAcreateserial \
  -out $CERT_DIR/redis.crt -days 365 -sha256

echo "=== Writing redis.conf (TLS + password) ==="
cat > redis.conf <<EOF
requirepass $REDIS_PASSWORD

port 0
tls-port 6379

tls-cert-file /certs/redis.crt
tls-key-file /certs/redis.key
tls-ca-cert-file /certs/ca.crt

tls-auth-clients no
EOF

echo ""
echo "--------------------------------------------------"
echo " TLS certificates and redis.conf generated."
echo ""
echo " Files created:"
echo "  - ./certs/ca.crt"
echo "  - ./certs/redis.crt"
echo "  - ./certs/redis.key"
echo "  - ./redis.conf"
echo ""
echo " You can now run Redis manually, e.g.:"
echo ""
echo " docker run -p 6379:6379 \\"
echo "   -v \$(pwd)/redis.conf:/etc/redis/redis.conf:ro \\"
echo "   -v \$(pwd)/certs:/certs:ro \\"
echo "   redis:7 redis-server /etc/redis/redis.conf"
echo ""
echo " Connect with:"
echo ""
echo " redis-cli --tls --cacert ./certs/ca.crt -a $REDIS_PASSWORD"
echo "--------------------------------------------------"
