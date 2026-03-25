from __future__ import annotations

from pathlib import Path
from typing import List, Literal, Optional

import joblib
import numpy as np
import json
from fastapi import FastAPI
from pydantic import BaseModel, Field

MODEL_PATH = Path(__file__).resolve().parent / "models" / "topic_risk_model.joblib"
REGISTRY_PATH = Path(__file__).resolve().parent / "models" / "registry.json"
_model = None


class TopicInput(BaseModel):
    key: str
    dersId: Optional[int] = None
    konuId: Optional[int] = None
    totalCount: int = Field(ge=1)
    correctCount: int = Field(ge=0)
    wrongCount: int = Field(ge=0)
    blankCount: int = Field(ge=0)
    successRate: float = Field(ge=0, le=100)


class PredictRequest(BaseModel):
    topics: List[TopicInput]


class Prediction(BaseModel):
    key: str
    riskScore: float
    recommendation: str
    source: Literal["ml", "heuristic"]
    modelVersion: Optional[str] = None


class PredictResponse(BaseModel):
    predictions: List[Prediction]
    modelVersion: Optional[str] = None


class StudyPlanRequest(BaseModel):
    topics: List[TopicInput]
    dailyMinutes: int = Field(default=120, ge=30, le=360)
    mode: Literal["mixed", "video", "test"] = "mixed"


class StudyTask(BaseModel):
    taskType: Literal["video", "quiz", "review"]
    title: str
    description: str
    estimatedMinutes: int
    priority: int


class StudyPlanResponse(BaseModel):
    tasks: List[StudyTask]
    summary: str


app = FastAPI(title="Hafiza AI ML Service", version="1.0.0")


def _load_model():
    global _model
    if _model is None and MODEL_PATH.exists():
        _model = joblib.load(MODEL_PATH)
    return _model


def _model_version() -> Optional[str]:
    if not REGISTRY_PATH.exists():
        return None
    try:
        data = json.loads(REGISTRY_PATH.read_text(encoding="utf-8"))
        return data.get("active_version")
    except Exception:
        return None


def _features(topic: TopicInput) -> np.ndarray:
    total = max(1, topic.totalCount)
    success_rate = topic.successRate
    wrong_rate = (topic.wrongCount * 100.0) / total
    blank_rate = (topic.blankCount * 100.0) / total
    volume = min(1.0, total / 50.0)
    return np.array([success_rate, wrong_rate, blank_rate, volume], dtype=float)


def _heuristic_risk(topic: TopicInput) -> float:
    total = max(1, topic.totalCount)
    wrong_rate = (topic.wrongCount * 100.0) / total
    blank_rate = (topic.blankCount * 100.0) / total
    score = (0.06 * wrong_rate) + (0.08 * blank_rate) + (0.6 if total < 15 else 0.0) - (0.05 * topic.successRate)
    risk = 100.0 / (1.0 + np.exp(-score))
    return round(float(risk), 2)


def _recommendation(risk: float, topic: TopicInput) -> str:
    total = max(1, topic.totalCount)
    blank_rate = (topic.blankCount * 100.0) / total
    if risk >= 75:
        return "Yuksek risk: once konu anlatimi, sonra kisa test setleri."
    if blank_rate >= 20:
        return "Bos birakma oranini dusurmek icin sure odakli soru cozumleri yap."
    if topic.successRate < 60:
        return "Basariyi arttirmak icin yanlis sorularin tekrarini yap."
    return "Seviyeyi koru: gunluk mini tekrar + 15 soru hedefi."


@app.get("/health")
def health():
    model = _load_model()
    return {"ok": True, "modelLoaded": model is not None, "modelVersion": _model_version()}


@app.post("/predict-weak-topics", response_model=PredictResponse)
def predict_weak_topics(req: PredictRequest):
    model = _load_model()
    model_version = _model_version()
    predictions: List[Prediction] = []

    if model is not None and req.topics:
        X = np.vstack([_features(t) for t in req.topics])
        # Model "risk_high" olasiligi doner (1 sinifi).
        proba = model.predict_proba(X)[:, 1]
        for topic, p in zip(req.topics, proba):
            risk = round(float(p * 100.0), 2)
            predictions.append(
                Prediction(
                    key=topic.key,
                    riskScore=risk,
                    recommendation=_recommendation(risk, topic),
                    source="ml",
                    modelVersion=model_version,
                )
            )
        return PredictResponse(predictions=predictions, modelVersion=model_version)

    for topic in req.topics:
        risk = _heuristic_risk(topic)
        predictions.append(
            Prediction(
                key=topic.key,
                riskScore=risk,
                recommendation=_recommendation(risk, topic),
                source="heuristic",
                modelVersion=model_version,
            )
        )
    return PredictResponse(predictions=predictions, modelVersion=model_version)


@app.post("/suggest-study-plan", response_model=StudyPlanResponse)
def suggest_study_plan(req: StudyPlanRequest):
    pred = predict_weak_topics(PredictRequest(topics=req.topics))
    by_key = {p.key: p for p in pred.predictions}
    sorted_topics = sorted(req.topics, key=lambda t: by_key[t.key].riskScore, reverse=True)

    tasks: List[StudyTask] = []
    remaining = req.dailyMinutes
    priority = 1

    for topic in sorted_topics[:6]:
        if remaining <= 0:
            break
        if req.mode != "test":
            m = min(max(12, req.dailyMinutes // 6), remaining)
            tasks.append(
                StudyTask(
                    taskType="video",
                    title=f"{topic.key} konu videosu",
                    description="Kisa tekrar videosu izle ve ana notlari cikar.",
                    estimatedMinutes=m,
                    priority=priority,
                )
            )
            priority += 1
            remaining -= m

        if remaining <= 0:
            break
        if req.mode != "video":
            m = min(max(15, req.dailyMinutes // 5), remaining)
            tasks.append(
                StudyTask(
                    taskType="quiz",
                    title=f"{topic.key} test seti",
                    description="Bu konu icin 20-30 soru coz, yanlislarini isaretle.",
                    estimatedMinutes=m,
                    priority=priority,
                )
            )
            priority += 1
            remaining -= m

    if remaining > 0:
        tasks.append(
            StudyTask(
                taskType="review",
                title="Gun sonu tekrar",
                description="Bugun zorlandigin sorulari tekrar cozmeyi dene.",
                estimatedMinutes=remaining,
                priority=priority,
            )
        )

    summary = f"{req.dailyMinutes} dakikalik {req.mode} plan olusturuldu."
    return StudyPlanResponse(tasks=tasks, summary=summary)
