# System Test and Bug Report

This document outlines the procedures to tear down and rebuild the local environment, test the SOC 2 Readiness Manager features end-to-end via cURL, and document any bugs discovered.

## 1. Environment Setup

To ensure a clean testing environment, please tear down the existing infrastructure and rebuild it.

```bash
# 1. Stop and remove existing containers and volumes
docker compose down -v

# 2. Start the infrastructure (PostgreSQL, Redis, Mailhog)
docker compose up -d

# 3. Start the Spring Boot Application
cd backend
./mvnw clean spring-boot:run
```

**Note on Authentication:** 
Because `AuthController` is a Developer 2 responsibility and is currently absent, you will need to generate a valid JWT manually using the `JwtUtil` class or your unit tests, and replace `<YOUR_JWT_TOKEN>` in the cURL commands below.

## 2. API Testing Sequence

Use the following cURL commands to verify the endpoints implemented by Java Developer 1.

### 2.1 Create a Readiness Item
```bash
curl -X POST http://localhost:8080/api/readiness-items/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -d '{
    "title": "Enable MFA for all users",
    "controlReference": "CC-002",
    "description": "Ensure MFA is strictly enforced across all production environments.",
    "category": "SECURITY",
    "status": "NOT_STARTED",
    "priority": "HIGH",
    "ownerName": "Alice Security",
    "ownerEmail": "alice@example.com",
    "readinessScore": 0,
    "dueDate": "2026-08-01"
  }'
```

### 2.2 Get Readiness Item by ID
*Note: Replace `1` with the ID returned from the previous creation request.*
```bash
curl -X GET http://localhost:8080/api/readiness-items/1 \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

### 2.3 Get All Readiness Items (Paginated)
```bash
curl -X GET "http://localhost:8080/api/readiness-items/all?page=0&size=10" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

### 2.4 Upload a File Attachment
*Note: Create a dummy text file named `evidence.txt` first.*
```bash
echo "Evidence data" > evidence.txt

curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -F "file=@evidence.txt"
```

### 2.5 Download a File Attachment
*Note: Replace `<UUID>` with the filename UUID returned from the upload request.*
```bash
curl -X GET http://localhost:8080/api/files/<UUID> \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -o downloaded_evidence.txt
```

---

## 3. Bug Report Template

If you encounter any issues during testing, log them below using this template.

### Open Bugs
*No active bugs currently.*

### Template
**Bug ID**: BUG-001
**Priority**: [P1/P2/P3]
**Title**: Short description of the issue
**Steps to Reproduce**:
1. Run command X
2. Expect Y
3. Observe Z instead
**Proposed Fix**: (Optional) What component needs adjusting?

---
*Testing completed by: Java Developer 1*
