from __future__ import annotations

from pathlib import Path
from datetime import datetime
import json
import shutil

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report, roc_auc_score

BASE_DIR = Path(__file__).resolve().parent
DATA_DIR = BASE_DIR / "data"
MODEL_DIR = BASE_DIR / "models"
MODEL_PATH = MODEL_DIR / "topic_risk_model.joblib"
REGISTRY_PATH = MODEL_DIR / "registry.json"


def ensure_dirs():
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    MODEL_DIR.mkdir(parents=True, exist_ok=True)


def build_synthetic_dataset(n: int = 2000) -> pd.DataFrame:
    rng = np.random.default_rng(42)
    total = rng.integers(5, 120, size=n)
    success = rng.uniform(25, 95, size=n)
    wrong_rate = np.clip(100 - success + rng.normal(0, 6, size=n), 1, 90)
    blank_rate = np.clip(rng.normal(12, 7, size=n), 0, 50)
    volume = np.clip(total / 50.0, 0, 1)

    # Pseudo-label (risk_high): zorlayici ama tutarli bir tanim.
    risk_signal = (0.05 * wrong_rate) + (0.07 * blank_rate) - (0.04 * success) + (0.4 * (total < 15))
    prob = 1 / (1 + np.exp(-risk_signal))
    y = (prob > 0.55).astype(int)

    return pd.DataFrame(
        {
            "success_rate": success,
            "wrong_rate": wrong_rate,
            "blank_rate": blank_rate,
            "volume": volume,
            "risk_high": y,
        }
    )


def load_or_generate_data() -> pd.DataFrame:
    csv_path = DATA_DIR / "topic_training.csv"
    if csv_path.exists():
        df = pd.read_csv(csv_path)
        required = {"success_rate", "wrong_rate", "blank_rate", "volume"}
        if required.issubset(df.columns):
            if "risk_high" not in df.columns:
                # Label yoksa basit kuraldan label uretiyoruz.
                df["risk_high"] = ((df["success_rate"] < 62) | (df["blank_rate"] > 18)).astype(int)
            return df
    return build_synthetic_dataset()


def train():
    ensure_dirs()
    df = load_or_generate_data()

    # Gercek veri azsa/sadece tek sinif varsa modeli stabil egitmek icin destek veri ekle.
    unique_classes = set(df["risk_high"].astype(int).unique().tolist())
    if len(unique_classes) < 2 or len(df) < 100:
        support = build_synthetic_dataset(max(1500, 100 - len(df)))
        df = pd.concat([df, support], ignore_index=True)

    X = df[["success_rate", "wrong_rate", "blank_rate", "volume"]].values
    y = df["risk_high"].astype(int).values

    stratify_target = y if len(set(y.tolist())) > 1 else None
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=stratify_target
    )

    model = RandomForestClassifier(
        n_estimators=250,
        max_depth=8,
        min_samples_leaf=4,
        random_state=42,
        class_weight="balanced",
    )
    model.fit(X_train, y_train)

    metrics = {}
    proba = model.predict_proba(X_test)
    if proba.shape[1] > 1:
        probs = proba[:, 1]
        preds = (probs > 0.5).astype(int)
        auc = round(float(roc_auc_score(y_test, probs)), 4)
        metrics["auc"] = auc
        print("AUC:", auc)
        print(classification_report(y_test, preds))
    else:
        preds = model.predict(X_test)
        print("Tek sinifli veri tespit edildi, AUC hesabi atlandi.")
        print(classification_report(y_test, preds))

    version = datetime.utcnow().strftime("v%Y%m%d_%H%M%S")
    version_path = MODEL_DIR / f"topic_risk_model_{version}.joblib"
    joblib.dump(model, version_path)
    shutil.copyfile(version_path, MODEL_PATH)

    registry = {
        "active_version": version,
        "active_model_path": str(MODEL_PATH.name),
        "trained_at_utc": datetime.utcnow().isoformat() + "Z",
        "data_rows": int(len(df)),
        "metrics": metrics,
    }
    REGISTRY_PATH.write_text(json.dumps(registry, ensure_ascii=True, indent=2), encoding="utf-8")

    print(f"Model kaydedildi: {MODEL_PATH}")
    print(f"Versiyonlu model: {version_path.name}")


if __name__ == "__main__":
    train()
