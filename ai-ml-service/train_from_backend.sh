#!/usr/bin/env bash
set -euo pipefail

if [ ! -d ".venv" ]; then
  python3 -m venv .venv
fi

source .venv/bin/activate
pip install -r requirements.txt

python fetch_training_data.py
python train_model.py

echo "Tamam: backend verisinden model egitildi."
