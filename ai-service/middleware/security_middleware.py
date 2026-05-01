import re
import logging
from flask import request, jsonify

# Patterns to detect malicious input
SUSPICIOUS_PATTERNS = [
    r"ignore previous instructions",
    r"system prompt",
    r"<script.*?>.*?</script>",
    r"select .* from",
    r"drop table",
    r"union select",
    r"--",
    r";"
]

# Max allowed input size
MAX_INPUT_LENGTH = 500


# Check for malicious patterns
def is_malicious(input_text):
    input_text = input_text.lower()
    for pattern in SUSPICIOUS_PATTERNS:
        if re.search(pattern, input_text):
            return True
    return False


# Sanitize input (remove HTML tags)
def sanitize_input(data):
    if isinstance(data, str):
        data = re.sub(r"<.*?>", "", data)
        return data.strip()

    elif isinstance(data, dict):
        return {key: sanitize_input(value) for key, value in data.items()}

    elif isinstance(data, list):
        return [sanitize_input(item) for item in data]

    return data


# PII detection
def contains_pii(text):
    email_pattern = r"[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+"
    phone_pattern = r"\b\d{10}\b"

    if re.search(email_pattern, text):
        return True
    if re.search(phone_pattern, text):
        return True
    return False


# Main middleware function
def security_middleware():

    # Apply only for POST and PUT requests
    if request.method in ["POST", "PUT"]:

        # JWT AUTH CHECK (Day 10)
        token = request.headers.get("Authorization")

        if not token:
            logging.warning("Blocked request without JWT token")
            return jsonify({
                "error": "Unauthorized",
                "message": "JWT token missing"
            }), 401

        # Check Bearer format
        if not token.startswith("Bearer "):
            logging.warning("Invalid JWT format")
            return jsonify({
                "error": "Unauthorized",
                "message": "Invalid token format"
            }), 401

        # Get JSON data
        data = request.get_json(silent=True)

        # Invalid or empty input
        if data is None:
         return jsonify({
        "error": "Invalid or empty JSON input"
    }), 400

        # Input too large
        if len(str(data)) > MAX_INPUT_LENGTH:
            logging.warning("Blocked large input")
            return jsonify({
                "error": "Input too large",
                "message": "Maximum allowed size exceeded"
            }), 400

        # Sanitize input
        clean_data = sanitize_input(data)

        # Convert to string for checks
        combined_text = str(clean_data)

        # PII detection
        if contains_pii(combined_text):
            logging.warning("Blocked request containing possible PII")
            return jsonify({
                "error": "Sensitive data not allowed"
            }), 400

        # Malicious input detection
        if is_malicious(combined_text):
            logging.warning("Blocked malicious input")
            return jsonify({
                "error": "Malicious input detected",
                "message": "Request blocked due to security policy"
            }), 400

        # Store cleaned data
        request.cleaned_data = clean_data

    # Allow request if everything is valid
    return None
