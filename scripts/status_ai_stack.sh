#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/.run-logs"
PID_DIR="$ROOT_DIR/.run-pids"
BACKEND_PORT="${AI_STACK_BACKEND_PORT:-8085}"
ML_PORT="${AI_STACK_ML_PORT:-8001}"

pid_on_port() {
  local port="$1"
  lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null | awk 'NR==1{print;exit}' || true
}

print_status() {
  local file="$1"
  local name="$2"
  local port="$3"
  local pid_var="$4"
  local status_var="$5"
  if [[ -f "$file" ]]; then
    local pid
    pid="$(cat "$file" 2>/dev/null || true)"
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
      echo "$name: RUNNING (pid: $pid)"
      printf -v "$pid_var" "%s" "$pid"
      printf -v "$status_var" "running"
      return
    fi
  fi
  local external_pid
  external_pid="$(pid_on_port "$port")"
  if [[ -n "$external_pid" ]]; then
    echo "$name: RUNNING (external pid: $external_pid)"
    printf -v "$pid_var" "%s" "$external_pid"
    printf -v "$status_var" "running_external"
    return
  fi
  echo "$name: STOPPED"
  printf -v "$pid_var" ""
  printf -v "$status_var" "stopped"
}

backend_state=""
ml_state=""
backend_pid=""
ml_pid=""
print_status "$PID_DIR/backend.pid" "Backend" "$BACKEND_PORT" backend_pid backend_state
print_status "$PID_DIR/ml.pid" "ML service" "$ML_PORT" ml_pid ml_state

echo
if curl -fsS "http://localhost:${BACKEND_PORT}/health" >/dev/null 2>&1; then
  echo "Backend health(${BACKEND_PORT}): OK"
  if [[ "$backend_state" == "stopped" ]]; then
    echo "Not: Backend script tarafindan degil, harici bir process ile calisiyor olabilir."
  fi
else
  echo "Backend health(${BACKEND_PORT}): FAIL"
fi

if curl -fsS "http://localhost:${ML_PORT}/health" >/dev/null 2>&1; then
  echo "ML health(${ML_PORT}): OK"
  if [[ "$ml_state" == "stopped" ]]; then
    echo "Not: ML service script tarafindan degil, harici bir process ile calisiyor olabilir."
  fi
else
  echo "ML health(${ML_PORT}): FAIL"
fi

echo
echo "Log dosyalari:"
echo "- $LOG_DIR/backend.log"
echo "- $LOG_DIR/ml.log"
