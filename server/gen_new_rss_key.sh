#!/bin/sh
#https://ktor.io/docs/rsa-keys-generation.html
# Generate RSA private key
# openssl genpkey -algorithm rsa -pkeyopt rsa_keygen_bits:2048 -out ktor.pk8
openssl genpkey -algorithm rsa -pkeyopt rsa_keygen_bits:2048 -out ktor.key
openssl pkcs8 -topk8 -inform PEM -outform DER -nocrypt -in ktor.key -out ktor.pk8
openssl rsa -pubout -in ktor.key -out ktor.pub