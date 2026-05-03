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
        "analyse_prompt.txt"
    )
    with open(template_path, "r") as f:
        return f.read()


def analyse_document(text: str) -> dict:
    try:
        # Step 1 - Load prompt template
        template = load_prompt_template()

        # Step 2 - Replace {input} with actual document text
        prompt = template.replace("{input}", text)

        # Step 3 - Call Groq API
        response = client.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {
                    "role": "system",
                    "content": "You are a SOC 2 compliance expert and security auditor. Always respond with valid JSON only. No extra text."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            temperature=0.3,
            max_tokens=2000,
            timeout=30
        )

        # Step 4 - Extract AI response
        result = response.choices[0].message.content.strip()

        # Step 5 - Clean response if needed
        if result.startswith("```"):
            result = result.split("```")[1]
            if result.startswith("json"):
                result = result[4:]

        # Step 6 - Parse JSON response
        analysis = json.loads(result)

        # Step 7 - Return structured response
        return {
            "input_length": len(text),
            "findings": analysis.get("findings", []),
            "summary": analysis.get("summary", ""),
            "total_findings": len(analysis.get("findings", [])),
            "generated_at": datetime.utcnow().isoformat(),
            "status": "success",
            "is_fallback": False
        }


    except json.JSONDecodeError as e:
        print(f"JSON parsing error: {e}")
        print(f"Raw response was: {result}")
        return fallback_response(text)


    except Exception as e:
        print(f"Error analysing document: {e}")
        import traceback
        traceback.print_exc()
        return fallback_response(text)


def fallback_response(text: str) -> dict:
    return {
        "input_length": len(text),
        "findings": [
            {
                "type": "risk",
                "category": "General",
                "finding": "Unable to analyse document at this time.",
                "severity": "medium",
                "recommendation": "Please try again later."
            }
        ],
        "summary": "Document analysis failed. Please try again.",
        "total_findings": 1,
        "generated_at": datetime.utcnow().isoformat(),
        "status": "failed",
        "is_fallback": True
    }