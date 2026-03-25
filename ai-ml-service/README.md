# AI ML Service (FastAPI)

Bu servis, konu bazli risk tahmini ve planlama icin Spring backend'e ML endpointleri sunar.

## 1) Kurulum

```bash
cd ai-ml-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## 2) Modeli egit

```bash
python train_model.py
```

Bu komut `models/topic_risk_model.joblib` dosyasini olusturur.

Istersen kendi verini `data/topic_training.csv` dosyasina koyabilirsin.

Beklenen kolonlar:
- `success_rate`
- `wrong_rate`
- `blank_rate`
- `volume` (0-1 arasi)
- `risk_high` (opsiyonel; yoksa otomatik turetilir)

## 2.1) Gercek backend verisinden dataset al ve egit

Bu adim icin admin kullanici lazim.

```bash
export BACKEND_API_URL=http://localhost:8080
export ADMIN_EMAIL=sevkivolkantasbent@gmail.com
export ADMIN_PASSWORD=1
export TRAIN_DAYS=120
export TRAIN_MIN_ANSWERS=12

bash train_from_backend.sh
```

Bu script:
1. `/api/auth/login` ile token alir
2. `/api/ai/training-dataset` endpointinden dataset ceker
3. `data/topic_training.csv` dosyasina yazar
4. modeli egitip `models/topic_risk_model.joblib` dosyasini uretir
5. versiyon kaydini `models/registry.json` dosyasina yazar

## 3) Servisi calistir

```bash
uvicorn app:app --host 0.0.0.0 --port 8001 --reload
```

## 4) Spring backend baglantisi

`application.yml` icinde:

```yaml
ai:
  ml:
    enabled: true
    base-url: http://localhost:8001
```

Ortam degiskenleriyle:
- `AI_ML_ENABLED=true`
- `AI_ML_BASE_URL=http://localhost:8001`

ML servis kapaliysa Spring tarafi otomatik heuristic fallback kullanir.

## 5) Tek komut stack yonetimi

Backend root altindaki scriptleri kullan:

```bash
bash scripts/start_ai_stack.sh
bash scripts/start_ai_stack.sh --train
bash scripts/status_ai_stack.sh
bash scripts/stop_ai_stack.sh
```

Detayli runbook: `AI_STACK_RUNBOOK.md`
