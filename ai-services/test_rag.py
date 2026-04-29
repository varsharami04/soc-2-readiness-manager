from services.chroma_client import ingest_documents, query_collection

print("=== Testing RAG Pipeline ===")
print()

# Step 1 - Ingest documents
print("Step 1 - Ingesting documents...")
total = ingest_documents()
print(f"Total chunks stored: {total}")
print()

# Step 2 - Test query
print("Step 2 - Testing query...")
question = "What is access control in SOC 2?"
results = query_collection(question)

print(f"Question: {question}")
print(f"Found {len(results)} relevant chunks:")
print()
for i, chunk in enumerate(results):
    print(f"Chunk {i+1}:")
    print(chunk[:200])
    print("...")
    print()

print("=== RAG Pipeline Test Complete ===")