#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$PROJECT_DIR/.env"

if [[ ! -f "$ENV_FILE" ]]; then
    echo "No se encontró $ENV_FILE. Crea el archivo a partir de .env.example" >&2
    exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "docker no está instalado o no es accesible en PATH" >&2
    exit 1
fi

set -a
source "$ENV_FILE"
set +a

CONTAINER_NAME=${CONTAINER_NAME:-aponia-postgres}
DUMP_FILE="$PROJECT_DIR/docker/initdb/seed.sql"

if ! docker compose ps --services --filter "status=running" | grep -qx "db"; then
    echo "El servicio 'db' no está en ejecución. Inícialo con 'docker compose up -d db'" >&2
    exit 1
fi

echo "Exportando base de datos desde el contenedor $CONTAINER_NAME..."
docker compose exec db pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" > "$DUMP_FILE"

echo "Dump actualizado en $DUMP_FILE"