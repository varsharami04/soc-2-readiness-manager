from flask import Flask, request, jsonify
import logging
from flask_talisman import Talisman
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from middleware.security_middleware import security_middleware

# LOGGING
logging.basicConfig(
    filename="security.log",
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)

app = Flask(__name__)

# SECURITY (Talisman)
#Talisman(app, force_https=False)

# RATE LIMITER
limiter = Limiter(get_remote_address, app=app, default_limits=["30 per minute"])

# MIDDLEWARE
@app.before_request
def before_request():
    try:
        return security_middleware()
    except Exception as e:
        logging.error(f"Middleware error: {e}")
        return jsonify({"error": "Internal server error"}), 500

# ROUTES
@app.route("/")
def home():
    return "Server is running"

@app.route("/health")
def health():
    return jsonify({"status": "ok"})

@app.route("/test", methods=["POST"])
def test():
    data = getattr(request, "cleaned_data", request.get_json())
    return jsonify({"message": "Request passed middleware", "data": data})

@app.route("/generate-report", methods=["POST"])
@limiter.limit("10 per minute")
def generate_report():
    return jsonify({"message": "Report generated successfully"})

# RUN
if __name__ == "__main__":
    print("Starting Flask server on http://127.0.0.1:5000")
    print("App initialized successfully")
    app.run(host="127.0.0.1", port=5000, debug=True, use_reloader=False)