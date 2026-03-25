from __future__ import annotations

import os
from pathlib import Path

import pandas as pd
import requests

BASE_DIR = Path(__file__).resolve().parent
DATA_DIR = BASE_DIR / "data"
OUT_PATH = DATA_DIR / "topic_training.csv"


def getenv(name: str, default: str | None = None) -> str:
    v = os.getenv(name, default)
    if v is None:
        raise RuntimeError(f"Env eksik: {name}")
    return v


def login_and_get_token(base_url: str, email: str, password: str) -> str:
    r = requests.post(
        f"{base_url}/api/auth/login",
        json={"email": email, "password": password},
        timeout=15,
    )
    r.raise_for_status()
    data = r.json()
    token = data.get("token")
    if not token:
        raise RuntimeError("Login response icinde token bulunamadi.")
    return token


def fetch_dataset(base_url: str, token: str, days: int, min_answers: int) -> list[dict]:
    r = requests.get(
        f"{base_url}/api/ai/training-dataset",
        params={"days": days, "minAnswers": min_answers},
        headers={"Authorization": f"Bearer {token}"},
        timeout=30,
    )
    r.raise_for_status()
    data = r.json()
    if not isinstance(data, list):
        raise RuntimeError("Dataset response list degil.")
    return data


def main():
    DATA_DIR.mkdir(parents=True, exist_ok=True)

    base_url = getenv("BACKEND_API_URL", "http://localhost:8080")
    email = getenv("ADMIN_EMAIL")
    password = getenv("ADMIN_PASSWORD")
    days = int(getenv("TRAIN_DAYS", "120"))
    min_answers = int(getenv("TRAIN_MIN_ANSWERS", "12"))

    token = login_and_get_token(base_url, email, password)
    rows = fetch_dataset(base_url, token, days, min_answers)
    if not rows:
        raise RuntimeError("Backend dataset bos dondu. Daha fazla veri olusmasini bekle.")

    df = pd.DataFrame(rows)
    keep_cols = ["successRate", "wrongRate", "blankRate", "volume", "riskHigh"]
    for c in keep_cols:
        if c not in df.columns:
            raise RuntimeError(f"Beklenen kolon yok: {c}")

    train_df = df[keep_cols].rename(
        columns={
            "successRate": "success_rate",
            "wrongRate": "wrong_rate",
            "blankRate": "blank_rate",
            "riskHigh": "risk_high",
        }
    )
    train_df.to_csv(OUT_PATH, index=False)
    print(f"Training data yazildi: {OUT_PATH} ({len(train_df)} satir)")


if __name__ == "__main__":
    main()
