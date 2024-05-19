#!/bin/sh
#https://ktor.io/docs/rsa-keys-generation.html
# Generate RSA private key (moved to gen_new_rss_key.sh)

echo "Public key: $(cat ktor.pub)"

# Path to the RSA public key
RSA_PUB_KEY_PATH="./ktor.pub"

# Check if the public key file exists
if [ ! -f "$RSA_PUB_KEY_PATH" ]; then
    echo "Public key file not found: $RSA_PUB_KEY_PATH"
    exit 1
fi

# Extract and format the modulus
MODULUS_HEX=$(openssl rsa -pubin -in "$RSA_PUB_KEY_PATH" -modulus -noout | cut -d'=' -f2)
MODULUS_BASE64=$(echo "$MODULUS_HEX" | xxd -r -p | base64 | tr -d '\n' | tr '+/' '-_' | tr -d '=')

# Extract and format the exponent
EXTRACTED_EXPONENT_HEX=$(openssl rsa -pubin -in "$RSA_PUB_KEY_PATH" -text -noout | grep "Exponent" | awk -F'[()]' '{print $2}' | sed 's/^0x//')
# Ensure the exponent hex string has even length
if [ $((${#EXTRACTED_EXPONENT_HEX} % 2)) -ne 0 ]; then
    EXTRACTED_EXPONENT_HEX="0${EXTRACTED_EXPONENT_HEX}"
fi
EXPONENT_BASE64=$(echo "$EXTRACTED_EXPONENT_HEX" | xxd -r -p | base64 | tr -d '\n' | tr '+/' '-_' | tr -d '=')

# Create the JWKS JSON
JWKS_JSON=$(cat <<EOF
{
  "keys": [
    {
      "alg": "RS512",
      "kty": "RSA",
      "e": "${EXPONENT_BASE64}",
      "kid": "shrtl.in_kid_1",
      "n": "${MODULUS_BASE64}"
    }
  ]
}
EOF
)

# Write the JWKS JSON to file
OUTPUT_DIR="./certs"
OUTPUT_FILE="$OUTPUT_DIR/jwks.json"
if [ ! -d "$OUTPUT_DIR" ]; then
    mkdir -p "$OUTPUT_DIR"
fi
echo "$JWKS_JSON" > "$OUTPUT_FILE"

# Output the generated jwks.json for verification
echo "Generated jwks.json:"
cat "$OUTPUT_FILE"