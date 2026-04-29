from flask import Blueprint, request, jsonify
from services.describe_service import generate_description

describe_bp = Blueprint("describe", __name__)


@describe_bp.post("/describe")
def describe():

    data = request.get_json(force=True)

    if not data:
        return jsonify({
            "error": "Request body is required",
            "status": "failed"
        }), 400

    text = data.get("text", "").strip()

    if not text:
        return jsonify({
            "error": "text field is required",
            "status": "failed"
        }), 400

    if len(text) < 3:
        return jsonify({
            "error": "text must be at least 3 characters",
            "status": "failed"
        }), 400

    if len(text) > 500:
        return jsonify({
            "error": "text must not exceed 500 characters",
            "status": "failed"
        }), 400

    result = generate_description(text)
    return jsonify(result)