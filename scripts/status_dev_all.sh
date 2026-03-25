#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SCRIPTS_DIR="$ROOT_DIR/DopingHafiza_Backend/scripts"
PID_DIR="$ROOT_DIR/DopingHafiza_Backend/.run-pids"
LOG_DIR="$ROOT_DIR/DopingHafiza_Backend/.run-logs"

WEB_PORT="${DEV_ALL_WEB_PORT:-5173}"
MOBILE_PORT="${DEV_ALL_MOBILE_PORT:-8081}"

WEB_PID_FILE="$PID_DIR/web.pid"
MOBILE_PID_FILE="$PID_DIR/mobile.pid"

pid_on_port() {
  local port="$1"
  lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null | awk 'NR==1{print;exit}' || true
}

print_state() {
  local name="$1"
  local pid_file="$2"
  local port="$3"
  if [[ -f "$pid_file" ]]; then
    local pid
    pid="$(cat "$pid_file" 2>/dev/null || true)"
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
      echo "$name: RUNNING (pid: $pid)"
      return
    fi
  fi
  local ext_pid
  ext_pid="$(pid_on_port "$port")"
  if [[ -n "$ext_pid" ]]; then
    echo "$name: RUNNING (external pid: $ext_pid)"
  else
    echo "$name: STOPPED"
  fi
}

echo "=== AI Stack ==="
bash "$SCRIPTS_DIR/status_ai_stack.sh"
echo
echo "=== Web/Mobile ==="
print_state "Web" "$WEB_PID_FILE" "$WEB_PORT"
print_state "Mobile Metro" "$MOBILE_PID_FILE" "$MOBILE_PORT"

echo
if curl -fsS "http://localhost:${WEB_PORT}" >/dev/null 2>&1; then
  echo "Web health(${WEB_PORT}): OK"
else
  echo "Web health(${WEB_PORT}): FAIL"
fi

if lsof -nP -iTCP:"$MOBILE_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Mobile Metro port(${MOBILE_PORT}): OK"
else
  echo "Mobile Metro port(${MOBILE_PORT}): FAIL"
fi

echo
echo "Log dosyalari:"
echo "- $LOG_DIR/web.log"
echo "- $LOG_DIR/mobile.log"
