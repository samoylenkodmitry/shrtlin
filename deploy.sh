#!/bin/bash
# --- Configuration ---
RELEASE_TAG="v_0.0.0.8" # Update with desired release tag 
                               # (https://github.com/samoylenkodmitry/shrtlin/releases)
FRONTEND_ARTIFACT_NAME="frontend-web.zip"
BACKEND_ARTIFACT_NAME="server-1.0.0.jar"
# Optional: Uncomment and set CHECKOUT_TAG to checkout a specific tag. 
# Otherwise, the latest commit on 'main' will be used.
# CHECKOUT_TAG="your_tag_here" 

download_artifact() {
  if [ -f "$1" ]; then
    read -p "$1 already exists. Overwrite? (N/y): " overwrite
    [ "$overwrite" != "y" ] && echo "Skipping download for $1" && return 0
  fi
  local artifact_url="https://github.com/samoylenkodmitry/shrtlin/releases/download/$RELEASE_TAG/$1"
  wget -O "$1" "$artifact_url" || { echo "Download failed for $1"; exit 1; }
}

# Ask user to confirm before resetting local changes
# Show local changes and prompt for reset
git status --short
read -p "Reset local changes? (Y/n): " reset_local
[ "$reset_local" == "n" ] && echo "Skipping reset." && exit 0

# --- Git Reset and Checkout ---
echo "Resetting local changes (hard reset)..."
git reset --hard origin/main 

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

# --- Create dhparam directory if it doesn't exist ---
if [ ! -d "./nginx/dhparam" ]; then
    sudo mkdir -p ./nginx/dhparam
    sudo chmod 777 ./nginx/dhparam  
fi

# --- Generate DH Parameters (if not present) ---
if [ ! -f "./nginx/dhparam/dhparam-2048.pem" ]; then
  echo "Generating Diffie-Hellman parameters..."
  openssl dhparam -out ./nginx/dhparam/dhparam-2048.pem 2048
else
  echo "Diffie-Hellman parameters found, skipping generation."
fi

# --- Download and Prepare Artifacts ---
download_artifact "$FRONTEND_ARTIFACT_NAME"
download_artifact "$BACKEND_ARTIFACT_NAME"

mkdir -p server/build/libs/
unzip -o "$FRONTEND_ARTIFACT_NAME"
rm -rf composeApp/build/
mv -f frontend-web/composeApp/* ./composeApp
cp "$BACKEND_ARTIFACT_NAME" server/build/libs/server-all.jar

# --- Generate RSA Keys (if not present) ---
if [ ! -f "./server/ktor.pk8" ]; then
  echo "Generating RSA keys..."
  chmod +x ./server/gen_new_rss_key.sh
  chmod +x ./server/gen_jwks_from_key.sh
  ./server/gen_new_rss_key.sh
  mv ktor.pub server/ktor.pub
  mv ktor.key server/ktor.key
  mv ktor.pk8 server/ktor.pk8
  ./server/gen_jwks_from_key.sh
  mv jwks.json server/jwks.json
else
  echo "RSA keys found, skipping generation."
fi

# --- Configure Environment (.env) ---
if [ ! -f "./.env" ]; then
  echo "Copying template.env to .env"
  cp ./template.env ./.env
fi

# --- Load .env file ---
load_dotenv() {
  if [ -f .env ]; then
    set -a
    source .env
    set +a
  else
    echo ".env file not found. Exiting..."
    exit 1
  fi
}

load_dotenv

# --- Prompt for Domain (only on first run) ---
if [ -z "$DOMAIN" ]; then
  read -p "Enter your domain (e.g., example.com): " DOMAIN
  echo "DOMAIN=$DOMAIN" >> .env 
  # reload .env to reflect the change
  load_dotenv
else
  echo "Domain found in .env, skipping prompt."
fi

# --- Prompt for Letsencrypt email (only on first run) ---
if [ -z "$LETSENCRYPT_EMAIL" ]; then
  read -p "Enter your letsencrypt email (e.g., mail@example.com): " LETSENCRYPT_EMAIL
  echo "LETSENCRYPT_EMAIL=$LETSENCRYPT_EMAIL" >> .env 
  # reload .env to reflect the change
  load_dotenv
else
  echo "Letsencrypt email found in .env, skipping prompt."
fi

# --- Prompt ZEROSSL_API_KEY (only on first run) ---
if [ -z "$ZEROSSL_API_KEY" ]; then
  read -p "Enter your ZeroSSL API key: " ZEROSSL_API_KEY
  echo "ZEROSSL_API_KEY=$ZEROSSL_API_KEY" >> .env 
  # reload .env to reflect the change
  load_dotenv
else
  echo "ZeroSSL API key found in .env, skipping prompt."
fi

# --- Prompt for Database Credentials (only on first run) ---
if [ ! -f db_username.txt ] || [ ! -f db_password.txt ]; then
  read -p "Enter database username: " DB_USERNAME
  echo "$DB_USERNAME" > db_username.txt
  chmod 600 db_username.txt 
  echo "Database username stored in db_username.txt"

  read -sp "Enter database password: " DB_PASSWORD
  echo "$DB_PASSWORD" > db_password.txt
  chmod 600 db_password.txt
  echo "Database password stored in db_password.txt"
else
  echo "Database credential files found, skipping prompts." 
fi

# --- Check for Docker network "proxy-tier" ---
if [ -z "$(docker network ls --filter name=proxy-tier -q)" ]; then
  # ask to create network
  read -p "Docker network 'proxy-tier' not found. Create 'proxy-tier' network? (Y/n): " create_network
  if [ "$create_network" == "n" ]; then
    echo "Skipping network creation. Exiting..."
    exit 0
  fi
  echo "Creating Docker network 'proxy-tier'..."
  docker network create proxy-tier
else
  echo "Docker network 'proxy-tier' found."
fi

# --- Start Docker Compose ---
start_docker_service() {
  local service_name=$1
  local service_dir=$2

  if [ -z "$(docker ps -q -f name=$service_name)" ]; then
    read -p "$service_name not running. Start $service_name? (Y/n): " start_service
    [ "$start_service" == "n" ] && echo "Skipping $service_name start." && return 0
    echo "Starting $service_name..."
    cd $service_dir
    docker-compose up -d --build
    cd ../..
  else
    echo "$service_name already running."
    # ask to restart service
    read -p "Restart $service_name? (y/N): " restart_service
    if [ "$restart_service" == "y" ]; then
      echo "Restarting $service_name..."
      cd $service_dir
      docker-compose down
      docker-compose up -d --build
      cd ../..
    fi
  fi
}

start_docker_service "nginx-proxy" "./dockers/reverseproxy"
start_docker_service "backend" "./dockers/backend"
start_docker_service "frontend" "./dockers/frontend"

echo "Deployment complete!"