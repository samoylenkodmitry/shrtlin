#!/bin/bash
# --- Configuration ---
RELEASE_TAG="v_0.0.0.2_test" # Update with your desired release tag https://github.com/samoylenkodmitry/shrtlin/releases
CHECKOUT_TAG="no_build_0.0.0.4_test" # Update with your desired release tag
FRONTEND_ARTIFACT_NAME="frontend-web.zip"
BACKEND_ARTIFACT_NAME="server-1.0.0.jar"

# --- Functions for clarity and reusability ---
download_artifact() {
  # check if the file already exists
  if [ -f "$1" ]; then
    # ask the user if they want to overwrite the file
    read -p "$1 already exists. Do you want to overwrite it? (y/n): " overwrite
    if [ "$overwrite" != "y" ]; then
      echo "Skipping download for $1"
      return
    fi
  fi
  local artifact_url="https://github.com/samoylenkodmitry/shrtlin/releases/download/$RELEASE_TAG/$1"
  wget -O "$1" "$artifact_url" || { echo "Download failed for $1"; exit 1; }
}

# --- Git Checkout on the tag, override conflicts ---
git fetch --all
git checkout "$CHECKOUT_TAG" || { echo "Failed to checkout $CHECKOUT_TAG"; exit 1; }
git pull origin "$CHECKOUT_TAG" || { echo "Failed to pull $CHECKOUT_TAG"; exit 1; }

# --- Download Artifacts ---
download_artifact "$FRONTEND_ARTIFACT_NAME"
download_artifact "$BACKEND_ARTIFACT_NAME"

# --- Prepare Artifacts ---
## make dirs if not exist
mkdir -p composeApp/build/distributions/
mkdir -p server/build/libs/

unzip -o "$FRONTEND_ARTIFACT_NAME" -d composeApp/build/distributions/
mv "$BACKEND_ARTIFACT_NAME" server/build/libs/server-all.jar

# --- Generate RSA Keys if not present ---

if [ ! -f "./server/ktor.pk8" ]; then
  echo "RSA keys not found, generating..."
  chmod +x ./server/gen_new_rss_key.sh
  chmod +x ./server/gen_jwks_from_key.sh
  ./server/gen_new_rss_key.sh
  ./server/gen_jwks_from_key.sh 
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

source ./.env

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