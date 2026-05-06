from flask import Blueprint, request, jsonify
from services.query_service import generate_answer

query_bp = Blueprint("query", __name__)


@query_bp.post("/query")
def query():
    # Step 1 - Get JSON data
    data = request.get_json(force=True)

    # Step 2 - Validate input
    if not data:
        return jsonify({
            "error": "Request body is required",
            "status": "failed"
        }), 400

    question = data.get("question", "").strip()

    # Step 3 - Validate question field
    if not question:
        return jsonify({
            "error": "question field is required",
            "status": "failed"
        }), 400

    # Step 4 - Validate minimum length
    if len(question) < 3:
        return jsonify({
            "error": "question must be at least 3 characters",
            "status": "failed"
        }), 400

    # Step 5 - Generate answer and return
    result = generate_answer(question)
    return jsonify(result)