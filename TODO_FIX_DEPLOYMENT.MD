# Deployment Fixes Documentation

This document contains all the fixes applied during the shrtlin VPS deployment process. Keep this as reference for future deployments and development.

## 1. JWKS Generation Script Path Issue

**Problem**: The `deploy.sh` script's JWKS generation was failing due to incorrect path handling.

**Location**: `deploy.sh` line with JWKS generation

**Solution**: Manual JWKS generation was required. The script attempted to generate JWKS but failed due to path context issues.

**Manual Fix Applied**:
```bash
cd /home/s/develop/projects/shrtlin/server
openssl genpkey -algorithm RSA -out ktor.pk8 -pkcs8 -pkcs8-password pass:password
node ../server/gen_jwks_from_key.sh ktor.pk8 password > certs/jwks.json
```

**Future Fix**: Update `deploy.sh` to handle proper directory context during JWKS generation.

## 2. Environment Variables Missing in Docker Containers

**Problem**: Backend container couldn't access environment variables from main `.env` file.

**Location**: `dockers/backend/` and `dockers/frontend/` directories

**Solution**: Copy `.env` file to subdirectories where Docker Compose files are located.

**Commands Applied**:
```bash
cp deploy_config.env dockers/backend/.env
cp deploy_config.env dockers/frontend/.env
cp deploy_config.env dockers/reverseproxy/.env
```

**Future Fix**: Modify Docker Compose files to reference parent directory `.env` file or update deployment script to handle this automatically.

## 3. SSL Certificate Provider Switch

**Problem**: ZeroSSL was experiencing delays and rate limiting issues during certificate generation.

**Location**: Environment configuration for ACME companion

**Original Configuration**:
```
CA_SERVER=https://acme.zerossl.com/v2/DV90
EAB_KID=2bc12f..................b8a1989e
EAB_HMAC_KEY=<key>
```

**Solution**: Switched to Let's Encrypt for faster certificate generation.

**Updated Configuration**:
```
# CA_SERVER=https://acme.zerossl.com/v2/DV90  # Commented out
# EAB_KID=2bc1...................eb8a1989e   # Commented out
# EAB_HMAC_KEY=<key>                          # Commented out
```

**Result**: SSL certificate generated successfully with Let's Encrypt, valid until October 8, 2025.

## 4. CRITICAL: ACME Challenge Routing Conflict

**Problem**: The most critical issue was nginx-proxy routing conflicts preventing SSL certificate validation.

**Root Cause**: Backend container's `VIRTUAL_PATH` was capturing `.well-known/acme-challenge/*` requests, preventing ACME companion from validating domain ownership.

**Location**: `dockers/backend/docker-compose.yml`

**Original Configuration**:
```yaml
VIRTUAL_PATH: "~^/(?!frontend-files).+$"
```

**Problem Analysis**: This regex captured ALL paths except `frontend-files`, including ACME challenge paths needed for SSL validation.

**Solution**: Modified regex to exclude both frontend files AND ACME challenges.

**Fixed Configuration**:
```yaml
VIRTUAL_PATH: "~^/(?!(\.well-known/acme-challenge.*|frontend-files)).+$"
```

**Explanation**: The new regex excludes:
- `.well-known/acme-challenge.*` - ACME challenge paths for SSL validation
- `frontend-files` - Frontend static files (original exclusion)

## 5. Backend Application ACME Handling

**Investigation**: Checked if backend application handles ACME challenges.

**Location**: `server/src/main/kotlin/in/shrtl/app/Application.kt`

**Current State**: Backend has `.well-known/jwks.json` route for JWT keys but does NOT handle ACME challenges.

**Code Found**:
```kotlin
static(".well-known") {
    files("certs")
}
```

**Conclusion**: Backend correctly serves JWKS but doesn't interfere with ACME challenges. The routing fix in Docker configuration was the correct solution.

## Deployment Verification Results

After all fixes were applied:

✅ **HTTP Access**: `http://shrtl.in` - Working  
✅ **HTTPS Access**: `https://shrtl.in` - Working with valid SSL  
✅ **Backend API**: `https://shrtl.in/pow/get` - Working correctly  
✅ **SSL Certificate**: Valid until October 8, 2025  
✅ **All Containers**: Healthy and running  

## Architecture Overview

The shrtlin application uses:
- **Kotlin Multiplatform**: Cross-platform URL shortening service
- **Docker Architecture**: nginx-proxy, frontend (nginx), backend (Ktor), PostgreSQL, Redis
- **Authentication**: JWT with Proof of Work challenge system
- **SSL**: ACME companion with Let's Encrypt for automatic certificates

## Future Deployment Checklist

1. **Environment Setup**:
   - [ ] Copy `.env` file to all docker subdirectories
   - [ ] Verify all required environment variables are set

2. **JWKS Generation**:
   - [ ] Ensure proper directory context for script execution
   - [ ] Verify RSA key and JWKS file generation

3. **SSL Configuration**:
   - [ ] Use Let's Encrypt for reliable certificate generation
   - [ ] Verify VIRTUAL_PATH regex excludes ACME challenges
   - [ ] Test certificate validation before full deployment

4. **Routing Verification**:
   - [ ] Ensure nginx-proxy routing doesn't interfere with SSL validation
   - [ ] Test both HTTP and HTTPS access
   - [ ] Verify backend API endpoints work over SSL

## Emergency Fixes Reference

If SSL fails during deployment:

1. **Check ACME challenges**: Ensure `/.well-known/acme-challenge/` is accessible
2. **Verify routing**: Backend VIRTUAL_PATH must not capture ACME paths
3. **Check logs**: `docker logs nginx-proxy-acme` for certificate generation errors
4. **Fallback**: Switch to Let's Encrypt if using ZeroSSL

## Contact Information

For questions about these fixes or future deployment issues, refer to this document and the deployment logs.

---
*Generated: August 10, 2025*  
*Deployment: shrtl.in VPS*  
*Status: All systems operational*
