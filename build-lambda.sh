#!/usr/bin/env bash
set -euo pipefail

./mvnw -DskipTests package

if [[ ! -f target/function.zip ]]; then
  echo "target/function.zip não foi gerado. O pacote Lambda não está pronto." >&2
  exit 1
fi

echo "Pacote pronto: target/function.zip"
