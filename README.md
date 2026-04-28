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


