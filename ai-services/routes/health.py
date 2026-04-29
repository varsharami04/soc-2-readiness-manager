from flask import Blueprint, jsonify
import time

# Start time to calculate uptime
START_TIME = time.time()

health_bp = Blueprint("health", __name__)

@health_bp.get("/health")
def health_check():
    uptime_seconds = int(time.time() - START_TIME)
    return jsonify({
        "status": "ok",
        "uptime_seconds": uptime_seconds,
        "model": "llama-3.3-70b-versatile"
    })