from flask import Blueprint, request, jsonify

query_bp = Blueprint("query", __name__)

@query_bp.post("/query")
def query():
    data = request.get_json(force=True)
    question = data.get("question", "")

    if not question:
        return jsonify({"error": "question is required"}), 400

    # RAG pipeline will go here on Day 5
    return jsonify({"result": "query endpoint ready"})