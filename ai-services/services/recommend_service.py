import os
import json
from datetime import datetime
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

client = Groq(api_key=os.getenv("GROQ_API_KEY"))


def load_prompt_template() -> str:
    template_path = os.path.join(
        os.path.dirname(__file__),
        "..",
        "prompts",
        "recommend_prompt.txt"
    )
    with open(template_path, "r") as f:
        return f.read()


def generate_recommendations(text: str) -> dict:
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
                    "content": "You are a SOC 2 compliance expert. Always respond with valid JSON only. No extra text."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            temperature=0.3,
            max_tokens=1000
        )

        # Step 4 - Extract AI response
        result = response.choices[0].message.content.strip()

        # Step 5 - Parse JSON response
        recommendations = json.loads(result)

        # Step 6 - Return structured response
        return {
            "input": text,
            "recommendations": recommendations,
            "total": len(recommendations),
            "generated_at": datetime.utcnow().isoformat(),
            "status": "success",
            "is_fallback": False
        }

    except json.JSONDecodeError as e:
        print(f"JSON parsing error: {e}")
        return fallback_response(text)

    except Exception as e:
        print(f"Error generating recommendations: {e}")
        return fallback_response(text)


def fallback_response(text: str) -> dict:
    return {
        "input": text,
        "recommendations": [
            {
                "action_type": "Policy",
                "description": "Develop and implement a formal policy for this control.",
                "priority": "High"
            },
            {
                "action_type": "Technical",
                "description": "Implement technical controls to enforce this requirement.",
                "priority": "High"
            },
            {
                "action_type": "Training",
                "description": "Conduct training sessions for all relevant staff.",
                "priority": "Medium"
            }
        ],
        "total": 3,
        "generated_at": datetime.utcnow().isoformat(),
        "status": "success",
        "is_fallback": True
    }