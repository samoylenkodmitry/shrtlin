#!/bin/bash
# --- Configuration ---
RELEASE_TAG="v_0.0.0.1_test" # Update with your desired release tag https://github.com/samoylenkodmitry/shrtlin/releases
FRONTEND_ARTIFACT_NAME="frontend-web.zip"
BACKEND_ARTIFACT_NAME="server-1.0.0.jar"

# --- Functions for clarity and reusability ---
download_artifact() {
  local artifact_url="https://github.com/samoylenkodmitry/shrtlin/releases/download/$RELEASE_TAG/$1"
  wget -O "$1" "$artifact_url" || { echo "Download failed for $1"; exit 1; }
}

# --- Download Artifacts ---
download_artifact "$FRONTEND_ARTIFACT_NAME"
download_artifact "$BACKEND_ARTIFACT_NAME"

# --- Prepare Artifacts ---
unzip -o "$FRONTEND_ARTIFACT_NAME" -d composeApp/build/distributions/
mv "$BACKEND_ARTIFACT_NAME" server/build/libs/server-all.jar

# --- Generate RSA Keys if not present ---

if [ ! -f "./server/ktor.pk8" ]; then
  echo "RSA keys not found, generating..."
  ./server/gen_new_rss_key.sh
  ./server/gen_jwks_from_key.sh 
  # Securely store ktor.pk8 - adjust permissions
  chmod 400 server/ktor.pk8 
  chmod 400 server/ktor.key
else 
  echo "RSA keys found, skipping generation..."
fi

# --- Configure Environment (.env) ---

if [ ! -f "./.env" ]; then
  echo "Copying template.env to .env"
  cp ./template.env ./.env

  read -p "Enter your database username (default: user): " DB_USER
  DB_USER=${DB_USER:-user} # Use 'user' as default if empty
  sed -i "s/DATABASE_USER=.*/DATABASE_USER=$DB_USER/" ./.env

  read -s -p "Enter your database password (default: password): " DB_PASSWORD
  DB_PASSWORD=${DB_PASSWORD:-password} # Use 'password' as default if empty
  sed -i "s/DATABASE_PASSWORD=.*/DATABASE_PASSWORD=$DB_PASSWORD/" ./.env

  echo "Database credentials updated in .env"
fi
export $(grep -v '^#' .env | xargs) 

# --- Create Docker Secrets (only on first run) --- 

if [ ! "$(docker secret ls -q | grep db_username)" ]; then 
  echo "Creating Docker secrets..."
  docker secret create db_username "$DATABASE_USER"
  docker secret create db_password "$DATABASE_PASSWORD"
else 
  echo "Docker secrets already exist, skipping creation."
fi

# --- Start Docker Compose ---
docker-compose up -d

echo "Deployment complete!"