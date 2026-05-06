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
        "report_prompt.txt"
    )
    with open(template_path, "r") as f:
        return f.read()


def generate_report(text: str) -> dict:
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
                    "content": "You are a SOC 2 compliance expert. Always respond with valid JSON only. No extra text outside JSON."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            temperature=0.3,
            max_tokens=2000
        )

        # Step 4 - Extract AI response
        result = response.choices[0].message.content.strip()

        # Step 5 - Clean response if needed
        if result.startswith("```"):
            result = result.split("```")[1]
            if result.startswith("json"):
                result = result[4:]

        # Step 6 - Parse JSON response
        report = json.loads(result)

        # Step 7 - Return structured response
        return {
            "input": text,
            "report": report,
            "generated_at": datetime.utcnow().isoformat(),
            "status": "success",
            "is_fallback": False
        }

    except json.JSONDecodeError as e:
        print(f"JSON parsing error: {e}")
        return fallback_response(text)

    except Exception as e:
        print(f"Error generating report: {e}")
        return fallback_response(text)


def fallback_response(text: str) -> dict:
    return {
        "input": text,
        "report": {
            "title": f"SOC 2 Readiness Report — {text.title()}",
            "executive_summary": "This report provides an overview of SOC 2 compliance requirements for the specified control. Organizations must implement appropriate controls to meet SOC 2 standards.",
            "overview": "SOC 2 compliance requires organizations to implement and maintain appropriate security controls. This report outlines key requirements and recommendations for achieving compliance.",
            "top_items": [
                "Implement formal policies and procedures",
                "Deploy technical controls and monitoring",
                "Conduct regular reviews and audits"
            ],
            "recommendations": [
                {
                    "action": "Develop and document formal policies",
                    "priority": "High"
                },
                {
                    "action": "Implement technical controls",
                    "priority": "High"
                },
                {
                    "action": "Schedule regular compliance reviews",
                    "priority": "Medium"
                }
            ]
        },
        "generated_at": datetime.utcnow().isoformat(),
        "status": "success",
        "is_fallback": True
    }