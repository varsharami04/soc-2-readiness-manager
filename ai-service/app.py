from flask import Flask
from middleware.security_middleware import security_middleware

# Step 1: Create app FIRST
app = Flask(__name__)

# Step 2: Then use it
@app.before_request
def before_request():
    return security_middleware()

# Sample route
@app.route("/health", methods=["GET"])
def health():
    return {"status": "ok"}

# Run app
if __name__ == "__main__":
    app.run(debug=True)

from flask import request

@app.route("/test", methods=["POST"])
def test():
    data = request.get_json()
    return {
        "message": "Request passed middleware",
        "data": data
    }

