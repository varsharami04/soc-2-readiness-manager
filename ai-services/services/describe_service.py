import os
from datetime import datetime
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

# Initialize Groq client
client = Groq(api_key=os.getenv("GROQ_API_KEY"))


def load_prompt_template() -> str:
    # Load prompt template from prompts folder
    template_path = os.path.join(
        os.path.dirname(__file__),
        "..",
        "prompts",
        "describe_prompt.txt"
    )
    with open(template_path, "r") as f:
        return f.read()


def generate_description(text: str) -> dict:
    try:
        # Step 1 - Load prompt template
        template = load_prompt_template()

        # Step 2 - Replace {input} with actual user input
        prompt = template.replace("{input}", text)

        # Step 3 - Call Groq API
        response = client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {
                    "role": "system",
                    "content": "You are a SOC 2 compliance expert. Always respond in professional formal language."
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

        # Step 4 - Extract AI response
        description = response.choices[0].message.content

        # Step 5 - Return structured JSON with generated_at timestamp
        return {
            "input": text,
            "description": description,
            "word_count": len(description.split()),
            "generated_at": datetime.utcnow().isoformat(),
            "status": "success",
            "is_fallback": False
        }

    except FileNotFoundError:
        return {
            "input": text,
            "description": "Prompt template not found.",
            "word_count": 0,
            "generated_at": datetime.utcnow().isoformat(),
            "status": "failed",
            "is_fallback": True
        }

    except Exception as e:
        print(f"Error generating description: {e}")
        return {
            "input": text,
            "description": "Unable to generate description at this time.",
            "word_count": 0,
            "generated_at": datetime.utcnow().isoformat(),
            "status": "failed",
            "is_fallback": True
        }