from flask import Blueprint, request, jsonify
from services.analyse_service import analyse_document

analyse_bp = Blueprint("analyse", __name__)


@analyse_bp.post("/analyse-document")
def analyse():
    # Step 1 - Get JSON data
    data = request.get_json(force=True)

    # Step 2 - Validate input exists
    if not data:
        return jsonify({
            "error": "Request body is required",
            "status": "failed"
        }), 400

    text = data.get("text", "").strip()

    # Step 3 - Validate text field
    if not text:
        return jsonify({
            "error": "text field is required",
            "status": "failed"
        }), 400

    # Step 4 - Validate minimum length
    if len(text) < 10:
        return jsonify({
            "error": "text must be at least 10 characters",
            "status": "failed"
        }), 400

    # Step 5 - Validate maximum length
    if len(text) > 5000:
        return jsonify({
            "error": "text must not exceed 5000 characters",
            "status": "failed"
        }), 400

    # Step 6 - Analyse document and return result
    result = analyse_document(text)
    return jsonify(result)