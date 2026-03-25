#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SCRIPTS_DIR="$ROOT_DIR/DopingHafiza_Backend/scripts"
PID_DIR="$ROOT_DIR/DopingHafiza_Backend/.run-pids"

WEB_PID_FILE="$PID_DIR/web.pid"
MOBILE_PID_FILE="$PID_DIR/mobile.pid"

stop_pid_file() {
  local file="$1"
  local name="$2"
  if [[ ! -f "$file" ]]; then
    echo "$name icin pid dosyasi yok."
    return
  fi

  local pid
  pid="$(cat "$file" 2>/dev/null || true)"
  if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
    kill "$pid" 2>/dev/null || true
    echo "$name durduruldu (pid: $pid)."
  else
    echo "$name zaten calismiyor (pid: $pid)."
  fi
  rm -f "$file"
}

echo "Web ve Mobile durduruluyor..."
stop_pid_file "$WEB_PID_FILE" "Web"
stop_pid_file "$MOBILE_PID_FILE" "Mobile Metro"

echo
echo "AI stack durduruluyor..."
bash "$SCRIPTS_DIR/stop_ai_stack.sh"
