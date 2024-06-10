#!/bin/bash

# --- Configuration ---
CONFIG_FILE="deploy_config.env"
BACKUP_DIR="backups"
LOG_FILE="deploy.log"
RELEASE_TAG="latest" # Default release tag
DRY_RUN=0
RESTORE_BACKUP=0

# --- Functions ---

log() {
  local message="$1"
  echo "$(date +'%Y-%m-%d %H:%M:%S') - $message" | tee -a $LOG_FILE
}

error_exit() {
  local message="$1"
  echo "$(date +'%Y-%m-%d %H:%M:%S') - ERROR: $message" | tee -a $LOG_FILE >&2
  read -p "Do you want to revert to the last deployment backup? (Y/n): " revert
  if [[ "$revert" == "Y" || "$revert" == "y" ]]; then
    restore_backup
  fi
  exit 1
}

check_dependencies() {
  log "Checking dependencies..."
  local dependencies=("git" "wget" "openssl" "docker" "docker-compose")
  for dep in "${dependencies[@]}"; do
    if ! command -v $dep &> /dev/null; then
      error_exit "$dep is not installed. Please install it and try again."
    fi
  done
  log "All dependencies are installed."
}

download_artifact() {
  local artifact_name="$1"
  local artifact_url="https://github.com/samoylenkodmitry/shrtlin/releases/download/$RELEASE_TAG/$artifact_name"
  log "Downloading $artifact_name from $artifact_url..."
  wget -O "$artifact_name" "$artifact_url" || error_exit "Download failed for $artifact_name"
  log "Downloaded $artifact_name successfully."
}

prompt_for_overwrite() {
  local artifact_name="$1"
  if [ -f "$artifact_name" ]; then
    read -p "$artifact_name already exists. Overwrite? (N/y): " overwrite
    echo "$overwrite" # Return the decision (y/n)
  else
    echo "y" # File doesn't exist, default to download
  fi
}

load_dotenv() {
  log "Loading environment variables..."
  if [ -f .env ]; then
    set -a
    source .env
    set +a
    log ".env file loaded."
  else
    error_exit ".env file not found. Exiting..."
  fi
}

validate_env_vars() {
  log "Validating environment variables..."
  local required_vars=("DOMAIN" "LETSENCRYPT_EMAIL")
  for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
      error_exit "Environment variable $var is not set. Exiting..."
    fi
  done
  log "All required environment variables are set."
}

start_docker_service() {
  local service_name="$1"
  local service_dir="$2"

  if [ -z "$(docker ps -q -f name=$service_name)" ]; then
    read -p "$service_name not running. Start $service_name? (Y/n): " start_service
    [ "$start_service" == "n" ] && log "Skipping $service_name start." && return 0
    log "Starting $service_name..."
    cd "$service_dir" || error_exit "Failed to change directory to $service_dir"
    docker-compose up -d --build || error_exit "Failed to start $service_name"
    cd - > /dev/null
  else
    log "$service_name already running."
    read -p "Restart $service_name? (y/N): " restart_service
    if [ "$restart_service" == "y" ];then
      log "Restarting $service_name..."
      cd "$service_dir" || error_exit "Failed to change directory to $service_dir"
      docker-compose down || error_exit "Failed to stop $service_name"
      docker-compose up -d --build || error_exit "Failed to start $service_name"
      cd - > /dev/null
    fi
  fi
}

backup_existing_deployment() {
  log "Backing up existing deployment..."
  mkdir -p $BACKUP_DIR
  tar -czf $BACKUP_DIR/backup_$(date +'%Y%m%d_%H%M%S').tar.gz composeApp server/build/libs/ || error_exit "Backup failed. Exiting..."
  log "Backup completed successfully."
}

restore_backup() {
  local latest_backup=$(ls -t $BACKUP_DIR | head -n 1)
  if [ -z "$latest_backup" ]; then
    error_exit "No backups found to revert to."
  fi
  log "Restoring backup $latest_backup"
  tar -xzf $BACKUP_DIR/$latest_backup || error_exit "Failed to restore backup."
  log "Restored to backup $latest_backup successfully."
}

usage() {
  echo "Usage: $0 [options]"
  echo "Options:"
  echo "  -h, --help       Show this help message and exit"
  echo "  -d, --dry-run    Perform a dry run without making any changes"
  echo "  -t, --tag TAG    Specify a release tag (default: latest)"
  echo "  -r, --restore    Restore the latest backup"
  exit 0
}

parse_args() {
  while [[ "$#" -gt 0 ]]; do
    case $1 in
      -h|--help) usage ;;
      -d|--dry-run) DRY_RUN=1 ;;
      -t|--tag) RELEASE_TAG="$2"; shift ;;
      -r|--restore) RESTORE_BACKUP=1 ;;
      *) echo "Unknown parameter passed: $1"; usage ;;
    esac
    shift
  done
}

health_check() {
  local service_name="$1"
  local service_url="$2"
  local max_retries=5
  local retry_count=0

  log "Checking health of $service_name at $service_url"

  while [ $retry_count -lt $max_retries ]; do
    http_code=$(curl -s -o /dev/null -w "%{http_code}" "$service_url")
    if [ "$http_code" == "200" ]; then
      log "$service_name is healthy."
      return 0 # Success! Exit the function
    else
      log "Health check failed for $service_name (HTTP $http_code). Retrying... ($((retry_count+1))/$max_retries)"
      retry_count=$((retry_count + 1))
      sleep 5
    fi
  done

  # If the loop completes without success
  error_exit "$service_name health check failed after $max_retries attempts. Exiting..." 
}

cleanup() {
  log "Cleaning up temporary files..."
  rm -f deploy.sh.bak
  log "Cleanup completed."
}

# --- Main Script ---

log "Starting deployment script."
parse_args "$@"

if [ "$RESTORE_BACKUP" == "1" ]; then
  log "Restoring the latest backup..."
  restore_backup
  exit 0
fi

if [ "$DRY_RUN" == "1" ]; then
  log "Performing a dry run..."
  exit 0
fi

# Check for required dependencies
check_dependencies

# Load configurations from a separate file
if [ -f "$CONFIG_FILE" ]; then
  source "$CONFIG_FILE"
else
  error_exit "Configuration file $CONFIG_FILE not found. Exiting..."
fi

# Ask user to confirm before resetting local changes
git status --short
read -p "Reset local changes? (Y/n): " reset_local
[ "$reset_local" == "n" ] && log "Skipping reset." && exit 0

log "Resetting local changes (hard reset)..."
git reset --hard origin/main 

cp deploy.sh deploy.sh.bak

git fetch --all 

if [ -z "$CHECKOUT_TAG" ]; then
  log "Checking out latest commit from 'main' branch..."
  git checkout main 
  git pull origin main
else
  log "Checking out tag: $CHECKOUT_TAG"
  git checkout "$CHECKOUT_TAG" || error_exit "Failed to checkout $CHECKOUT_TAG"
fi

if ! cmp -s deploy.sh deploy.sh.bak; then
  log "deploy.sh has changed. Reloading script..."
  rm deploy.sh.bak
  # ask user if them want to run new script
  read -p "deploy.sh has changed. Do you want to run the new script? (Y/n): " run_new_script
  if [ "$run_new_script" == "n" ]; then
    log "Skipping new script. Exiting..."
    exit 0
  fi
  sudo chmod +x ./deploy.sh
  exec ./deploy.sh
  exit 0
fi

rm deploy.sh.bak

# Load configurations again after checkout
if [ -f "$CONFIG_FILE" ]; then
  source "$CONFIG_FILE"
else
  error_exit "Configuration file $CONFIG_FILE not found. Exiting..."
fi

# Ensure necessary directories exist
mkdir -p ./nginx/dhparam ./server/build/libs

if [ ! -f "./nginx/dhparam/dhparam-2048.pem" ]; then
  log "Generating Diffie-Hellman parameters..."
  openssl dhparam -out ./nginx/dhparam/dhparam-2048.pem 2048 || error_exit "Failed to generate DH parameters"
else
  log "Diffie-Hellman parameters found, skipping generation."
fi

log "Backing up existing deployment."
backup_existing_deployment

log "Checking for existing artifacts:"

frontend_overwrite=$(prompt_for_overwrite "$FRONTEND_ARTIFACT_NAME")
backend_overwrite=$(prompt_for_overwrite "$BACKEND_ARTIFACT_NAME")
# Start downloads based on decisions, ALL in the background
[ "$frontend_overwrite" == "y" ] && download_artifact "$FRONTEND_ARTIFACT_NAME" &
[ "$backend_overwrite" == "y" ] && download_artifact "$BACKEND_ARTIFACT_NAME" &

wait

unzip -o "$FRONTEND_ARTIFACT_NAME" || error_exit "Failed to unzip $FRONTEND_ARTIFACT_NAME"
rm -rf composeApp/build/
mkdir -p composeApp/build/dist
mv -f frontend-web-wasm/composeApp/build/dist/* ./composeApp/build/dist || error_exit "Failed to move frontend files"
mv -f frontend-web-js/composeApp/build/dist/* ./composeApp/build/dist || error_exit "Failed to move frontend files"
cp "$BACKEND_ARTIFACT_NAME" server/build/libs/server-all.jar || error_exit "Failed to copy backend artifact"

if [ ! -f "./server/ktor.pk8" ]; then
  log "Generating RSA keys..."
  chmod +x ./server/gen_new_rss_key.sh
  chmod +x ./server/gen_jwks_from_key.sh
  ./server/gen_new_rss_key.sh || error_exit "Failed to generate new RSA keys"
  mv ktor.pub server/ktor.pub || error_exit "Failed to move RSA public key"
  mv ktor.key server/ktor.key || error_exit "Failed to move RSA private key"
  mv ktor.pk8 server/ktor.pk8 || error_exit "Failed to move RSA PK8 key"
  cd server || error_exit "Failed to change directory to server"
  ./server/gen_jwks_from_key.sh || error_exit "Failed to generate JWKS"
  cd .. || error_exit "Failed to change directory to root"
  mv jwks.json server/jwks.json || error_exit "Failed to move JWKS file"
else
  log "RSA keys found, skipping generation."
fi

if [ ! -f "./.env" ]; then
  log "Copying template.env to .env"
  cp ./template.env ./.env || error_exit "Failed to copy .env template"
fi

load_dotenv

if [ -z "$DOMAIN" ]; then
  read -p "Enter your domain (e.g., example.com): " DOMAIN
  echo "DOMAIN=$DOMAIN" >> .env 
  load_dotenv
else
  log "Domain found in .env, skipping prompt."
fi

if [ -z "$LETSENCRYPT_EMAIL" ]; then
  read -p "Enter your Let's Encrypt email: " LETSENCRYPT_EMAIL
  echo "LETSENCRYPT_EMAIL=$LETSENCRYPT_EMAIL" >> .env 
  load_dotenv
else
  log "Let's Encrypt email found in .env, skipping prompt."
fi

if [ -z "$ZEROSSL_API_KEY" ]; then
  read -p "Enter your ZeroSSL API key: " ZEROSSL_API_KEY
  echo "ZEROSSL_API_KEY=$ZEROSSL_API_KEY" >> .env 
  load_dotenv
else
  log "ZeroSSL API key found in .env, skipping prompt."
fi

validate_env_vars

if [ ! -f db_username.txt ] || [ ! -f db_password.txt ]; then
  read -p "Enter database username: " DB_USERNAME
  echo "$DB_USERNAME" > db_username.txt
  chmod 600 db_username.txt 
  log "Database username stored in db_username.txt"

  read -sp "Enter database password: " DB_PASSWORD
  echo "$DB_PASSWORD" > db_password.txt
  chmod 600 db_password.txt
  log "Database password stored in db_password.txt"
else
  log "Database credential files found, skipping prompts."
fi

if [ -z "$(docker network ls --filter name=proxy-tier -q)" ]; then
  read -p "Docker network 'proxy-tier' not found. Create 'proxy-tier' network? (Y/n): " create_network
  if [ "$create_network" == "n" ]; then
    log "Skipping network creation. Exiting..."
    exit 0
  fi
  log "Creating Docker network 'proxy-tier'..."
  docker network create proxy-tier || error_exit "Failed to create Docker network"
else
  log "Docker network 'proxy-tier' found."
fi

start_docker_service "nginx-proxy" "./dockers/reverseproxy"
start_docker_service "backend" "./dockers/backend"
start_docker_service "frontend" "./dockers/frontend"

health_check "Nginx" "$DOMAIN"
health_check "Backend" "$DOMAIN/pow/get"

cleanup

log "Deployment completed successfully."