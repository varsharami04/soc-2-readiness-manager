from flask import Blueprint, request, jsonify, Response, stream_with_context
from services.report_service import generate_report
from services.report_stream_service import stream_report

report_bp = Blueprint("report", __name__)


# Original endpoint — returns full JSON response
@report_bp.post("/generate-report")
def create_report():
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

    # Step 6 - Generate report and return
    result = generate_report(text)
    return jsonify(result)


# New SSE streaming endpoint
@report_bp.get("/generate-report/stream")
def stream_report_endpoint():
    # Step 1 - Get text from query parameter
    text = request.args.get("text", "").strip()

    # Step 2 - Validate input
    if not text:
        return jsonify({
            "error": "text parameter is required",
            "status": "failed"
        }), 400

    # Step 3 - Validate minimum length
    if len(text) < 3:
        return jsonify({
            "error": "text must be at least 3 characters",
            "status": "failed"
        }), 400

    # Step 4 - Return SSE streaming response
    return Response(
        stream_with_context(stream_report(text)),
        mimetype="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "X-Accel-Buffering": "no",
            "Access-Control-Allow-Origin": "*"
        }
    )