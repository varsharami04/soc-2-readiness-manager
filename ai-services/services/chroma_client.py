import os
import chromadb
from chromadb.utils import embedding_functions

# Path to store ChromaDB data
CHROMA_PATH = os.path.join(
    os.path.dirname(__file__),
    "..",
    "chroma_data"
)

# Collection name
COLLECTION_NAME = "soc2_knowledge"

# Initialize sentence-transformers embedding function
embedding_function = embedding_functions.SentenceTransformerEmbeddingFunction(
    model_name="all-MiniLM-L6-v2"
)


def get_chroma_client():
    client = chromadb.PersistentClient(path=CHROMA_PATH)
    return client


def get_collection():
    client = get_chroma_client()
    collection = client.get_or_create_collection(
        name=COLLECTION_NAME,
        embedding_function=embedding_function
    )
    return collection


def chunk_text(text: str, chunk_size: int = 500, overlap: int = 50) -> list:
    chunks = []
    start = 0
    while start < len(text):
        end = start + chunk_size
        chunk = text[start:end]
        if chunk.strip():
            chunks.append(chunk)
        start = end - overlap
    return chunks


def load_documents() -> list:
    docs_path = os.path.join(
        os.path.dirname(__file__),
        "..",
        "docs"
    )
    documents = []
    for filename in os.listdir(docs_path):
        if filename.endswith(".txt"):
            filepath = os.path.join(docs_path, filename)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
                documents.append({
                    "filename": filename,
                    "content": content
                })
    return documents


def ingest_documents():
    collection = get_collection()

    # Check if already ingested
    existing = collection.count()
    if existing > 0:
        print(f"ChromaDB already has {existing} chunks. Skipping ingestion.")
        return existing

    # Load documents
    documents = load_documents()
    print(f"Loaded {len(documents)} documents")

    all_chunks = []
    all_ids = []
    all_metadata = []

    for doc in documents:
        chunks = chunk_text(doc["content"])
        print(f"Document {doc['filename']}: {len(chunks)} chunks")

        for i, chunk in enumerate(chunks):
            chunk_id = f"{doc['filename']}_chunk_{i}"
            all_chunks.append(chunk)
            all_ids.append(chunk_id)
            all_metadata.append({
                "filename": doc["filename"],
                "chunk_index": i
            })

    # Store in ChromaDB
    collection.add(
        documents=all_chunks,
        ids=all_ids,
        metadatas=all_metadata
    )

    print(f"Successfully stored {len(all_chunks)} chunks in ChromaDB")
    return len(all_chunks)


def query_collection(question: str, n_results: int = 3) -> list:
    collection = get_collection()
    results = collection.query(
        query_texts=[question],
        n_results=n_results
    )
    return results["documents"][0]