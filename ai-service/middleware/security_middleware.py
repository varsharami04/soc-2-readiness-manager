import re
from flask import request, jsonify

# Common malicious patterns
SUSPICIOUS_PATTERNS = [
    r"ignore previous instructions",
    r"system prompt",
    r"<script.*?>.*?</script>",
    r"select .* from",
    r"drop table",
    r"--",
    r";",
    r"union select",
]

def is_malicious(input_text):
    input_text = input_text.lower()
    for pattern in SUSPICIOUS_PATTERNS:
        if re.search(pattern, input_text):
            return True
    return False


def sanitize_input(data):
    if isinstance(data, str):
        # Remove HTML tags
        data = re.sub(r"<.*?>", "", data)
        return data.strip()

    if isinstance(data, dict):
        return {k: sanitize_input(v) for k, v in data.items()}

    if isinstance(data, list):
        return [sanitize_input(item) for item in data]

    return data


def security_middleware():
    if request.method in ["POST", "PUT"]:
        data = request.get_json(silent=True)

        if not data:
            return jsonify({"error": "Invalid or empty JSON input"}), 400

        # Sanitize input
        clean_data = sanitize_input(data)

        # Convert to string for pattern check
        combined_text = str(clean_data)

        # Detect malicious patterns
        if is_malicious(combined_text):
            return jsonify({
                "error": "Malicious input detected",
                "message": "Request blocked due to security policy"
            }), 400

        # Replace request data with sanitized version
        request._cached_json = clean_data