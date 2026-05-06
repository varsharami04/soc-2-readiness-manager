import pytest
import json
from unittest.mock import patch, MagicMock
from datetime import datetime


# ================================
# MOCK HELPERS
# ================================

def mock_groq_response(content: str):
    """Create a mock Groq API response"""
    mock_response = MagicMock()
    mock_response.choices[0].message.content = content
    return mock_response


def mock_describe_result():
    return {
        "input": "access control policy",
        "description": "Access control policy is a set of rules that govern who can access systems.",
        "word_count": 150,
        "generated_at": datetime.utcnow().isoformat(),
        "status": "success",
        "is_fallback": False
    }


def mock_recommend_result():
    return {
        "input": "access control policy",
        "recommendations": [
            {
                "action_type": "Policy",
                "description": "Develop a formal access control policy",
                "priority": "High"
            },
            {
                "action_type": "Technical",
                "description": "Implement role based access control",
                "priority": "High"
            },
            {
                "action_type": "Training",
                "description": "Train employees on access control",
                "priority": "Medium"
            }
        ],
        "total": 3,
        "generated_at": datetime.utcnow().isoformat(),
        "status": "success",
        "is_fallback": False
    }


def mock_report_result():
    return {
        "input": "access control policy",
        "report": {
            "title": "SOC 2 Readiness Report: Access Control",
            "executive_summary": "This report assesses access control.",
            "overview": "Access control is critical for SOC 2.",
            "top_items": ["Item 1", "Item 2", "Item 3"],
            "recommendations": [
                {"action": "Implement MFA", "priority": "High"},
                {"action": "Review access rights", "priority": "Medium"},
                {"action": "Train employees", "priority": "Low"}
            ]
        },
        "generated_at": datetime.utcnow().isoformat(),
        "status": "success",
        "is_fallback": False
    }


def mock_analyse_result():
    return {
        "input_length": 200,
        "findings": [
            {
                "type": "insight",
                "category": "Access Control",
                "finding": "Good access control practices found",
                "severity": "info",
                "recommendation": "Continue monitoring"
            },
            {
                "type": "risk",
                "category": "Encryption",
                "finding": "No encryption policy found",
                "severity": "high",
                "recommendation": "Implement encryption"
            }
        ],
        "summary": "Analysis complete",
        "total_findings": 2,
        "generated_at": datetime.utcnow().isoformat(),
        "status": "success",
        "is_fallback": False
    }


# ================================
# TEST 1 — Health Endpoint
# ================================

def test_health_endpoint(client):
    """Test health endpoint returns correct response"""
    response = client.get("/health")
    data = json.loads(response.data)

    assert response.status_code == 200
    assert data["status"] == "ok"
    assert "uptime_seconds" in data
    assert "model" in data
    print("✅ Test 1 passed — Health endpoint working")


# ================================
# TEST 2 — Describe Valid Input
# ================================

@patch("services.describe_service.generate_description")
def test_describe_valid_input(mock_generate, client):
    """Test describe endpoint with valid input"""
    mock_generate.return_value = mock_describe_result()

    response = client.post(
        "/api/describe",
        data=json.dumps({"text": "access control policy"}),
        content_type="application/json"
    )
    data = json.loads(response.data)

    assert response.status_code == 200
    assert data["status"] == "success"
    assert data["is_fallback"] == False
    assert "description" in data
    assert "generated_at" in data
    print("✅ Test 2 passed — Describe valid input working")


# ================================
# TEST 3 — Describe Empty Input
# ================================

def test_describe_empty_input(client):
    """Test describe endpoint with empty input returns 400"""
    response = client.post(
        "/api/describe",
        data=json.dumps({}),
        content_type="application/json"
    )
    data = json.loads(response.data)

    assert response.status_code == 400
    assert data["status"] == "failed"
    assert "error" in data
    print("✅ Test 3 passed — Describe empty input returns 400")


# ================================
# TEST 4 — Describe Short Input
# ================================

def test_describe_short_input(client):
    """Test describe endpoint with too short input returns 400"""
    response = client.post(
        "/api/describe",
        data=json.dumps({"text": "ab"}),
        content_type="application/json"
    )
    data = json.loads(response.data)

    assert response.status_code == 400
    assert data["status"] == "failed"
    print("✅ Test 4 passed — Describe short input returns 400")


# ================================
# TEST 5 — Recommend Valid Input
# ================================

@patch("services.recommend_service.generate_recommendations")
def test_recommend_valid_input(mock_generate, client):
    """Test recommend endpoint with valid input"""
    mock_generate.return_value = mock_recommend_result()

    response = client.post(
        "/api/recommend",
        data=json.dumps({"text": "access control policy"}),
        content_type="application/json"
    )
    data = json.loads(response.data)

    assert response.status_code == 200
    assert data["status"] == "success"
    assert len(data["recommendations"]) == 3
    assert data["total"] == 3
    print("✅ Test 5 passed — Recommend valid input working")


# ================================
# TEST 6 — Recommend Empty Input
# ================================

def test_recommend_empty_input(client):
    """Test recommend endpoint with empty input returns 400"""
    response = client.post(
        "/api/recommend",
        data=json.dumps({}),
        content_type="application/json"
    )
    data = json.loads(response.data)

    assert response.status_code == 400
    assert data["status"] == "failed"
    print("✅ Test 6 passed — Recommend empty input returns 400")


# ================================
# TEST 7 — Generate Report Valid Input
# ================================

@patch("services.report_service.generate_report")
def test_generate_report_valid_input(mock_generate, client):
    """Test generate report endpoint with valid input"""
    mock_generate.return_value = mock_report_result()

    response = client.post(
        "/api/generate-report",
        data=json.dumps({"text": "access control policy"}),
        content_type="application/json"
    )
    data = json.loads(response.data)

    assert response.status_code == 200
    assert data["status"] == "success"
    assert "report" in data
    assert "title" in data["report"]
    assert "executive_summary" in data["report"]
    assert "overview" in data["report"]
    assert "top_items" in data["report"]
    assert "recommendations" in data["report"]
    print("✅ Test 7 passed — Generate report valid input working")


# ================================
# TEST 8 — Generate Report Empty Input
# ================================

def test_generate_report_empty_input(client):
    """Test generate report endpoint with empty input returns 400"""
    response = client.post(
        "/api/generate-report",
        data=json.dumps({}),
        content_type="application/json"
    )
    data = json.loads(response.data)

    assert response.status_code == 400
    assert data["status"] == "failed"
    print("✅ Test 8 passed — Generate report empty input returns 400")


# ================================
# TEST 9 — Analyse Document Valid Input
# ================================

@patch("services.analyse_service.analyse_document")
def test_analyse_document_valid_input(mock_analyse, client):
    """Test analyse document endpoint with valid input"""
    mock_analyse.return_value = mock_analyse_result()

    response = client.post(
        "/api/analyse-document",
        data=json.dumps({
            "text": "Our organization has implemented access control policy."
        }),
        content_type="application/json"
    )
    data = json.loads(response.data)

    assert response.status_code == 200
    assert data["status"] == "success"
    assert "findings" in data
    assert len(data["findings"]) > 0
    assert "summary" in data
    assert "total_findings" in data
    print("✅ Test 9 passed — Analyse document valid input working")


# ================================
# TEST 10 — Analyse Document Empty Input
# ================================

def test_analyse_document_empty_input(client):
    """Test analyse document endpoint with empty input returns 400"""
    response = client.post(
        "/api/analyse-document",
        data=json.dumps({}),
        content_type="application/json"
    )
    data = json.loads(response.data)

    assert response.status_code == 400
    assert data["status"] == "failed"
    print("✅ Test 10 passed — Analyse document empty input returns 400")