#!/usr/bin/env bash
#
# Complete smoke test suite for the AponiaBackend REST API.
#
# This script exercises every public REST endpoint defined in the
# Spring Boot controllers.  It performs basic CRUD operations on
# entities such as usuarios, perfiles, habitaciones, tipos de
# habitaciones, reservas, estancias, servicios, disponibilidades,
# reservas de servicios, pagos y resúmenes de pago.  Each request
# is logged along with its payload and HTTP status so that any
# unexpected failures can be diagnosed by inspecting the log file.
#
# Usage: ./api-smoke-test-updated.sh [BASE_URL]
#
# BASE_URL defaults to http://localhost:8083.  You may override it
# when invoking the script, e.g.:
#   ./api-smoke-test-updated.sh http://127.0.0.1:8083

set -euo pipefail

# Print usage when requested
if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    cat <<'USAGE'
Uso: api-smoke-test-updated.sh [BASE_URL]

Ejecuta una batería de pruebas de humo contra la API REST y
registra todos los detalles en un archivo con marca de tiempo
dentro de la carpeta logs/.

Argumentos:
  BASE_URL      URL base de la API (por defecto http://localhost:8083)

Variables de entorno:
  LOG_DIR       Carpeta donde se guardarán los logs (por defecto logs)

USAGE
    exit 0
fi

# Base URL of the API; default to local Spring Boot port
BASE_URL=${1:-http://localhost:8083}

# Directory and filename for logs
LOG_DIR=${LOG_DIR:-logs}
TIMESTAMP=$(date -u +"%Y%m%dT%H%M%SZ")
LOG_FILE="${LOG_DIR}/api-smoke-${TIMESTAMP}.log"

mkdir -p "$LOG_DIR"
echo "Registrando resultados en: $LOG_FILE"

# Array to track temporary payload files for cleanup
declare -a TEMP_FILES=()

# Cleanup function: remove temp files and announce where logs live
cleanup() {
    [[ ${#TEMP_FILES[@]} -gt 0 ]] && rm -f "${TEMP_FILES[@]}"
    echo "Logs guardados en: $LOG_FILE"
}
trap cleanup EXIT

# Log helper: timestamp messages and append to log file
log() {
    local message="$1"
    printf '\n[%s] %s\n' "$(date -u +"%Y-%m-%dT%H:%M:%SZ")" "$message" | tee -a "$LOG_FILE"
}

# Log the contents of a file into the log file
log_file() {
    local file="$1"
    tee -a "$LOG_FILE" < "$file" >/dev/null
}

# Make an HTTP request with curl.  The first argument is the
# method (GET, POST, PUT, DELETE), the second the URL and the
# optional third argument is a file containing the JSON payload.
request() {
    local method="$1"
    shift
    local url="$1"
    shift
    local body_file="${1:-}"

    log "$method $url"
    if [[ -n "$body_file" ]]; then
        log "Payload:"
        log_file "$body_file"
    fi

    # Perform the request; write status code on its own line for clarity
    curl -sS -X "$method" "$url" \
        -H 'Content-Type: application/json' \
        ${body_file:+--data "@$body_file"} \
        -w '\nHTTP_STATUS:%{http_code}\n' | tee -a "$LOG_FILE"
}

# Create a temporary file containing the heredoc payload; returns its path
mkpayload() {
    local tmp
    tmp=$(mktemp)
    TEMP_FILES+=("$tmp")
    cat >"$tmp"
    echo "$tmp"
}

# Generate a unique suffix for this test run
RUN_ID="apitest-$(date +%s)"

### 1. Usuarios y Perfiles ###

USER_ID="${RUN_ID}-user"
USER_EMAIL="${USER_ID}@example.com"

# Crear usuario inicial (rol CLIENTE por defecto)
USER_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$USER_ID",
  "email": "$USER_EMAIL",
  "password": "password123",
  "rol": "CLIENTE"
}
EOF
)
request POST "$BASE_URL/api/usuarios/add" "$USER_PAYLOAD"

# Obtener usuario por id y por email
request GET "$BASE_URL/api/usuarios/find/$USER_ID"
request GET "$BASE_URL/api/usuarios/email/$USER_EMAIL"

# Listar todos los usuarios
request GET "$BASE_URL/api/usuarios/all"

# Actualizar usuario (solo cambia el email)
USER_UPDATE_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$USER_ID",
  "email": "updated-${USER_EMAIL}",
  "passwordHash": "password123",
  "rol": "CLIENTE"
}
EOF
)
request PUT "$BASE_URL/api/usuarios/update" "$USER_UPDATE_PAYLOAD"

# Crear/actualizar perfil de cliente asociado al usuario
CLIENTE_PROFILE_PAYLOAD=$(mkpayload <<EOF
{
  "nombreCompleto": "Juan de Prueba",
  "telefono": "3001234567"
}
EOF
)
request POST "$BASE_URL/api/usuarios/$USER_ID/perfil-cliente" "$CLIENTE_PROFILE_PAYLOAD"

# Crear/actualizar perfil de empleado asociado al usuario (con salario de prueba)
EMP_PROFILE_PAYLOAD=$(mkpayload <<EOF
{
  "usuarioId": "$USER_ID",
  "nombreCompleto": "Empleado de Prueba",
  "telefono": "3009876543",
  "cargo": "Recepcionista",
  "salario": 2000000,
  "fechaContratacion": "2024-01-01"
}
EOF
)
request POST "$BASE_URL/api/usuarios/$USER_ID/perfil-empleado" "$EMP_PROFILE_PAYLOAD"

# Listar perfiles de cliente y empleado de forma independiente
request GET "$BASE_URL/api/clientes-perfil/all"
request GET "$BASE_URL/api/clientes-perfil/find/$USER_ID"
request GET "$BASE_URL/api/empleados-perfil/all"
request GET "$BASE_URL/api/empleados-perfil/find/$USER_ID"

### 2. Tipos de Habitación y Habitaciones ###

TIPO_ID="${RUN_ID}-tipo"

# Crear tipo de habitación
TIPO_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$TIPO_ID",
  "nombre": "Tipo Prueba",
  "descripcion": "Tipo para pruebas de API",
  "aforoMaximo": 2,
  "precioPorNoche": 150000,
  "activa": true
}
EOF
)
request POST "$BASE_URL/api/habitaciones-tipos/add" "$TIPO_PAYLOAD"

# Obtener tipo de habitación y listar todos
request GET "$BASE_URL/api/habitaciones-tipos/find/$TIPO_ID"
request GET "$BASE_URL/api/habitaciones-tipos/all"
request GET "$BASE_URL/api/habitaciones-tipos/activos"

# Actualizar tipo de habitación
TIPO_UPDATE_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$TIPO_ID",
  "nombre": "Tipo Prueba Actualizado",
  "descripcion": "Descripción actualizada",
  "aforoMaximo": 3,
  "precioPorNoche": 170000,
  "activa": true
}
EOF
)
request PUT "$BASE_URL/api/habitaciones-tipos/update" "$TIPO_UPDATE_PAYLOAD"

# Crear habitación asociada al tipo
HAB_ID="${RUN_ID}-habitacion"
HAB_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$HAB_ID",
  "numero": "999",
  "activa": true
}
EOF
)
request POST "$BASE_URL/api/habitaciones/add?tipoId=$TIPO_ID" "$HAB_PAYLOAD"

# Listar habitaciones y filtrar por tipo
request GET "$BASE_URL/api/habitaciones/all"
request GET "$BASE_URL/api/habitaciones/activos"
request GET "$BASE_URL/api/habitaciones/tipo/$TIPO_ID"
request GET "$BASE_URL/api/habitaciones/find/$HAB_ID"

# Actualizar habitación (cambiar número y estado) utilizando query param para tipo
HAB_UPDATE_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$HAB_ID",
  "numero": "1000",
  "activa": false
}
EOF
)
request PUT "$BASE_URL/api/habitaciones/update?tipoId=$TIPO_ID" "$HAB_UPDATE_PAYLOAD"

### 3. Reservas ###

RESERVA_ID="${RUN_ID}-reserva"
RESERVA_CODIGO="${RUN_ID}-codigo"
RESERVA_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$RESERVA_ID",
  "codigo": "$RESERVA_CODIGO",
  "cliente": { "id": "$USER_ID" },
  "notas": "Reserva de prueba para smoke test"
}
EOF
)
request POST "$BASE_URL/api/reservas/add" "$RESERVA_PAYLOAD"

# Listar reservas
request GET "$BASE_URL/api/reservas/all"
# Listar por cliente y por estado (utiliza estado PENDIENTE por defecto)
request GET "$BASE_URL/api/reservas/cliente/$USER_ID"
request GET "$BASE_URL/api/reservas/estado/PENDIENTE"
request GET "$BASE_URL/api/reservas/activas/$USER_ID"

# Obtener reserva por código y por ID
request GET "$BASE_URL/api/reservas/codigo/$RESERVA_CODIGO"
request GET "$BASE_URL/api/reservas/find/$RESERVA_ID"

# Verificar disponibilidad de tipo de habitación en un rango de fechas
request GET "$BASE_URL/api/reservas/disponible?tipoHabitacionId=$TIPO_ID&entrada=2025-10-01&salida=2025-10-03&numeroHuespedes=2"

# Calcular total de la reserva
request GET "$BASE_URL/api/reservas/$RESERVA_ID/total"

# Actualizar reserva (modificar notas)
RESERVA_UPDATE_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$RESERVA_ID",
  "codigo": "$RESERVA_CODIGO",
  "cliente": { "id": "$USER_ID" },
  "notas": "Notas actualizadas en smoke test",
  "estado": "PENDIENTE"
}
EOF
)
request PUT "$BASE_URL/api/reservas/update" "$RESERVA_UPDATE_PAYLOAD"

# Confirmar, completar y cancelar reserva (en orden) – cada acción se registra
request POST "$BASE_URL/api/reservas/$RESERVA_ID/confirmar"
request POST "$BASE_URL/api/reservas/$RESERVA_ID/completar"
request POST "$BASE_URL/api/reservas/$RESERVA_ID/cancelar"

### 4. Estancias ###

EST_ID="${RUN_ID}-estancia"
EST_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$EST_ID",
  "checkIn": false,
  "checkOut": false,
  "entrada": "2025-10-01",
  "salida": "2025-10-03",
  "numeroHuespedes": 2,
  "precioPorNoche": 150000,
  "totalEstadia": 300000,
  "reserva": { "id": "$RESERVA_ID" }
}
EOF
)

# Crear estancia asociada a reserva y tipo de habitación mediante query param
request POST "$BASE_URL/api/estancias/add?tipoHabitacionId=$TIPO_ID" "$EST_PAYLOAD"

# Listar estancias por distintos criterios
request GET "$BASE_URL/api/estancias/all"
request GET "$BASE_URL/api/estancias/reserva/$RESERVA_ID"
request GET "$BASE_URL/api/estancias/checkins?tipoHabitacionId=$TIPO_ID&fecha=2025-10-01"
request GET "$BASE_URL/api/estancias/checkouts?tipoHabitacionId=$TIPO_ID&fecha=2025-10-03"
request GET "$BASE_URL/api/estancias/disponible?tipoHabitacionId=$TIPO_ID&checkIn=2025-10-01&checkOut=2025-10-03&numeroHuespedes=2"
request GET "$BASE_URL/api/estancias/ocupadas/contar?tipoHabitacionId=$TIPO_ID&checkIn=2025-10-01&checkOut=2025-10-03"
request GET "$BASE_URL/api/estancias/conflictos?habitacionId=$HAB_ID&checkIn=2025-10-01&checkOut=2025-10-03"
request GET "$BASE_URL/api/estancias/find/$EST_ID"

# Actualizar estancia (modificar numero de huéspedes) – tipo se mantiene vía query param
EST_UPDATE_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$EST_ID",
  "checkIn": false,
  "checkOut": false,
  "entrada": "2025-10-01",
  "salida": "2025-10-03",
  "numeroHuespedes": 3,
  "precioPorNoche": 150000,
  "totalEstadia": 450000,
  "reserva": { "id": "$RESERVA_ID" }
}
EOF
)
request PUT "$BASE_URL/api/estancias/update?tipoHabitacionId=$TIPO_ID" "$EST_UPDATE_PAYLOAD"

# Asignar habitación disponible a la estancia
request POST "$BASE_URL/api/estancias/$EST_ID/asignar-habitacion"

### 5. Servicios y Disponibilidades ###

SERV_ID="${RUN_ID}-servicio"
SERV_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$SERV_ID",
  "nombre": "Servicio de Spa",
  "descripcion": "Acceso completo al spa durante 60 minutos",
  "lugar": "Spa central",
  "precioPorPersona": 50000,
  "duracionMinutos": 60,
  "capacidadMaxima": 10
}
EOF
)

# Crear servicio
request POST "$BASE_URL/api/servicios/add" "$SERV_PAYLOAD"

# Consultar y actualizar servicio
request GET "$BASE_URL/api/servicios/find/$SERV_ID"
request GET "$BASE_URL/api/servicios/all"
SERV_UPDATE_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$SERV_ID",
  "nombre": "Servicio de Spa Premium",
  "descripcion": "Spa con masaje incluido",
  "lugar": "Spa central",
  "precioPorPersona": 70000,
  "duracionMinutos": 90,
  "capacidadMaxima": 8
}
EOF
)
request PUT "$BASE_URL/api/servicios/update" "$SERV_UPDATE_PAYLOAD"

### Disponibilidad de servicio ###
DISP_ID="${RUN_ID}-disponibilidad"
DISP_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$DISP_ID",
  "servicio": { "id": "$SERV_ID" },
  "fecha": "2025-10-02",
  "horaInicio": "09:00",
  "horaFin": "11:00",
  "capacidadDisponible": 5
}
EOF
)
request POST "$BASE_URL/api/disponibilidades/add" "$DISP_PAYLOAD"

# Consultas especializadas de disponibilidad
request GET "$BASE_URL/api/disponibilidades/all"
request GET "$BASE_URL/api/disponibilidades/find/$DISP_ID"
request GET "$BASE_URL/api/disponibilidades/servicio/$SERV_ID/fecha/2025-10-02?capacidad=2"
request GET "$BASE_URL/api/disponibilidades/servicio/$SERV_ID/rango?inicio=2025-10-01&fin=2025-10-03"
request GET "$BASE_URL/api/disponibilidades/buscar?servicioId=$SERV_ID&fecha=2025-10-02&horaInicio=09:00"
request GET "$BASE_URL/api/disponibilidades/existe?servicioId=$SERV_ID&fecha=2025-10-02&horaInicio=09:00"

# Actualizar disponibilidad (cambiar capacidad)
DISP_UPDATE_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$DISP_ID",
  "servicio": { "id": "$SERV_ID" },
  "fecha": "2025-10-02",
  "horaInicio": "09:00",
  "horaFin": "11:00",
  "capacidadDisponible": 8
}
EOF
)
request PUT "$BASE_URL/api/disponibilidades/update" "$DISP_UPDATE_PAYLOAD"

### 6. Reservas de Servicios ###
RS_ID="${RUN_ID}-reserva-servicio"
RS_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$RS_ID",
  "fecha": "2025-10-02",
  "horaInicio": "12:00",
  "numeroPersonas": 2,
  "precioPorPersona": 70000,
  "totalServicio": 140000
}
EOF
)
request POST "$BASE_URL/api/reservas-servicios/add?reservaId=$RESERVA_ID&servicioId=$SERV_ID" "$RS_PAYLOAD"

# Consultar reservas de servicios
request GET "$BASE_URL/api/reservas-servicios/all"
request GET "$BASE_URL/api/reservas-servicios/reserva/$RESERVA_ID"
request GET "$BASE_URL/api/reservas-servicios/servicio/$SERV_ID?fecha=2025-10-02"
request GET "$BASE_URL/api/reservas-servicios/find/$RS_ID"

# Actualizar reserva de servicio (cambiar número de personas y total)
RS_UPDATE_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$RS_ID",
  "fecha": "2025-10-02",
  "horaInicio": "12:00",
  "numeroPersonas": 3,
  "precioPorPersona": 70000,
  "totalServicio": 210000
}
EOF
)
request PUT "$BASE_URL/api/reservas-servicios/update?reservaId=$RESERVA_ID&servicioId=$SERV_ID" "$RS_UPDATE_PAYLOAD"

### 7. Pagos y Resúmenes de Pago ###

PAGO_ID="${RUN_ID}-pago"
PAGO_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$PAGO_ID",
  "tipo": "ANTICIPO",
  "monto": 100000,
  "metodoPago": "Tarjeta",
  "concepto": "Primer pago"
}
EOF
)
request POST "$BASE_URL/api/pagos/add?reservaId=$RESERVA_ID" "$PAGO_PAYLOAD"

# Consultar pagos
request GET "$BASE_URL/api/pagos/all"
request GET "$BASE_URL/api/pagos/find/$PAGO_ID"
request GET "$BASE_URL/api/pagos/reserva/$RESERVA_ID"
request GET "$BASE_URL/api/pagos/reserva/$RESERVA_ID/estado/PENDIENTE"
request GET "$BASE_URL/api/pagos/tipo/ANTICIPO"
request GET "$BASE_URL/api/pagos/reserva/$RESERVA_ID/total-completados"

# Actualizar pago (cambiar monto y tipo)
PAGO_UPDATE_PAYLOAD=$(mkpayload <<EOF
{
  "id": "$PAGO_ID",
  "tipo": "PAGO_PARCIAL",
  "monto": 150000,
  "metodoPago": "Tarjeta",
  "concepto": "Pago parcial actualizado"
}
EOF
)
request PUT "$BASE_URL/api/pagos/update" "$PAGO_UPDATE_PAYLOAD"

# Acciones de negocio en pagos
request POST "$BASE_URL/api/pagos/$PAGO_ID/completar"
request POST "$BASE_URL/api/pagos/$PAGO_ID/fallido"
request POST "$BASE_URL/api/pagos/$PAGO_ID/reembolso"

### Resumen de Pago ###
RESUMEN_PAYLOAD=$(mkpayload <<EOF
{
  "totalHabitaciones": 300000,
  "totalServicios": 210000,
  "totalPagado": 150000
}
EOF
)
request POST "$BASE_URL/api/resumen-pagos/add?reservaId=$RESERVA_ID" "$RESUMEN_PAYLOAD"

# Consultar resumen de pago
request GET "$BASE_URL/api/resumen-pagos/all"
request GET "$BASE_URL/api/resumen-pagos/find/$RESERVA_ID"

# Actualizar resumen de pago (con nuevos totales)
RESUMEN_UPDATE_PAYLOAD=$(mkpayload <<EOF
{
  "reservaId": "$RESERVA_ID",
  "totalHabitaciones": 350000,
  "totalServicios": 210000,
  "totalPagado": 150000
}
EOF
)
request PUT "$BASE_URL/api/resumen-pagos/update?reservaId=$RESERVA_ID" "$RESUMEN_UPDATE_PAYLOAD"

# Recalcular resumen de pago (indica nuevos totales de habitaciones y servicios)
request POST "$BASE_URL/api/resumen-pagos/$RESERVA_ID/recalcular?totalHabitaciones=350000&totalServicios=210000"

### 8. Limpieza de entidades creadas ###

# Eliminar recursos en orden inverso para no dejar dependencias pendientes
request DELETE "$BASE_URL/api/pagos/delete/$PAGO_ID"
request DELETE "$BASE_URL/api/resumen-pagos/delete/$RESERVA_ID"
request DELETE "$BASE_URL/api/reservas-servicios/delete/$RS_ID"
request DELETE "$BASE_URL/api/disponibilidades/delete/$DISP_ID"
request DELETE "$BASE_URL/api/servicios/delete/$SERV_ID"
request DELETE "$BASE_URL/api/estancias/delete/$EST_ID"
request DELETE "$BASE_URL/api/reservas/delete/$RESERVA_ID"
request DELETE "$BASE_URL/api/habitaciones/delete/$HAB_ID"
request DELETE "$BASE_URL/api/habitaciones-tipos/delete/$TIPO_ID"
request DELETE "$BASE_URL/api/empleados-perfil/delete/$USER_ID"
request DELETE "$BASE_URL/api/clientes-perfil/delete/$USER_ID"
request DELETE "$BASE_URL/api/usuarios/delete/$USER_ID"

log "Smoke test finalizado. Revisa $LOG_FILE para más detalles."