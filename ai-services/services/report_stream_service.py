import os
import json
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

client = Groq(api_key=os.getenv("GROQ_API_KEY"))


def load_prompt_template() -> str:
    template_path = os.path.join(
        os.path.dirname(__file__),
        "..",
        "prompts",
        "report_prompt.txt"
    )
    with open(template_path, "r") as f:
        return f.read()


def stream_report(text: str):
    try:
        # Step 1 - Load prompt template
        template = load_prompt_template()

        # Step 2 - Replace {input} with actual user input
        prompt = template.replace("{input}", text)

        # Step 3 - Call Groq API with stream=True
        stream = client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {
                    "role": "system",
                    "content": "You are a SOC 2 compliance expert. Always respond with valid JSON only. No extra text outside JSON."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            temperature=0.3,
            max_tokens=2000,
            stream=True        # ← This enables streaming
        )

        # Step 4 - Yield each token as SSE event
        for chunk in stream:
            token = chunk.choices[0].delta.content
            if token is not None:
                # Send token as SSE event
                data = json.dumps({"token": token, "done": False})
                yield f"data: {data}\n\n"

        # Step 5 - Send done event when finished
        done_data = json.dumps({"token": "", "done": True})
        yield f"data: {done_data}\n\n"

    except Exception as e:
        print(f"Streaming error: {e}")
        error_data = json.dumps({
            "token": "",
            "done": True,
            "error": "Streaming failed",
            "is_fallback": True
        })
        yield f"data: {error_data}\n\n"