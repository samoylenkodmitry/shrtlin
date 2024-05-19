#!/bin/bash

# --- Build Applications --- 

# Build frontend (Web)
./gradlew :composeApp:build

# Build backend
./gradlew :server:build

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