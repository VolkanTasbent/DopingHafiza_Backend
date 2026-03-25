# AI Stack Runbook

Bu dokuman, AI/ML katmanini standart portlarla tek komutla yonetmek icindir.

## Standart Portlar

- Backend: `8085`
- ML service: `8001`

## Scriptler

- Baslat: `bash scripts/start_ai_stack.sh`
- Baslat + egit: `bash scripts/start_ai_stack.sh --train`
- Durum: `bash scripts/status_ai_stack.sh`
- Durdur: `bash scripts/stop_ai_stack.sh`

## Tum Gelistirme Stack'i (AI + Web + Mobile)

- Baslat: `bash scripts/start_dev_all.sh`
- Baslat + egit: `bash scripts/start_dev_all.sh --train`
- Durum: `bash scripts/status_dev_all.sh`
- Durdur: `bash scripts/stop_dev_all.sh`

NPM kisayollari:

- Root (`hafiza-web`): `npm run dev:all`, `npm run dev:all:train`, `npm run dev:all:status`, `npm run dev:all:stop`
- Mobile (`hafiza-mobile`): `npm run dev:all`, `npm run dev:all:train`, `npm run dev:all:status`, `npm run dev:all:stop`

## Gercek veriyle egitim (opsiyonel)

Eger `--train` ile backend verisinden egitim yapmak istiyorsan:

```bash
export ADMIN_EMAIL=sevkivolkantasbent@gmail.com
export ADMIN_PASSWORD=1
export TRAIN_DAYS=120
export TRAIN_MIN_ANSWERS=12
bash scripts/start_ai_stack.sh --train
```

Not:
- Bu mod backend login + `/api/ai/training-dataset` endpointini kullanir.
- Admin bilgileri verilmezse lokal/sentetik veriyle egitim yapar.

## Ortam degiskenleri

Port degistirmek istersen:

```bash
export AI_STACK_BACKEND_PORT=8085
export AI_STACK_ML_PORT=8001
```

## Dogrulama endpointleri

- `GET /api/ai/analyze-weak-topics`
- `GET /api/ai/suggest-study-plan`
- `GET /api/ai/ab-compare`
- `POST /api/ai/chat`

Girisli kullanici (JWT): kayitli calisma programlari (sunucuda, kullanici basina en fazla 20 kayit):

- `GET /api/ai/saved-study-plans` — listele (yeni once)
- `POST /api/ai/saved-study-plans` — kaydet (JSON: `title`, `summary`, `analyzedDays`, `dailyMinutes`, `mode`, `tasks`, `focusTips`, `weakTopicsPreview`)
- `DELETE /api/ai/saved-study-plans/{id}` — sil (204 veya 404)

Veritabani: Flyway `V45__ai_saved_study_plan.sql` ile `ai_saved_study_plan` tablosu olusur. Backend ilk acilista migrasyonu uygular; tablo yoksa backend logunda Flyway satirlarini kontrol et.

Admin:
- `GET /api/ai/training-dataset`
