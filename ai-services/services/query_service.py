import os
from datetime import datetime
from groq import Groq
from dotenv import load_dotenv
from services.chroma_client import query_collection

load_dotenv()

client = Groq(api_key=os.getenv("GROQ_API_KEY"))


def generate_answer(question: str) -> dict:
    try:
        # Step 1 - Get relevant chunks from ChromaDB
        chunks = query_collection(question, n_results=3)

        # Step 2 - Build context from chunks
        context = "\n\n".join(chunks)

        # Step 3 - Build prompt with context
        prompt = f"""You are a SOC 2 compliance expert.

Use the following context to answer the question.
If the context does not contain enough information,
use your own knowledge to answer.

Context:
{context}

Question: {question}

Provide a clear and professional answer in 2-3 paragraphs.
"""

        # Step 4 - Call Groq API
        response = client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {
                    "role": "system",
                    "content": "You are a SOC 2 compliance expert. Answer questions clearly and professionally."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            temperature=0.3,
            max_tokens=1000,
            timeout=30
        )

        # Step 5 - Extract answer
        answer = response.choices[0].message.content

        # Step 6 - Return structured response
        return {
            "question": question,
            "answer": answer,
            "sources": chunks,
            "generated_at": datetime.utcnow().isoformat(),
            "status": "success",
            "is_fallback": False
        }

    except Exception as e:
        print(f"Error generating answer: {e}")
        return {
            "question": question,
            "answer": "Unable to generate answer at this time.",
            "sources": [],
            "generated_at": datetime.utcnow().isoformat(),
            "status": "failed",
            "is_fallback": True
        }