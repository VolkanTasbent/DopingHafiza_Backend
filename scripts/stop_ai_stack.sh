#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_DIR="$ROOT_DIR/.run-pids"

stop_pid_file() {
  local file="$1"
  local name="$2"
  if [[ ! -f "$file" ]]; then
    echo "$name icin pid dosyasi yok."
    return
  fi

  local pid
  pid="$(cat "$file" 2>/dev/null || true)"
  if [[ -z "$pid" ]]; then
    echo "$name pid bos."
    rm -f "$file"
    return
  fi

  if kill -0 "$pid" 2>/dev/null; then
    kill "$pid" 2>/dev/null || true
    echo "$name durduruldu (pid: $pid)."
  else
    echo "$name zaten calismiyor (pid: $pid)."
  fi
  rm -f "$file"
}

stop_pid_file "$PID_DIR/backend.pid" "Backend"
stop_pid_file "$PID_DIR/ml.pid" "ML service"
