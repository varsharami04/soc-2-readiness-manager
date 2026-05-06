from flask import Flask
from flask_cors import CORS
from dotenv import load_dotenv
import os

load_dotenv()

from services.chroma_client import ingest_documents
print("Initializing ChromaDB...")
ingest_documents()


def create_app():
    app = Flask(__name__)
    CORS(app)

    from routes.health import health_bp
    from routes.describe import describe_bp
    from routes.recommend import recommend_bp
    from routes.report import report_bp
    from routes.query import query_bp
    from routes.analyse import analyse_bp
    from routes.batch import batch_bp          # ← Added

    app.register_blueprint(health_bp)
    app.register_blueprint(describe_bp,   url_prefix="/api")
    app.register_blueprint(recommend_bp,  url_prefix="/api")
    app.register_blueprint(report_bp,     url_prefix="/api")
    app.register_blueprint(query_bp,      url_prefix="/api")
    app.register_blueprint(analyse_bp,    url_prefix="/api")
    app.register_blueprint(batch_bp,      url_prefix="/api")  # ← Added

    return app


if __name__ == "__main__":
    app = create_app()
    app.run(
        debug=True,
        host="0.0.0.0",
        port=5000
    )