# SOC 2 Readiness Manager — AI Service

AI microservice for the SOC 2 Readiness Manager project.
Built with Flask and powered by Groq LLaMA-3.3-70b.
Provides AI-powered endpoints for SOC 2 compliance analysis.

---
## Table of Contents

- [Prerequisites](#prerequisites)
- [Setup Steps](#setup-steps)
- [Environment Variables](#environment-variables)
- [Run Instructions](#run-instructions)
- [API Reference](#api-reference)
- [Running Tests](#running-tests)
- [Project Structure](#project-structure)

---
## Prerequisites

Make sure you have the following installed
before running this service:

| Requirement | Version | Download |
|---|---|---|
| Python | 3.11 or higher | python.org |
| pip | Latest | Comes with Python |
| Git | 2.x or higher | git-scm.com |
| Groq API Key | Free tier | console.groq.com |

---

## Setup Steps

### Step 1 — Clone the Repository

git clone https://github.com/KulsumQavi/soc-2-readiness-manager.git
cd soc-2-readiness-manager/ai-services

### Step 2 — Install Dependencies

pip install -r requirements.txt

### Step 3 — Create Environment File

cp .env.example .env

Open .env and add your Groq API key.

### Step 4 — Run the Service

python app.py

Service will start on http://localhost:5000

---

## Environment Variables

Create a .env file in the ai-services/ folder
with the following variables:

| Variable | Required | Description |
|---|---|---|
| GROQ_API_KEY | Yes | Your Groq API key from console.groq.com |
| FLASK_ENV | No | Set to development or production |
| FLASK_DEBUG | No | Set to 1 for debug mode |

Example .env file:

GROQ_API_KEY=your_groq_api_key_here
FLASK_ENV=development
FLASK_DEBUG=1

Never commit your .env file to GitHub.

---

## Run Instructions

### Development Mode

python app.py

### Production Mode

gunicorn -w 4 -b 0.0.0.0:5000 "app:create_app()"

### Verify Service is Running

GET http://localhost:5000/health

Expected response:
{
    "status": "ok",
    "model": "llama-3.3-70b-versatile",
    "uptime_seconds": 5,
    "chroma_status": "ok",
    "chroma_chunks": 13,
    "service": "ai-service",
    "version": "1.0.0"
}

---

## API Reference

Base URL: http://localhost:5000

---

### 1. Health Check

GET /health

Response:
{
    "status": "ok",
    "model": "llama-3.3-70b-versatile",
    "uptime_seconds": 120,
    "chroma_status": "ok",
    "chroma_chunks": 13
}

---

### 2. Describe

POST /api/describe

Request:
{
    "text": "access control policy"
}

Response:
{
    "input": "access control policy",
    "description": "An access control policy refers to...",
    "word_count": 159,
    "generated_at": "2026-05-03T10:00:00",
    "status": "success",
    "is_fallback": false
}

---

### 3. Recommend

POST /api/recommend

Request:
{
    "text": "access control policy"
}

Response:
{
    "input": "access control policy",
    "recommendations": [
        {
            "action_type": "Policy",
            "description": "Develop a formal policy",
            "priority": "High"
        },
        {
            "action_type": "Technical",
            "description": "Implement technical controls",
            "priority": "High"
        },
        {
            "action_type": "Training",
            "description": "Train employees",
            "priority": "Medium"
        }
    ],
    "total": 3,
    "status": "success",
    "is_fallback": false
}

---

### 4. Generate Report

POST /api/generate-report

Request:
{
    "text": "access control policy"
}

Response:
{
    "input": "access control policy",
    "report": {
        "title": "SOC 2 Readiness Report: Access Control",
        "executive_summary": "This report assesses...",
        "overview": "Access control is critical...",
        "top_items": ["Item 1", "Item 2", "Item 3"],
        "recommendations": [
            {"action": "Implement MFA", "priority": "High"},
            {"action": "Review access", "priority": "Medium"},
            {"action": "Train staff", "priority": "Low"}
        ]
    },
    "status": "success",
    "is_fallback": false
}

---

### 5. Generate Report Stream

GET /api/generate-report/stream?text=access+control+policy

Response (text/event-stream):
data: {"token": "{", "done": false}
data: {"token": "title", "done": false}
data: {"token": "", "done": true}

---

### 6. Query

POST /api/query

Request:
{
    "question": "What is access control in SOC 2?"
}

Response:
{
    "question": "What is access control in SOC 2?",
    "answer": "Access control in SOC 2 refers to...",
    "sources": ["chunk 1", "chunk 2", "chunk 3"],
    "status": "success",
    "is_fallback": false
}

---

### 7. Analyse Document

POST /api/analyse-document

Request:
{
    "text": "Our organization has implemented access control..."
}

Response:
{
    "input_length": 145,
    "findings": [
        {
            "type": "insight",
            "category": "Access Control",
            "finding": "Good practices found",
            "severity": "info",
            "recommendation": "Continue monitoring"
        },
        {
            "type": "risk",
            "category": "Encryption",
            "finding": "No encryption policy",
            "severity": "high",
            "recommendation": "Implement encryption"
        }
    ],
    "summary": "Analysis complete",
    "total_findings": 2,
    "status": "success",
    "is_fallback": false
}

---

### 8. Batch Process

POST /api/batch-process

Request:
{
    "items": [
        "access control policy",
        "encryption at rest",
        "incident response plan"
    ]
}

Response:
{
    "total_items": 3,
    "processed": 3,
    "failed": 0,
    "results": [
        {
            "index": 0,
            "input": "access control policy",
            "description": "AI response...",
            "status": "success",
            "is_fallback": false
        }
    ],
    "status": "success"
}

---

## Running Tests

python -m pytest tests/test_endpoints.py -v

Expected output:
test_health_endpoint PASSED
test_describe_valid_input PASSED
test_describe_empty_input PASSED
test_describe_short_input PASSED
test_recommend_valid_input PASSED
test_recommend_empty_input PASSED
test_generate_report_valid_input PASSED
test_generate_report_empty_input PASSED
test_analyse_document_valid_input PASSED
test_analyse_document_empty_input PASSED
10 passed

---

## Project Structure

ai-services/
├── routes/
│   ├── health.py
│   ├── describe.py
│   ├── recommend.py
│   ├── report.py
│   ├── query.py
│   ├── analyse.py
│   └── batch.py
├── services/
│   ├── describe_service.py
│   ├── recommend_service.py
│   ├── report_service.py
│   ├── report_stream_service.py
│   ├── query_service.py
│   ├── analyse_service.py
│   ├── batch_service.py
│   └── chroma_client.py
├── prompts/
│   ├── describe_prompt.txt
│   ├── recommend_prompt.txt
│   ├── report_prompt.txt
│   └── analyse_prompt.txt
├── tests/
│   ├── conftest.py
│   └── test_endpoints.py
├── app.py
├── requirements.txt
├── .env.example
└── README.md

---
