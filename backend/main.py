from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from fastapi.responses import RedirectResponse
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

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
    url: str

@app.post("/api/shorten")
def shorten_url(request: URLRequest):
    return {"short_code": "abc123", "short_url": "http://localhost:5000/abc123", "original_url": "http://example.com" }

@app.get("/{short_code}")
def redirect(short_code: str):
    if short_code in db:
        return RedirectResponse(url=db[short_code])
    raise HTTPException(status_code=404, detail="Short code not found")
