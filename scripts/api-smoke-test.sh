#!/usr/bin/env bash
set -euo pipefail

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<'USAGE'
Uso: scripts/api-smoke-test.sh [BASE_URL]

Ejecuta una serie de pruebas de humo contra la API REST y guarda los logs
en un archivo con marca de tiempo dentro de la carpeta logs/.

Argumentos:
  BASE_URL  URL base de la API (por defecto http://localhost:8083)

Variables de entorno:
  LOG_DIR   Carpeta donde se guardarán los logs (por defecto logs)
USAGE
  exit 0
fi

BASE_URL=${1:-http://localhost:8083}
LOG_DIR=${LOG_DIR:-logs}
TIMESTAMP=$(date -u +"%Y%m%dT%H%M%SZ")
LOG_FILE="${LOG_DIR}/api-smoke-${TIMESTAMP}.log"

mkdir -p "$LOG_DIR"

echo "Registrando resultados en: $LOG_FILE"

declare -a TEMP_FILES
cleanup() {
  [[ ${#TEMP_FILES[@]} -gt 0 ]] && rm -f "${TEMP_FILES[@]}"
  echo "Logs guardados en: $LOG_FILE"
}
trap cleanup EXIT

log() {
  local message="$1"
  printf '\n[%s] %s\n' "$(date -u +"%Y-%m-%dT%H:%M:%SZ")" "$message" | tee -a "$LOG_FILE"
}

log_file() {
  local file="$1"
  tee -a "$LOG_FILE" < "$file" >/dev/null
}

request() {
  local method="$1"; shift
  local url="$1"; shift
  local body_file="${1:-}"

  log "${method} ${url}"
  if [[ -n "$body_file" ]]; then
    log "Payload:"
    log_file "$body_file"
  fi

  curl -sS -X "$method" "$url" \
    -H 'Content-Type: application/json' \
    ${body_file:+--data @"$body_file"} \
    -w '\nHTTP_STATUS:%{http_code}\n' | tee -a "$LOG_FILE"
}

mkpayload() {
  local tmp
  tmp=$(mktemp)
  TEMP_FILES+=("$tmp")
  cat >"$tmp"
  echo "$tmp"
}

USER_ID="apitest-$(date +%s)"
USER_EMAIL="${USER_ID}@example.com"

log "Iniciando smoke test contra ${BASE_URL} con usuario ${USER_ID}"

USER_PAYLOAD=$(mkpayload <<JSON
{
  "id": "${USER_ID}",
  "email": "${USER_EMAIL}",
  "password": "Secreto123!",
  "rol": "CLIENTE"
}
JSON
)

request POST "${BASE_URL}/api/usuarios/add" "$USER_PAYLOAD"

request GET "${BASE_URL}/api/usuarios/find/${USER_ID}"

CLIENTE_CREATE=$(mkpayload <<JSON
{
  "usuarioId": "${USER_ID}",
  "nombreCompleto": "Cliente Demo",
  "telefono": "555-0001"
}
JSON
)
request POST "${BASE_URL}/api/usuarios/${USER_ID}/perfil-cliente" "$CLIENTE_CREATE"

CLIENTE_UPDATE=$(mkpayload <<JSON
{
  "usuarioId": "${USER_ID}",
  "nombreCompleto": "Cliente Demo Actualizado",
  "telefono": "555-9999",
  "direccion": "Calle Falsa 123"
}
JSON
)
request POST "${BASE_URL}/api/usuarios/${USER_ID}/perfil-cliente" "$CLIENTE_UPDATE"

EMPLEADO_CREATE=$(mkpayload <<JSON
{
  "usuarioId": "${USER_ID}",
  "nombreCompleto": "Empleado Demo",
  "telefono": "555-0002",
  "cargo": "Recepcionista",
  "salario": 1800.50,
  "fechaContratacion": "2020-01-01"
}
JSON
)
request POST "${BASE_URL}/api/usuarios/${USER_ID}/perfil-empleado" "$EMPLEADO_CREATE"

EMPLEADO_UPDATE=$(mkpayload <<JSON
{
  "usuarioId": "${USER_ID}",
  "nombreCompleto": "Empleado Demo Actualizado",
  "telefono": "555-8888",
  "cargo": "Supervisor",
  "salario": 2500.75,
  "fechaContratacion": "2019-05-20"
}
JSON
)
request POST "${BASE_URL}/api/usuarios/${USER_ID}/perfil-empleado" "$EMPLEADO_UPDATE"

SALARIO_INVALIDO=$(mkpayload <<JSON
{
  "usuarioId": "${USER_ID}",
  "nombreCompleto": "Empleado Demo Actualizado",
  "telefono": "555-8888",
  "cargo": "Supervisor",
  "salario": 56789876567,
  "fechaContratacion": "2019-05-20"
}
JSON
)

log "Probando validación de salario con un valor inválido"
request PUT "${BASE_URL}/api/empleados-perfil/update" "$SALARIO_INVALIDO"

log "Recuperando listado final de usuarios"
request GET "${BASE_URL}/api/usuarios/all"

log "Smoke test finalizado"