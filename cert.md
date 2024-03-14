# Generating a simple pkcs12 cert

Generate a CA cert
```shell
openssl genpkey -algorithm RSA -out ca.key 

openssl req \
  -new \
  -x509 \
  -nodes \
  -days 3650 \
  -subj '/CN=chrome-network-changed-h2' \
  -key ca.key \
  -out ca.crt
```

Generate a server certificate and chain
```shell
openssl genpkey -algorithm RSA -out server.key 

openssl req \
  -new \
  -key server.key \
  -subj '/CN=localhost' \
  -out server.csr
  
openssl x509 \
  -req \
  -in server.csr \
  -CA ca.crt \
  -CAkey ca.key \
  -CAcreateserial \
  -days 3650 \
  -out server.crt

rm server.csr

cat server.crt ca.crt > server.chain.crt
```

Convert to pkcs12
```shell
openssl pkcs12 -export -in server.chain.crt -inkey server.key -out selfsigned.p12 -passout pass:secret
```