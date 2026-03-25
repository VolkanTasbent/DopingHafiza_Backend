#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SCRIPTS_DIR="$ROOT_DIR/DopingHafiza_Backend/scripts"
LOG_DIR="$ROOT_DIR/DopingHafiza_Backend/.run-logs"
PID_DIR="$ROOT_DIR/DopingHafiza_Backend/.run-pids"
MOBILE_DIR="$ROOT_DIR/hafiza-mobile"

WEB_PORT="${DEV_ALL_WEB_PORT:-5173}"
MOBILE_PORT="${DEV_ALL_MOBILE_PORT:-8081}"
TRAIN_FLAG="${1:-}"

WEB_PID_FILE="$PID_DIR/web.pid"
MOBILE_PID_FILE="$PID_DIR/mobile.pid"

mkdir -p "$LOG_DIR" "$PID_DIR"

is_running_pid() {
  local pid="$1"
  [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null
}

is_running_file() {
  local pid_file="$1"
  if [[ -f "$pid_file" ]]; then
    local pid
    pid="$(cat "$pid_file" 2>/dev/null || true)"
    if is_running_pid "$pid"; then
      return 0
    fi
  fi
  return 1
}

pid_on_port() {
  local port="$1"
  lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null | awk 'NR==1{print;exit}' || true
}

wait_http_ok() {
  local url="$1"
  local retries="${2:-30}"
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

wait_port_open() {
  local port="$1"
  local retries="${2:-30}"
  local sleep_s="${3:-1}"
  local i
  for i in $(seq 1 "$retries"); do
    if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
      return 0
    fi
    sleep "$sleep_s"
  done
  return 1
}

start_web() {
  local existing_pid
  existing_pid="$(pid_on_port "$WEB_PORT")"
  if [[ -n "$existing_pid" ]]; then
    echo "Web portu zaten kullaniliyor (pid: $existing_pid), mevcut process kullanilacak."
    echo "$existing_pid" > "$WEB_PID_FILE"
    return
  fi

  if is_running_file "$WEB_PID_FILE"; then
    echo "Web zaten calisiyor (pid: $(cat "$WEB_PID_FILE"))."
    return
  fi

  echo "Web baslatiliyor (port $WEB_PORT)..."
  (
    cd "$ROOT_DIR"
    nohup npm run dev -- --host 0.0.0.0 --port "$WEB_PORT" > "$LOG_DIR/web.log" 2>&1 &
    echo $! > "$WEB_PID_FILE"
  )

  if ! wait_http_ok "http://localhost:${WEB_PORT}" 35 1; then
    echo "Web health check basarisiz."
    exit 1
  fi

  existing_pid="$(pid_on_port "$WEB_PORT")"
  if [[ -n "$existing_pid" ]]; then
    echo "$existing_pid" > "$WEB_PID_FILE"
  fi
}

start_mobile() {
  local existing_pid
  existing_pid="$(pid_on_port "$MOBILE_PORT")"
  if [[ -n "$existing_pid" ]]; then
    echo "Mobile Metro portu zaten kullaniliyor (pid: $existing_pid), mevcut process kullanilacak."
    echo "$existing_pid" > "$MOBILE_PID_FILE"
    return
  fi

  if is_running_file "$MOBILE_PID_FILE"; then
    echo "Mobile Metro zaten calisiyor (pid: $(cat "$MOBILE_PID_FILE"))."
    return
  fi

  echo "Mobile Metro baslatiliyor (port $MOBILE_PORT)..."
  (
    cd "$MOBILE_DIR"
    nohup env CI=false npx expo start --port "$MOBILE_PORT" -c > "$LOG_DIR/mobile.log" 2>&1 &
    echo $! > "$MOBILE_PID_FILE"
  )

  if ! wait_port_open "$MOBILE_PORT" 40 1; then
    echo "Mobile Metro port check basarisiz."
    exit 1
  fi

  existing_pid="$(pid_on_port "$MOBILE_PORT")"
  if [[ -n "$existing_pid" ]]; then
    echo "$existing_pid" > "$MOBILE_PID_FILE"
  fi
}

echo "Tum stack baslatiliyor (AI + Web + Mobile)..."
bash "$SCRIPTS_DIR/start_ai_stack.sh" "$TRAIN_FLAG"
start_web
start_mobile

echo
echo "Hazir."
echo "Web:    http://localhost:${WEB_PORT}"
echo "Mobile: Metro http://localhost:${MOBILE_PORT}"
echo "Durum:  bash \"$SCRIPTS_DIR/status_dev_all.sh\""
