# Tool-16 — SOC 2 Readiness Manager

## Overview
SOC 2 Readiness Manager is an AI-powered web application built as an MVP Capstone Project. It helps organizations track, manage, and audit their compliance readiness for SOC 2 controls. The application provides a robust backend utilizing a modern Java stack with role-based access control, caching, and an integrated Python AI microservice for automated compliance analysis and reporting.

## Architecture

```text
                               +-------------------------+
                               |     React Frontend      |
                               |    (Vite, Tailwind)     |
                               +-----------+-------------+
                                           | HTTP/REST (JWT)
                                           v
+-------------------+          +-------------------------+          +-------------------+
|    Redis 7        | <------> |   Spring Boot Backend   | -------> | JavaMailSender    |
| (Response Caching)|          |       (Java 17)         |          | (Email Alerts)    |
+-------------------+          +---+-----------------^---+          +-------------------+
                                   |                 |
                               JDBC|                 | HTTP/REST
                                   v                 v
+-------------------+          +-------------------------+          +-------------------+
|  PostgreSQL 15    |          |    Flask AI Service     | -------> |    ChromaDB       |
|  (Core Database)  |          |       (Python 3)        |          |  (Vector Store)   |
+-------------------+          +-----------+-------------+          +-------------------+
                                           |
                                           v
                               +-------------------------+
                               |        Groq API         |
                               |     (LLaMA-3.3-70b)     |
                               +-------------------------+
```

## Prerequisites
Before running this project, ensure you have the following installed on your machine:
- **Java 17** (Adoptium recommended)
- **Maven** (or use the included `./mvnw` wrapper)
- **Docker & Docker Compose** (for running the full stack: Postgres, Redis, AI Service)
- **Python 3.11** (If running the AI service locally outside of Docker)
- **Git**

## Setup Steps
1. **Clone the repository:**
   ```bash
   git clone <repository_url>
   cd soc-2-readiness-manager
   ```

2. **Configure Environment Variables:**
   Create a `.env` file from the provided example template and fill in your secrets (especially your Groq API key and a secure JWT secret).
   ```bash
   cp .env.example .env
   ```

3. **Start the Infrastructure (Docker):**
   You can spin up the entire application stack including the database, cache, backend, frontend, and AI microservice using Docker Compose:
   ```bash
   docker-compose up --build
   ```

   *(Alternatively, to run the Spring Boot backend natively for development)*:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

4. **Verify Application Health:**
   - **Backend API & Swagger Docs**: `http://localhost:8080/swagger-ui.html`
   - **Frontend Application**: `http://localhost:80`
   - **AI Microservice Health**: `http://localhost:5000/health`

## Environment Variables (`.env.example` Reference)

| Variable | Description | Default / Example Value |
|----------|-------------|-------------------------|
| `DB_URL` | PostgreSQL connection string | `jdbc:postgresql://postgres:5432/soc2_db` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `secret_password` |
| `JPA_SHOW_SQL` | Toggle Hibernate SQL logging | `false` |
| `REDIS_HOST` | Redis cache server host | `redis` |
| `REDIS_PORT` | Redis cache server port | `6379` |
| `REDIS_PASSWORD` | Redis password (if any) | `""` |
| `MAIL_HOST` | SMTP server for email notifications | `mailhog` |
| `MAIL_PORT` | SMTP port | `1025` |
| `MAIL_USERNAME` | SMTP username | `""` |
| `MAIL_PASSWORD` | SMTP password | `""` |
| `JWT_SECRET` | 32+ char secret for signing JWTs | `your-secure-development-32-char-jwt-secret!` |
| `JWT_EXPIRATION_MS` | JWT validity duration (in ms) | `86400000` (24 hours) |
| `SERVER_PORT` | Spring Boot API port | `8080` |
| `GROQ_API_KEY` | API Key for Groq LLaMA models | `gsk_...` |

---

#AI Security Middleware for SOC 2 Compliance

#Project Overview

This project implements a secure AI service using **Flask (Python)** with a focus on protecting APIs from common security threats. The system includes a custom security middleware, rate limiting, and logging mechanisms to ensure safe and controlled AI interactions.

The goal of this project is to align with **SOC 2 security principles** by preventing malicious inputs, controlling API usage, and maintaining proper monitoring.

#Features

*Input Validation and Sanitization
*Prompt Injection Protection
*SQL Injection and Script Injection Prevention
*Rate Limiting (API Abuse Protection)
*Logging of Suspicious Activities
*Security Testing and Validation

#Tech Stack

* **Python**
* **Flask**
* **Flask-Limiter**
* **REST API**

#Project Structure

```
ai-service/
│
├── app.py
├── middleware/
│     └── security_middleware.py
├── security.log
├── requirements.txt
├── SECURITY.md
└── README.md
```

#How to Run the Project

#1. Install Dependencies

```bash
pip install -r requirements.txt
```

#2. Run the Flask Server

```bash
python app.py
```

#3. Server will start at:

```
http://127.0.0.1:5000
```

#API Endpoints

| Endpoint           | Method | Description                |
| ------------------ | ------ | -------------------------- |
| `/`                | GET    | Check if server is running |
| `/health`          | GET    | Health check               |
| `/test`            | POST   | Test security middleware   |
| `/generate-report` | POST   | Rate-limited endpoint      |


#Sample Request

### POST `/test`

```json
{
  "text": "Hello AI"
}
```

#Security Implementation

#1. Security Middleware

*Validates incoming requests
*Sanitizes input data
*Detects malicious patterns

#2. Attack Prevention

The system blocks:

*Prompt Injection (e.g., "ignore previous instructions")
*SQL Injection (e.g., "DROP TABLE users")
*Script Injection (e.g., `<script>alert('hack')</script>`)

#3. Rate Limiting

*Global limit: **30 requests per minute**
*`/generate-report`: **10 requests per minute**

#4. Logging & Monitoring

*All suspicious activities are logged in:

```
security.log
```

*Helps in tracking and auditing attacks



#Testing

The system was tested for:

* ✅ Normal Input Handling
* ❌ Prompt Injection
* ❌ SQL Injection
* ❌ Script Injection
* ❌ Large Payload Attacks
* ❌ API Rate Limit Violations

All tests passed successfully.



#Documentation

Detailed security analysis and testing reports are available in:

```
SECURITY.md
```

#Conclusion

This project demonstrates a secure AI service that protects against common vulnerabilities and ensures safe API usage. It is designed with a focus on **security, reliability, and compliance**.
