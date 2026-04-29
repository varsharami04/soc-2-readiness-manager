from flask import Blueprint, request, jsonify

report_bp = Blueprint("report", __name__)

@report_bp.post("/generate-report")
def generate_report():
    data = request.get_json(force=True)
    text = data.get("text", "")

    if not text:
        return jsonify({"error": "text is required"}), 400

    # Service logic will go here on Day 6
    return jsonify({"result": "generate-report endpoint ready"})