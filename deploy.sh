#!/bin/bash

# --- Configuration ---
CONFIG_FILE="deploy_config.env"

# Load configurations from a separate file
if [ -f "$CONFIG_FILE" ]; then
  source "$CONFIG_FILE"
else
  echo "Configuration file $CONFIG_FILE not found. Exiting..."
  exit 1
fi

# --- Functions ---
download_artifact() {
  local artifact_name="$1"
  if [ -f "$artifact_name" ]; then
    read -p "$artifact_name already exists. Overwrite? (N/y): " overwrite
    [ "$overwrite" != "y" ] && echo "Skipping download for $artifact_name" && return 0
  fi
  local artifact_url="https://github.com/samoylenkodmitry/shrtlin/releases/download/$RELEASE_TAG/$artifact_name"
  wget -O "$artifact_name" "$artifact_url" || { echo "Download failed for $artifact_name"; exit 1; }
}

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

start_docker_service() {
  local service_name="$1"
  local service_dir="$2"

  if [ -z "$(docker ps -q -f name=$service_name)" ]; then
    read -p "$service_name not running. Start $service_name? (Y/n): " start_service
    [ "$start_service" == "n" ] && echo "Skipping $service_name start." && return 0
    echo "Starting $service_name..."
    cd "$service_dir"
    docker-compose up -d --build
    cd - > /dev/null
  else
    echo "$service_name already running."
    read -p "Restart $service_name? (y/N): " restart_service
    if [ "$restart_service" == "y" ]; then
      echo "Restarting $service_name..."
      cd "$service_dir"
      docker-compose down
      docker-compose up -d --build
      cd - > /dev/null
    fi
  fi
}

# --- Main Script ---

# Ask user to confirm before resetting local changes
git status --short
read -p "Reset local changes? (Y/n): " reset_local
[ "$reset_local" == "n" ] && echo "Skipping reset." && exit 0

echo "Resetting local changes (hard reset)..."
git reset --hard origin/main 

cp deploy.sh deploy.sh.bak

git fetch --all 

if [ -z "$CHECKOUT_TAG" ]; then
  echo "Checking out latest commit from 'main' branch..."
  git checkout main 
  git pull origin main
else
  echo "Checking out tag: $CHECKOUT_TAG"
  git checkout "$CHECKOUT_TAG" || { echo "Failed to checkout $CHECKOUT_TAG"; exit 1; }
fi

if ! cmp -s deploy.sh deploy.sh.bak; then
  echo "deploy.sh has changed. Reloading script..."
  rm deploy.sh.bak
  exec ./deploy.sh
  exit 0
fi

rm deploy.sh.bak

if [ ! -d "./nginx/dhparam" ]; then
  sudo mkdir -p ./nginx/dhparam
  sudo chmod 777 ./nginx/dhparam  
fi

if [ ! -f "./nginx/dhparam/dhparam-2048.pem" ]; then
  echo "Generating Diffie-Hellman parameters..."
  openssl dhparam -out ./nginx/dhparam/dhparam-2048.pem 2048
else
  echo "Diffie-Hellman parameters found, skipping generation."
fi

download_artifact "$FRONTEND_ARTIFACT_NAME"
download_artifact "$BACKEND_ARTIFACT_NAME"

mkdir -p server/build/libs/
unzip -o "$FRONTEND_ARTIFACT_NAME"
rm -rf composeApp/build/
mv -f frontend-web/composeApp/* ./composeApp
cp "$BACKEND_ARTIFACT_NAME" server/build/libs/server-all.jar

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

if [ ! -f "./.env" ]; then
  echo "Copying template.env to .env"
  cp ./template.env ./.env
fi

load_dotenv

if [ -z "$DOMAIN" ]; then
  read -p "Enter your domain (e.g., example.com): " DOMAIN
  echo "DOMAIN=$DOMAIN" >> .env 
  load_dotenv
else
  echo "Domain found in .env, skipping prompt."
fi

if [ -z "$LETSENCRYPT_EMAIL" ]; then
  read -p "Enter your letsencrypt email (e.g., mail@example.com): " LETSENCRYPT_EMAIL
  echo "LETSENCRYPT_EMAIL=$LETSENCRYPT_EMAIL" >> .env 
  load_dotenv
else
  echo "Letsencrypt email found in .env, skipping prompt."
fi

if [ -z "$ZEROSSL_API_KEY" ]; then
  read -p "Enter your ZeroSSL API key: " ZEROSSL_API_KEY
  echo "ZEROSSL_API_KEY=$ZEROSSL_API_KEY" >> .env 
  load_dotenv
else
  echo "ZeroSSL API key found in .env, skipping prompt."
fi

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

if [ -z "$(docker network ls --filter name=proxy-tier -q)" ]; then
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

start_docker_service "nginx-proxy" "./dockers/reverseproxy"
start_docker_service "backend" "./dockers/backend"
start_docker_service "frontend" "./dockers/frontend"

echo "Deployment complete!"