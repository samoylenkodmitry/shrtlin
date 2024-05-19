FROM nginx:stable-alpine

# Copy built frontend
COPY ./build/dist/wasmJs/productionExecutable/* /usr/share/nginx/html/

# Expose port
EXPOSE 80