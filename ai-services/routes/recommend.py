from flask import Blueprint, request, jsonify
from services.recommend_service import generate_recommendations

recommend_bp = Blueprint("recommend", __name__)


@recommend_bp.post("/recommend")
def recommend():
    # Step 1 - Get JSON data
    data = request.get_json(force=True)

    # Step 2 - Validate input
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
    if len(text) < 3:
        return jsonify({
            "error": "text must be at least 3 characters",
            "status": "failed"
        }), 400

    # Step 5 - Validate maximum length
    if len(text) > 500:
        return jsonify({
            "error": "text must not exceed 500 characters",
            "status": "failed"
        }), 400

    # Step 6 - Call service and return result
    result = generate_recommendations(text)
    return jsonify(result)