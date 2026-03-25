#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ML_DIR="$ROOT_DIR/ai-ml-service"
LOG_DIR="$ROOT_DIR/.run-logs"
PID_DIR="$ROOT_DIR/.run-pids"

BACKEND_PORT="${AI_STACK_BACKEND_PORT:-8085}"
ML_PORT="${AI_STACK_ML_PORT:-8001}"
TRAIN_FIRST="${1:-}"

mkdir -p "$LOG_DIR" "$PID_DIR"

ML_PID_FILE="$PID_DIR/ml.pid"
BACKEND_PID_FILE="$PID_DIR/backend.pid"

is_running() {
  local pid_file="$1"
  if [[ -f "$pid_file" ]]; then
    local pid
    pid="$(cat "$pid_file" 2>/dev/null || true)"
    if [[ -n "${pid}" ]] && kill -0 "$pid" 2>/dev/null; then
      return 0
    fi
  fi
  return 1
}

wait_http_ok() {
  local url="$1"
  local retries="${2:-25}"
  local sleep_s="${3:-1}"
  local i
  for i in $(seq 1 "$retries"); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      return 0
    fi
    sleep "$sleep_s"
  done
  return 1
}

wait_process_and_http_ok() {
  local pid_file="$1"
  local url="$2"
  local retries="${3:-25}"
  local sleep_s="${4:-1}"
  local i
  for i in $(seq 1 "$retries"); do
    if ! is_running "$pid_file"; then
      return 2
    fi
    if curl -fsS "$url" >/dev/null 2>&1; then
      return 0
    fi
    sleep "$sleep_s"
  done
  return 1
}

pid_on_port() {
  local port="$1"
  lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null | awk 'NR==1{print;exit}' || true
}

stop_pid_if_running() {
  local pid_file="$1"
  if [[ -f "$pid_file" ]]; then
    local pid
    pid="$(cat "$pid_file" 2>/dev/null || true)"
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
      sleep 1
    fi
    rm -f "$pid_file"
  fi
}

start_ml() {
  local existing_pid
  existing_pid="$(pid_on_port "$ML_PORT")"
  if [[ -n "$existing_pid" ]]; then
    echo "ML service portu zaten kullaniliyor (pid: $existing_pid), mevcut service kullanilacak."
    echo "$existing_pid" > "$ML_PID_FILE"
    return
  fi

  if is_running "$ML_PID_FILE"; then
    echo "ML service zaten calisiyor (pid: $(cat "$ML_PID_FILE"))."
    return
  fi

  if [[ ! -d "$ML_DIR/.venv" ]]; then
    echo "ML sanal ortam olusturuluyor..."
    python3 -m venv "$ML_DIR/.venv"
  fi

  echo "ML bagimliliklari kontrol ediliyor..."
  source "$ML_DIR/.venv/bin/activate"
  pip install -r "$ML_DIR/requirements.txt" >/dev/null

  echo "ML service baslatiliyor (port $ML_PORT)..."
  nohup "$ML_DIR/.venv/bin/uvicorn" --app-dir "$ML_DIR" app:app --host 0.0.0.0 --port "$ML_PORT" \
    > "$LOG_DIR/ml.log" 2>&1 &
  echo $! > "$ML_PID_FILE"

  if ! wait_process_and_http_ok "$ML_PID_FILE" "http://localhost:${ML_PORT}/health" 20 1; then
    echo "ML service health check basarisiz."
    exit 1
  fi

  existing_pid="$(pid_on_port "$ML_PORT")"
  if [[ -n "$existing_pid" ]]; then
    echo "$existing_pid" > "$ML_PID_FILE"
  fi
}

start_backend() {
  local existing_pid
  existing_pid="$(pid_on_port "$BACKEND_PORT")"
  if [[ -n "$existing_pid" ]]; then
    echo "Backend portu zaten kullaniliyor (pid: $existing_pid), mevcut service kullanilacak."
    echo "$existing_pid" > "$BACKEND_PID_FILE"
    return
  fi

  if is_running "$BACKEND_PID_FILE"; then
    echo "Backend zaten calisiyor (pid: $(cat "$BACKEND_PID_FILE"))."
    return
  fi

  echo "Backend baslatiliyor (port $BACKEND_PORT)..."
  (
    cd "$ROOT_DIR"
    nohup env AI_ML_ENABLED=true AI_ML_BASE_URL="http://localhost:${ML_PORT}" \
      ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port="${BACKEND_PORT}" \
      > "$LOG_DIR/backend.log" 2>&1 &
    echo $! > "$BACKEND_PID_FILE"
  )

  if ! wait_process_and_http_ok "$BACKEND_PID_FILE" "http://localhost:${BACKEND_PORT}/health" 35 1; then
    echo "Backend health check basarisiz."
    echo "Son backend loglari:"
    tail -n 40 "$LOG_DIR/backend.log" || true
    exit 1
  fi

  existing_pid="$(pid_on_port "$BACKEND_PORT")"
  if [[ -n "$existing_pid" ]]; then
    echo "$existing_pid" > "$BACKEND_PID_FILE"
  fi
}

train_model() {
  echo "Model egitimi baslatiliyor..."
  if [[ -n "${ADMIN_EMAIL:-}" && -n "${ADMIN_PASSWORD:-}" ]]; then
    (
      cd "$ML_DIR"
      BACKEND_API_URL="${BACKEND_API_URL:-http://localhost:${BACKEND_PORT}}" \
      TRAIN_DAYS="${TRAIN_DAYS:-120}" \
      TRAIN_MIN_ANSWERS="${TRAIN_MIN_ANSWERS:-12}" \
      bash "./train_from_backend.sh" >/dev/null
    )
  else
    source "$ML_DIR/.venv/bin/activate"
    python "$ML_DIR/train_model.py" >/dev/null
  fi
}

restart_ml() {
  echo "ML service yeniden baslatiliyor (yeni model yuklemek icin)..."
  stop_pid_if_running "$ML_PID_FILE"
  start_ml
}

echo "AI stack baslatiliyor..."
start_ml
start_backend
if [[ "$TRAIN_FIRST" == "--train" ]]; then
  train_model
  restart_ml
fi

echo
echo "Tamamlandi."
echo "ML log:      $LOG_DIR/ml.log"
echo "Backend log: $LOG_DIR/backend.log"
echo "Durum: bash \"$ROOT_DIR/scripts/status_ai_stack.sh\""
