#!/bin/bash
# --- Configuration ---
RELEASE_TAG="v_0.0.0.2_test" # Update with desired release tag 
                               # (https://github.com/samoylenkodmitry/shrtlin/releases)
FRONTEND_ARTIFACT_NAME="frontend-web.zip"
BACKEND_ARTIFACT_NAME="server-1.0.0.jar"
# Optional: Uncomment and set CHECKOUT_TAG to checkout a specific tag. 
# Otherwise, the latest commit on 'main' will be used.
# CHECKOUT_TAG="your_tag_here" 

# --- Functions for clarity and reusability ---
download_artifact() {
  if [ -f "$1" ]; then
    read -p "$1 already exists. Overwrite? (y/n): " overwrite
    [ "$overwrite" != "y" ] && echo "Skipping download for $1" && return 0
  fi
  local artifact_url="https://github.com/samoylenkodmitry/shrtlin/releases/download/$RELEASE_TAG/$1"
  wget -O "$1" "$artifact_url" || { echo "Download failed for $1"; exit 1; }
}
# --- Git Reset and Checkout ---
echo "Resetting local changes (hard reset)..."
git reset --hard origin/main  # Forcefully reset to remote main

# --- Git Checkout ---
git fetch --all 

# Checkout logic based on whether CHECKOUT_TAG is set
if [ -z "$CHECKOUT_TAG" ]; then
  echo "Checking out latest commit from 'main' branch..."
  git checkout main 
  git pull origin main  # Pull latest changes from 'main'
else
  echo "Checking out tag: $CHECKOUT_TAG"
  git checkout "$CHECKOUT_TAG" || { echo "Failed to checkout $CHECKOUT_TAG"; exit 1; }
fi

# --- Download and Prepare Artifacts ---
download_artifact "$FRONTEND_ARTIFACT_NAME"
download_artifact "$BACKEND_ARTIFACT_NAME"

mkdir -p server/build/libs/
unzip -o "$FRONTEND_ARTIFACT_NAME"
# move everything from frontend-web/composeApp to ./composeApp
mv frontend-web/composeApp/* ./composeApp
cp "$BACKEND_ARTIFACT_NAME" server/build/libs/server-all.jar

# --- Generate RSA Keys (if not present) ---
if [ ! -f "./server/ktor.pk8" ]; then
  echo "Generating RSA keys..."
  chmod +x ./server/gen_new_rss_key.sh
  chmod +x ./server/gen_jwks_from_key.sh
  ./server/gen_new_rss_key.sh
  ./server/gen_jwks_from_key.sh
else
  echo "RSA keys found, skipping generation."
fi

# --- Configure Environment (.env) ---
if [ ! -f "./.env" ]; then
  echo "Copying template.env to .env"
  cp ./template.env ./.env
fi 

# --- Prompt for Database Credentials (only on first run) ---
if [ ! "$(docker secret ls | grep db_username)" ]; then 
  read -p "Enter database username: " DB_USER
  read -sp "Enter database password: " DB_PASSWORD
  echo  # Add a newline after password input

  # --- Create Docker Secrets ---
  echo "Creating Docker secrets..."
  # Create secrets using 'echo' and piping to 'docker secret create'
  echo "$DB_USER" | docker secret create db_username -
  echo "$DB_PASSWORD" | docker secret create db_password - 
else
  echo "Docker secrets already exist, skipping creation."
fi

# --- Start Docker Compose ---
docker-compose up -d

echo "Deployment complete!"