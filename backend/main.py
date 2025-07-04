from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, HttpUrl
from fastapi.responses import RedirectResponse
from fastapi.middleware.cors import CORSMiddleware
import string, random
import logging

## Key Improvements
# In-memory disctionary db mapping short codes to URLs
# Short code generator using random string (avoiding collisions)
# URL validation via Pydantic's HttpUrl
# Proper 404 handling for unknown short codes
# logging

app = FastAPI()
logger = logging.getLogger("uvicorn.error")

# Behold, the database!
db = {"abc123": "https://example.com"}

origins = ["http://localhost:3000"]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class URLRequest(BaseModel):
    url: HttpUrl #validates that it's a proper URL

def generate_short_code(length=6):
    characters = string.ascii_letters + string.digits
    while True:
        code = ''.join(random.choices(characters, k=length))
        if code not in db:
            return code

@app.post("/api/shorten")
def shorten_url(request: URLRequest):
    #return {"short_code": "abc123", "short_url": "http://localhost:5000/abc123", "original_url": "http://example.com" }
    short_code = generate_short_code()
    db[short_code] = request.url
    logger.info(f"Storred mapping: {short_code} -> {request.url}")

    return {
        "short_code": short_code,
        "short_url": "http://localhost:5000/" + short_code,
        "original_url": request.url
    }

@app.get("/{short_code}")
def redirect(short_code: str):
    logger.info(f"Received redirect request for short code: {short_code}")

    if short_code in db:
        logger.info(f"Redirecting to {db[short_code]}")
        return RedirectResponse(url=db[short_code])

    logger.warning(f"Short code {short_code} not found")
    raise HTTPException(status_code=404, detail="Short code not found")
