import os
import time
import json
from datetime import datetime
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

client = Groq(api_key=os.getenv("GROQ_API_KEY"))


def load_describe_prompt() -> str:
    template_path = os.path.join(
        os.path.dirname(__file__),
        "..",
        "prompts",
        "describe_prompt.txt"
    )
    with open(template_path, "r") as f:
        return f.read()


def process_single_item(text: str, index: int) -> dict:
    try:
        # Load prompt template
        template = load_describe_prompt()
        prompt = template.replace("{input}", text)

        # Call Groq API
        response = client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {
                    "role": "system",
                    "content": "You are a SOC 2 compliance expert. Respond in professional formal language."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            temperature=0.3,
            max_tokens=500,
            timeout=30
        )

        description = response.choices[0].message.content

        return {
            "index": index,
            "input": text,
            "description": description,
            "word_count": len(description.split()),
            "status": "success",
            "is_fallback": False
        }

    except Exception as e:
        print(f"Error processing item {index}: {e}")
        return {
            "index": index,
            "input": text,
            "description": "Unable to process this item.",
            "word_count": 0,
            "status": "failed",
            "is_fallback": True
        }


def batch_process(items: list) -> dict:
    results = []
    processed = 0
    failed = 0

    for index, item in enumerate(items):
        # Process each item
        result = process_single_item(item, index)
        results.append(result)

        # Count processed and failed
        if result["status"] == "success":
            processed += 1
        else:
            failed += 1

        # 100ms delay between each item
        # Skip delay after last item
        if index < len(items) - 1:
            time.sleep(0.1)

    return {
        "total_items": len(items),
        "processed": processed,
        "failed": failed,
        "results": results,
        "generated_at": datetime.utcnow().isoformat(),
        "status": "success"
    }