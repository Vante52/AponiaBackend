# AponiaBackend
Servicio REST de Aponia: Gestión de  reservas, estancias, servicios y pagos con control por empleado. Spring Boot + PostgreSQL + Swagger.
 ### Restaurar en otro dispositivo

1. Clona el repositorio.
2. Crea el archivo `.env`.
3. Ejecuta `docker compose up -d db`.
4. Ejecuta `docker exec -i MiPostgres psql -U samiLeMeteAlFront -d aponiadb < docker/initdb/seed.sql`.

La primera vez que se cree el volumen, PostgreSQL restaurará automáticamente los datos del volcado versionado.

> **Nota:** Si necesitas regenerar por completo la base y forzar la restauración del volcado, elimina el volumen local: `docker compose down -v`.

* **Fuente única:** `docker/initdb/seed.sql` se versiona junto con el código. Al clonar el repositorio se obtiene el mismo archivo (estructura y datos) en cualquier equipo.
* **Restauración automatizada:** cada vez que el volumen `pgdata` se crea desde cero, el contenedor de PostgreSQL ejecuta automáticamente todo lo que haya en `docker/initdb`. Si el `seed.sql` contiene tus datos, se insertarán antes de que la aplicación empiece a usarlos.
* **Actualización controlada:** cuando cambias información en tu base en un dispositivo, vuelves a ejecutar `./scripts/export-db.sh` (u `pg_dump`) y haces commit del resultado. De esa forma el `seed.sql` vuelve a representar el estado oficial y, tras sincronizar el repositorio, el resto de dispositivos puede recrear exactamente ese estado eliminando su volumen local.

En resumen, mientras el volcado sea la referencia y se mantenga versionado, cualquier entorno que arranque con este `docker-compose` reproducirá la misma base de datos.

 ### Restaurar en otro dispositivo

1. Clona el repositorio.
2. Crea el archivo `.env` a partir de `.env.example` (ajusta contraseña si es necesario).
3. Ejecuta `docker compose up -d db`.

La primera vez que se cree el volumen, PostgreSQL restaurará automáticamente los datos del volcado versionado.

> **Nota:** Si necesitas regenerar por completo la base y forzar la restauración del volcado, elimina el volumen local: `docker compose down -v`.

### ¿Por qué esto garantiza la misma base en todos los dispositivos?

* **Fuente única de verdad:** `docker/initdb/seed.sql` se versiona junto con el código. Al clonar el repositorio se obtiene el mismo archivo (estructura y datos) en cualquier equipo.
* **Restauración automatizada:** cada vez que el volumen `pgdata` se crea desde cero, el contenedor de PostgreSQL ejecuta automáticamente todo lo que haya en `docker/initdb`. Si el `seed.sql` contiene tus datos, se insertarán antes de que la aplicación empiece a usarlos.
* **Actualización controlada:** cuando cambias información en tu base en un dispositivo, vuelves a ejecutar `./scripts/export-db.sh` (u `pg_dump`) y haces commit del resultado. De esa forma el `seed.sql` vuelve a representar el estado oficial y, tras sincronizar el repositorio, el resto de dispositivos puede recrear exactamente ese estado eliminando su volumen local.

En resumen, mientras el volcado sea la referencia y se mantenga versionado, cualquier entorno que arranque con este `docker-compose` reproducirá la misma base de datos.