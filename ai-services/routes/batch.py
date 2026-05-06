from flask import Blueprint, request, jsonify
from services.batch_service import batch_process

batch_bp = Blueprint("batch", __name__)


@batch_bp.post("/batch-process")
def batch():
    # Step 1 - Get JSON data
    data = request.get_json(force=True)

    # Step 2 - Validate input exists
    if not data:
        return jsonify({
            "error": "Request body is required",
            "status": "failed"
        }), 400

    items = data.get("items", [])

    # Step 3 - Validate items field exists
    if not items:
        return jsonify({
            "error": "items field is required",
            "status": "failed"
        }), 400

    # Step 4 - Validate items is a list
    if not isinstance(items, list):
        return jsonify({
            "error": "items must be a list",
            "status": "failed"
        }), 400

    # Step 5 - Validate minimum items
    if len(items) < 1:
        return jsonify({
            "error": "items must have at least 1 item",
            "status": "failed"
        }), 400

    # Step 6 - Validate maximum items
    if len(items) > 20:
        return jsonify({
            "error": "items must not exceed 20 items",
            "status": "failed"
        }), 400

    # Step 7 - Validate each item is a string
    for i, item in enumerate(items):
        if not isinstance(item, str):
            return jsonify({
                "error": f"item at index {i} must be a string",
                "status": "failed"
            }), 400
        if len(item.strip()) < 3:
            return jsonify({
                "error": f"item at index {i} must be at least 3 characters",
                "status": "failed"
            }), 400

    # Step 8 - Process items and return result
    result = batch_process(items)
    return jsonify(result)