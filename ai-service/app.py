from flask import Flask, request, jsonify
import logging
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
from middleware.security_middleware import security_middleware


# LOGGING 
logging.basicConfig(
    filename="security.log",
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)


# CREATE APP
app = Flask(__name__)


# RATE LIMITER 
limiter = Limiter(
    get_remote_address,
    app=app,
    default_limits=["30 per minute"]
)


# MIDDLEWARE 

@app.before_request
def before_request():
    return security_middleware()


# ROUTES


@app.route("/")
def home():
    return "Server is running"

@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})

@app.route("/test", methods=["POST"])
def test():
    data = getattr(request, "cleaned_data", request.get_json())
    return jsonify({
        "message": "Request passed middleware",
        "data": data
    })

@app.route("/generate-report", methods=["POST"])
@limiter.limit("10 per minute")
def generate_report():
    return jsonify({
        "message": "Report generated successfully"
    })

#  SECURITY HEADERS 

@app.after_request
def add_security_headers(response):
    response.headers["Content-Security-Policy"] = "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self';"
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["X-XSS-Protection"] = "1; mode=block"
    response.headers["Server"] = "SecureServer"
    return response


# RUN APP 

if __name__ == "__main__":
    print("Starting Flask server on http://127.0.0.1:5000")
    app.run(host="0.0.0.0", port=5000, debug=False)
  


