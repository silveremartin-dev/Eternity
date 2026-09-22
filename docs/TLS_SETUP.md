# TLS/SSL Setup Guide

**Authors:** Silvere Martin-Michiellot, Antigravity

## Overview

This guide explains how to enable TLS/SSL for secure communication in the Eternity II server.

## Generate Self-Signed Certificate (Development)

```bash
# Generate keystore with self-signed certificate
keytool -genkeypair \
  -alias eternity \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -keystore keystore.jks \
  -storepass changeit \
  -dname "CN=localhost, OU=Dev, O=Eternity, L=City, ST=State, C=US"

# Export certificate
keytool -exportcert \
  -alias eternity \
  -keystore keystore.jks \
  -storepass changeit \
  -file eternity.crt

# Create truststore
keytool -importcert \
  -alias eternity \
  -file eternity.crt \
  -keystore truststore.jks \
  -storepass changeit \
  -noprompt
```

## Environment Variables

```bash
# Server TLS configuration
TLS_ENABLED=true
TLS_KEYSTORE_PATH=/path/to/keystore.jks
TLS_KEYSTORE_PASSWORD=changeit
TLS_KEY_PASSWORD=changeit

# gRPC TLS
GRPC_TLS_ENABLED=true
GRPC_CERT_CHAIN=/path/to/cert.pem
GRPC_PRIVATE_KEY=/path/to/key.pem
```

## gRPC Server TLS Configuration

```java
// In EternityServer.java
SslContext sslContext = GrpcSslContexts.forServer(
    new File(System.getenv("GRPC_CERT_CHAIN")),
    new File(System.getenv("GRPC_PRIVATE_KEY"))
).build();

Server server = NettyServerBuilder.forPort(9090)
    .sslContext(sslContext)
    .addService(new EternityServiceImpl())
    .build();
```

## gRPC Client TLS Configuration

```java
// In EternityClient.java
SslContext sslContext = GrpcSslContexts.forClient()
    .trustManager(new File("/path/to/ca.crt"))
    .build();

ManagedChannel channel = NettyChannelBuilder.forAddress(host, port)
    .sslContext(sslContext)
    .build();
```

## Let's Encrypt (Production)

```bash
# Install certbot
apt-get install certbot

# Generate certificate
certbot certonly --standalone -d eternity.example.com

# Convert to PKCS12
openssl pkcs12 -export \
  -in /etc/letsencrypt/live/eternity.example.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/eternity.example.com/privkey.pem \
  -out keystore.p12 \
  -name eternity \
  -passout pass:changeit

# Convert to JKS (if needed)
keytool -importkeystore \
  -srckeystore keystore.p12 \
  -srcstoretype PKCS12 \
  -srcstorepass changeit \
  -destkeystore keystore.jks \
  -deststoretype JKS \
  -deststorepass changeit
```

## Docker with TLS

```dockerfile
# Dockerfile
COPY keystore.jks /app/keystore.jks
ENV TLS_ENABLED=true
ENV TLS_KEYSTORE_PATH=/app/keystore.jks
```

```yaml
# docker-compose.yml
services:
  eternity:
    environment:
      - TLS_ENABLED=true
      - TLS_KEYSTORE_PATH=/app/keystore.jks
      - TLS_KEYSTORE_PASSWORD=${TLS_PASSWORD}
    volumes:
      - ./certs/keystore.jks:/app/keystore.jks:ro
```

## Kubernetes with TLS

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: eternity-tls
type: kubernetes.io/tls
data:
  tls.crt: <base64-encoded-cert>
  tls.key: <base64-encoded-key>
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: eternity-ingress
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - eternity.example.com
    secretName: eternity-tls
  rules:
  - host: eternity.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: eternity-server
            port:
              number: 9090
```

## Verify TLS

```bash
# Test gRPC with TLS
grpcurl -cert client.crt -key client.key \
  eternity.example.com:9090 list

# Check certificate
openssl s_client -connect eternity.example.com:9090 -showcerts
```
