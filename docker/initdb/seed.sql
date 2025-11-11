-- PostgreSQL database dump corregido



SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- FUNCIONES
CREATE OR REPLACE FUNCTION public.actualizar_resumen_despues_cambio() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    DECLARE
        v_reserva_id CHAR(36);
    BEGIN
        IF TG_TABLE_NAME = 'estancias' THEN
            v_reserva_id := COALESCE(NEW.reserva_id, OLD.reserva_id);
        ELSIF TG_TABLE_NAME = 'reservas_servicios' THEN
            v_reserva_id := COALESCE(NEW.reserva_id, OLD.reserva_id);
        ELSIF TG_TABLE_NAME = 'pagos' THEN
            v_reserva_id := COALESCE(NEW.reserva_id, OLD.reserva_id);
        ELSIF TG_TABLE_NAME = 'reservas' THEN
            v_reserva_id := COALESCE(NEW.id, OLD.id);
        END IF;
        
        PERFORM actualizar_resumen_pagos(v_reserva_id);
        RETURN NULL;
    END;
END;
$$;

ALTER FUNCTION public.actualizar_resumen_despues_cambio() OWNER TO "samiLeMeteAlFront";

CREATE OR REPLACE FUNCTION public.actualizar_resumen_pagos(p_reserva_id varchar(36)) 
RETURNS void
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO resumen_pagos (
        reserva_id, 
        total_habitaciones, 
        total_servicios, 
        total_reserva, 
        total_pagado, 
        saldo_pendiente, 
        ultima_actualizacion
    )
    SELECT 
        r.id,
        COALESCE(SUM(e.total_estadia), 0),
        COALESCE(SUM(rs.total_servicio), 0),
        COALESCE(SUM(e.total_estadia), 0) + COALESCE(SUM(rs.total_servicio), 0),
        COALESCE(SUM(p.monto) FILTER (WHERE p.estado = 'COMPLETADO'), 0),
        (COALESCE(SUM(e.total_estadia), 0) + COALESCE(SUM(rs.total_servicio), 0)) - 
        COALESCE(SUM(p.monto) FILTER (WHERE p.estado = 'COMPLETADO'), 0),
        CURRENT_TIMESTAMP
    FROM reservas r
    LEFT JOIN estancias e ON r.id = e.reserva_id
    LEFT JOIN reservas_servicios rs ON r.id = rs.reserva_id
    LEFT JOIN pagos p ON r.id = p.reserva_id
    WHERE r.id = p_reserva_id  
    GROUP BY r.id
    ON CONFLICT (reserva_id) 
    DO UPDATE SET
        total_habitaciones = EXCLUDED.total_habitaciones,
        total_servicios = EXCLUDED.total_servicios,
        total_reserva = EXCLUDED.total_reserva,
        total_pagado = EXCLUDED.total_pagado,
        saldo_pendiente = EXCLUDED.saldo_pendiente,
        ultima_actualizacion = EXCLUDED.ultima_actualizacion;
END;
$$;

ALTER FUNCTION public.actualizar_resumen_pagos(p_reserva_id varchar(36)) OWNER TO "samiLeMeteAlFront";

CREATE OR REPLACE FUNCTION public.asignar_habitacion_al_confirmar() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_habitacion_id CHAR(36);
    e_record RECORD;
BEGIN
    IF NEW.estado = 'CONFIRMADA' AND OLD.estado != 'CONFIRMADA' THEN
        FOR e_record IN 
            SELECT * FROM estancias WHERE reserva_id = NEW.id AND habitacion_asignada IS NULL
        LOOP
            v_habitacion_id := asignar_habitacion_disponible(
                e_record.tipo_habitacion_id, 
                e_record.entrada, 
                e_record.salida
            );
            
            IF v_habitacion_id IS NOT NULL THEN
                UPDATE estancias
                SET habitacion_asignada = v_habitacion_id
                WHERE id = e_record.id;
            ELSE
                RAISE EXCEPTION 'No hay habitaciones disponibles del tipo % para las fechas % a %', 
                    e_record.tipo_habitacion_id, e_record.entrada, e_record.salida;
            END IF;
        END LOOP;
    END IF;
    
    RETURN NEW;
END;
$$;

ALTER FUNCTION public.asignar_habitacion_al_confirmar() OWNER TO "samiLeMeteAlFront";

-- Función para asignar habitación disponible (verifica ocupación por fechas)
CREATE OR REPLACE FUNCTION public.asignar_habitacion_disponible(p_tipo_id varchar(36), p_check_in date, p_check_out date) RETURNS varchar(36)
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_habitacion_id CHAR(36);
BEGIN
    -- Busca una habitación del tipo solicitado que NO esté ocupada en las fechas dadas
    SELECT h.id INTO v_habitacion_id
    FROM habitaciones h
    WHERE h.tipo_id = p_tipo_id
    AND h.activa = TRUE
    AND NOT EXISTS (
        SELECT 1
        FROM estancias e
        WHERE e.habitacion_asignada = h.id
        AND e.habitacion_asignada IS NOT NULL
        -- Verifica que las fechas NO se traslapen
        AND NOT (p_check_out <= e.entrada OR p_check_in >= e.salida)
    )
    LIMIT 1;
    
    RETURN v_habitacion_id;
END;
$$;

ALTER FUNCTION public.asignar_habitacion_disponible(p_tipo_id varchar(36), p_check_in date, p_check_out date) OWNER TO "samiLeMeteAlFront";

-- Función para verificar disponibilidad de un tipo de habitación
CREATE OR REPLACE FUNCTION public.verificar_disponibilidad_tipo(p_tipo_id varchar(36), p_check_in date, p_check_out date) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_total_habitaciones integer;
    v_habitaciones_ocupadas integer;
    v_disponibles integer;
BEGIN
    -- Cuenta el total de habitaciones activas de este tipo
    SELECT COUNT(*) INTO v_total_habitaciones
    FROM habitaciones
    WHERE tipo_id = p_tipo_id AND activa = TRUE;
    
    -- Cuenta cuántas están ocupadas en el rango de fechas
    SELECT COUNT(DISTINCT e.habitacion_asignada) INTO v_habitaciones_ocupadas
    FROM estancias e
    WHERE e.habitacion_asignada IN (
        SELECT id FROM habitaciones WHERE tipo_id = p_tipo_id AND activa = TRUE
    )
    AND e.habitacion_asignada IS NOT NULL
    AND NOT (p_check_out <= e.entrada OR p_check_in >= e.salida);
    
    v_disponibles := v_total_habitaciones - COALESCE(v_habitaciones_ocupadas, 0);
    
    RETURN v_disponibles;
END;
$$;

ALTER FUNCTION public.verificar_disponibilidad_tipo(p_tipo_id varchar(36), p_check_in date, p_check_out date) OWNER TO "samiLeMeteAlFront";

SET default_tablespace = '';
SET default_table_access_method = heap;

-- TABLAS
CREATE TABLE IF NOT EXISTS public.usuarios (
    id            VARCHAR(36) PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol           VARCHAR(50)  NOT NULL DEFAULT 'CLIENTE',
    CONSTRAINT check_rol_valido
        CHECK (rol IN ('ADMIN','CLIENTE','STAFF','RECEPCIONISTA'))
);

ALTER TABLE public.usuarios OWNER TO "samiLeMeteAlFront";
CREATE TABLE IF NOT EXISTS public.clientes_perfil (
    usuario_id     VARCHAR(36) PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    telefono        VARCHAR(25),
    fecha_registro  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT,
    CONSTRAINT fk_clientes_perfil_usuario
        FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id)
);


ALTER TABLE public.clientes_perfil OWNER TO "samiLeMeteAlFront";

CREATE TABLE IF NOT EXISTS public.empleados_perfil (
    usuario_id         VARCHAR(36) PRIMARY KEY,
    nombre_completo    VARCHAR(150) NOT NULL,
    telefono           VARCHAR(25),
    cargo              VARCHAR(100) NOT NULL,
    salario            NUMERIC(12,2),
    fecha_contratacion DATE NOT NULL DEFAULT CURRENT_DATE,
    version            BIGINT,
    CONSTRAINT fk_empleados_perfil_usuario
        FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id),
    CONSTRAINT check_salario_no_negativo CHECK (salario IS NULL OR salario >= 0)
);


ALTER TABLE public.empleados_perfil OWNER TO "samiLeMeteAlFront";





CREATE TABLE IF NOT EXISTS public.habitaciones_tipos (
    id varchar(36) PRIMARY KEY,
    nombre varchar(50) UNIQUE NOT NULL,
    descripcion text,
    aforo_maximo integer NOT NULL,
    precio_por_noche numeric(12,2) NOT NULL,
    activa boolean DEFAULT true NOT NULL,
    CONSTRAINT check_aforo_positivo CHECK ((aforo_maximo > 0)),
    CONSTRAINT check_precio_positivo CHECK ((precio_por_noche >= (0)::numeric))
);


CREATE TABLE IF NOT EXISTS public.habitaciones (
    id varchar(36) PRIMARY KEY,
    tipo_id varchar(36) NOT NULL,
    numero integer UNIQUE NOT NULL,
    activa boolean DEFAULT true NOT NULL,
    CONSTRAINT fk_habitaciones_tipo
        FOREIGN KEY (tipo_id) REFERENCES public.habitaciones_tipos(id),
    CONSTRAINT check_numero_valido CHECK ((numero > 0))
);

ALTER TABLE public.habitaciones OWNER TO "samiLeMeteAlFront";

CREATE TABLE IF NOT EXISTS public.habitaciones_tipos (
    id               VARCHAR(36) PRIMARY KEY,
    nombre           VARCHAR(50) UNIQUE NOT NULL,
    descripcion      TEXT,
    aforo_maximo     INTEGER NOT NULL,
    precio_por_noche NUMERIC(12,2) NOT NULL,
    activa           BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT check_aforo_positivo CHECK (aforo_maximo > 0),
    CONSTRAINT check_precio_positivo CHECK (precio_por_noche >= 0)
);

ALTER TABLE public.habitaciones_tipos OWNER TO "samiLeMeteAlFront";

CREATE TABLE IF NOT EXISTS public.reservas (
    id              VARCHAR(36) PRIMARY KEY,
    codigo          VARCHAR(32) UNIQUE NOT NULL,
    cliente_id      VARCHAR(36) NOT NULL,
    fecha_creacion  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado          VARCHAR(32) NOT NULL DEFAULT 'CONFIRMADA',
    notas           TEXT,

    CONSTRAINT fk_reservas_cliente
        FOREIGN KEY (cliente_id) REFERENCES public.usuarios(id),

    CONSTRAINT check_estado_valido
        CHECK (estado IN ('CONFIRMADA','CANCELADA','COMPLETADA'))
);

ALTER TABLE public.reservas OWNER TO "samiLeMeteAlFront";

CREATE TABLE IF NOT EXISTS public.servicios (
    id                  VARCHAR(36) PRIMARY KEY,
    nombre              VARCHAR(120) NOT NULL,
    descripcion         TEXT,
    lugar               VARCHAR(120) NOT NULL,
    precio_por_persona  NUMERIC(12,2) NOT NULL,
    duracion_minutos    INTEGER NOT NULL,
    capacidad_maxima    INTEGER,

    CONSTRAINT check_precio_positivo    CHECK (precio_por_persona >= 0),
    CONSTRAINT check_duracion_positiva  CHECK (duracion_minutos > 0),
    CONSTRAINT check_capacidad_positiva CHECK (capacidad_maxima IS NULL OR capacidad_maxima > 0)
);

ALTER TABLE public.servicios OWNER TO "samiLeMeteAlFront";


CREATE TABLE IF NOT EXISTS public.estancias (
    id                   VARCHAR(36) PRIMARY KEY,
    reserva_id           VARCHAR(36) NOT NULL,
    tipo_habitacion_id   VARCHAR(36) NOT NULL,
    check_in             BOOLEAN NOT NULL DEFAULT FALSE,
    check_out            BOOLEAN NOT NULL DEFAULT FALSE,
    entrada              DATE    NOT NULL,
    salida               DATE    NOT NULL,
    numero_huespedes     INTEGER NOT NULL,
    precio_por_noche     NUMERIC(12,2) NOT NULL,
    total_estadia        NUMERIC(12,2) NOT NULL,
    habitacion_asignada  VARCHAR(36),
    CONSTRAINT fk_estancias_reserva
        FOREIGN KEY (reserva_id) REFERENCES public.reservas(id),
    CONSTRAINT fk_estancias_tipo_habitacion
        FOREIGN KEY (tipo_habitacion_id) REFERENCES public.habitaciones_tipos(id),
    CONSTRAINT fk_estancias_habitacion_asignada
        FOREIGN KEY (habitacion_asignada) REFERENCES public.habitaciones(id),
    CONSTRAINT check_fechas_validas       CHECK (salida > entrada),
    CONSTRAINT check_huespedes_positivo   CHECK (numero_huespedes > 0),
    CONSTRAINT check_precio_positivo      CHECK (precio_por_noche >= 0),
    CONSTRAINT check_total_positivo       CHECK (total_estadia   >= 0)
);

ALTER TABLE public.estancias OWNER TO "samiLeMeteAlFront";


CREATE TABLE IF NOT EXISTS public.pagos (
    id            VARCHAR(36) PRIMARY KEY,
    reserva_id    VARCHAR(36) NOT NULL,
    tipo          VARCHAR(20) NOT NULL,
    monto         NUMERIC(12,2) NOT NULL,
    fecha         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metodo_pago   VARCHAR(50),
    estado        VARCHAR(20) NOT NULL DEFAULT 'COMPLETADO',
    concepto      VARCHAR(200),
    CONSTRAINT fk_pagos_reserva
        FOREIGN KEY (reserva_id) REFERENCES public.reservas(id),
    CONSTRAINT check_monto_positivo CHECK (monto > 0),
    CONSTRAINT check_tipo_valido CHECK (tipo IN ('ANTICIPO','PAGO_PARCIAL','PAGO_COMPLETO','REEMBOLSO')),
    CONSTRAINT check_estado_pago_valido CHECK (estado IN ('COMPLETADO','FALLIDO','REEMBOLSADO'))
);

ALTER TABLE public.pagos OWNER TO "samiLeMeteAlFront";



CREATE TABLE IF NOT EXISTS public.reservas_servicios (
    id                    VARCHAR(36) PRIMARY KEY,
    reserva_id            VARCHAR(36) NOT NULL,
    servicio_id           VARCHAR(36) NOT NULL,
    fecha                 DATE NOT NULL,
    hora_inicio           TIME WITHOUT TIME ZONE NOT NULL,
    numero_personas       INTEGER NOT NULL,
    precio_por_persona    NUMERIC(12,2) NOT NULL,
    total_servicio        NUMERIC(12,2) NOT NULL,
    contratado_por_empleado_id VARCHAR(36),

    CONSTRAINT fk_rs_reserva    FOREIGN KEY (reserva_id)  REFERENCES public.reservas(id),
    CONSTRAINT fk_rs_servicio   FOREIGN KEY (servicio_id) REFERENCES public.servicios(id),
    CONSTRAINT fk_rs_empleado   FOREIGN KEY (contratado_por_empleado_id) REFERENCES public.usuarios(id),

    CONSTRAINT check_personas_positivo CHECK (numero_personas > 0),
    CONSTRAINT check_precio_positivo   CHECK (precio_por_persona >= 0),
    CONSTRAINT check_total_positivo    CHECK (total_servicio >= 0)
);

ALTER TABLE public.reservas_servicios OWNER TO "samiLeMeteAlFront";

CREATE TABLE IF NOT EXISTS public.resumen_pagos (
    reserva_id         VARCHAR(36) PRIMARY KEY,
    total_habitaciones NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_servicios    NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_reserva      NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_pagado       NUMERIC(12,2) NOT NULL DEFAULT 0,
    saldo_pendiente    NUMERIC(12,2) NOT NULL DEFAULT 0,
    ultima_actualizacion TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resumen_pagos_reserva
        FOREIGN KEY (reserva_id) REFERENCES public.reservas(id),
    CONSTRAINT check_no_negativos_resumen CHECK (
        total_habitaciones >= 0 AND
        total_servicios    >= 0 AND
        total_reserva      >= 0 AND
        total_pagado       >= 0 AND
        saldo_pendiente    >= 0
    )
);

ALTER TABLE public.resumen_pagos OWNER TO "samiLeMeteAlFront";

CREATE TABLE IF NOT EXISTS public.servicio_disponibilidad (
    id                    VARCHAR(36) PRIMARY KEY,
    servicio_id           VARCHAR(36) NOT NULL,
    fecha                 DATE NOT NULL,
    hora_inicio           TIME WITHOUT TIME ZONE NOT NULL,
    hora_fin              TIME WITHOUT TIME ZONE NOT NULL,
    capacidad_disponible  INTEGER NOT NULL,

    CONSTRAINT fk_servicio_disp_servicio
        FOREIGN KEY (servicio_id) REFERENCES public.servicios(id),

    CONSTRAINT check_capacidad_valida CHECK (capacidad_disponible >= 0),
    CONSTRAINT check_horas_validas    CHECK (hora_fin > hora_inicio),

    CONSTRAINT ux_servicio_disp UNIQUE (servicio_id, fecha, hora_inicio)
);

ALTER TABLE public.servicio_disponibilidad OWNER TO "samiLeMeteAlFront";



CREATE TABLE IF NOT EXISTS public.imagenes (
    id                  VARCHAR(36) PRIMARY KEY,
    servicio_id         VARCHAR(36),
    tipo_habitacion_id  VARCHAR(36),
    url                 VARCHAR(500) NOT NULL,

    CONSTRAINT fk_imagenes_servicio
        FOREIGN KEY (servicio_id) REFERENCES public.servicios(id),
    CONSTRAINT fk_imagenes_tipo_hab
        FOREIGN KEY (tipo_habitacion_id) REFERENCES public.habitaciones_tipos(id),

    -- exactamente uno de los dos debe venir (XOR)
    CONSTRAINT check_imagen_destino CHECK (
        (servicio_id IS NOT NULL AND tipo_habitacion_id IS NULL) OR
        (servicio_id IS NULL AND tipo_habitacion_id IS NOT NULL)
    )
);

ALTER TABLE public.imagenes OWNER TO "samiLeMeteAlFront";

-- DATOS
INSERT INTO public.usuarios (id, email, password_hash, rol) VALUES
('7325d128-3b86-40fb-865c-4584b25cb2f9', 'juanfe@dropsy.com', '1234', 'CLIENTE'),
('4564fe1b-2dd7-4e56-8b31-8f88a62fd632', 'angarita@lemeteaweb.puj', '1234', 'ADMIN'),
('60fc894b-e09a-4265-b436-df53a490e2eb', 'samu@gmail.com', '1234', 'CLIENTE'),
('0bf298eb-bc81-4938-ad2c-b7f96a13a105', 'nuevo@gmail.com', '123', 'CLIENTE'),
('emp_001', 'recepcion1@hotel.com', '1234', 'RECEPCIONISTA'),
('emp_002', 'recepcion2@hotel.com', '1234', 'RECEPCIONISTA'),
('emp_003', 'chef1@hotel.com', '1234', 'STAFF'),
('emp_004', 'chef2@hotel.com', '1234', 'STAFF'),
('emp_005', 'camarera1@hotel.com', '1234', 'STAFF'),
('emp_006', 'camarera2@hotel.com', '1234', 'STAFF'),
('emp_007', 'gerente@hotel.com', '1234', 'ADMIN'),
('emp_008', 'mantenimiento1@hotel.com', '1234', 'STAFF'),
('cli_001', 'cliente1@hotel.com', '1234', 'CLIENTE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.clientes_perfil (usuario_id, nombre_completo, telefono, fecha_registro, version) VALUES
('7325d128-3b86-40fb-865c-4584b25cb2f9', 'Aguirre', '234567', '2025-09-08 22:51:27.58619', NULL),
('60fc894b-e09a-4265-b436-df53a490e2eb', 'samuel campos', '321', '2025-09-09 10:03:56.095483', NULL),
('0bf298eb-bc81-4938-ad2c-b7f96a13a105', 'nuevo', '3002112575', '2025-09-09 10:57:58.800666', NULL),
('cli_001', 'Carlos Pérez', '3000000000', '2025-10-07 10:41:37.742121', NULL)
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO public.empleados_perfil (usuario_id, nombre_completo, telefono, cargo, salario, fecha_contratacion, version) VALUES
('emp_001', 'María García', '3001234567', 'Recepcionista', 1800000.00, '2023-05-10', NULL),
('emp_002', 'Hania Campos', '3019876543', 'Recepcionista', 1750000.00, '2023-01-15', NULL),
('emp_003', 'Santiago Fortich', '3204567890', 'Chef Ejecutivo', 2800000.00, '2021-08-20', NULL),
('emp_004', 'Andrés Gómez', '3107654321', 'Camarero', 1500000.00, '2022-11-01', NULL),
('emp_005', 'Karen Manuela', '3152345678', 'Camarera', 1500000.00, '2023-06-12', NULL),
('emp_006', 'Paola Aguilera', '3186543210', 'Sous Chef', 2300000.00, '2023-09-01', NULL),
('emp_007', 'Sebastian Angarita', '3205558899', 'Gerente General', 4200000.00, '2020-03-05', NULL),
('emp_008', 'Camilo Triana', '3114455667', 'Mantenimiento', 1600000.00, '2022-02-17', NULL)
ON CONFLICT (usuario_id) DO NOTHING;

INSERT INTO public.habitaciones_tipos (id, nombre, descripcion, aforo_maximo, precio_por_noche, activa) VALUES
('tipo_normal', 'Normal', 'Habitación funcional y cómoda para estancias cortas. Incluye amenities básicos y Wi-Fi gratuito.', 2, 250000.00, true),
('tipo_executive', 'Executive', 'Habitación con espacio adicional y escritorio de trabajo. Ideal para viajes de negocios.', 2, 380000.00, true),
('tipo_vip', 'VIP', 'Suite con amenidades premium y vista privilegiada. Incluye acceso a lounge VIP.', 3, 520000.00, true),
('tipo_luxury', 'Luxury', 'Suite de máximo confort con sala privada y terraza. Experiencia de lujo con servicios personalizados.', 4, 700000.00, true),
('tipo_connecting', 'Connecting', 'Habitaciones interconectadas perfectas para grupos familiares o que viajan juntos.', 4, 600000.00, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.habitaciones (id, tipo_id, numero, activa) VALUES
('hab_101', 'tipo_normal', 101, true),
('hab_102', 'tipo_normal', 102, true),
('hab_103', 'tipo_normal', 103, true),
('hab_104', 'tipo_normal', 104, true),
('hab_105', 'tipo_normal', 105, true),
('hab_106', 'tipo_normal', 106, true),
('hab_107', 'tipo_normal', 107, true),
('hab_108', 'tipo_normal', 108, true),
('hab_109', 'tipo_normal', 109, true),
('hab_110', 'tipo_normal', 110, true),
('hab_201', 'tipo_executive', 201, true),
('hab_202', 'tipo_executive', 202, true),
('hab_203', 'tipo_executive', 203, true),
('hab_204', 'tipo_executive', 204, true),
('hab_205', 'tipo_executive', 205, true),
('hab_206', 'tipo_executive', 206, true),
('hab_207', 'tipo_executive', 207, true),
('hab_208', 'tipo_executive', 208, true),
('hab_209', 'tipo_executive', 209, true),
('hab_210', 'tipo_executive', 210, true),
('hab_301', 'tipo_vip', 301, true),
('hab_302', 'tipo_vip', 302, true),
('hab_303', 'tipo_vip', 303, true),
('hab_304', 'tipo_vip', 304, true),
('hab_305', 'tipo_vip', 305, true),
('hab_306', 'tipo_vip', 306, true),
('hab_307', 'tipo_vip', 307, true),
('hab_308', 'tipo_vip', 308, true),
('hab_309', 'tipo_vip', 309, true),
('hab_310', 'tipo_vip', 310, true),
('hab_401', 'tipo_luxury', 401, true),
('hab_402', 'tipo_luxury', 402, true),
('hab_403', 'tipo_luxury', 403, true),
('hab_404', 'tipo_luxury', 404, true),
('hab_405', 'tipo_luxury', 405, true),
('hab_406', 'tipo_luxury', 406, true),
('hab_407', 'tipo_luxury', 407, true),
('hab_408', 'tipo_luxury', 408, true),
('hab_409', 'tipo_luxury', 409, true),
('hab_410', 'tipo_luxury', 410, true),
('hab_501', 'tipo_connecting', 501, true),
('hab_502', 'tipo_connecting', 502, true),
('hab_503', 'tipo_connecting', 503, true),
('hab_504', 'tipo_connecting', 504, true),
('hab_505', 'tipo_connecting', 505, true),
('hab_506', 'tipo_connecting', 506, true),
('hab_507', 'tipo_connecting', 507, true),
('hab_508', 'tipo_connecting', 508, true),
('hab_509', 'tipo_connecting', 509, true),
('hab_510', 'tipo_connecting', 510, true)
ON CONFLICT (numero) DO NOTHING;

INSERT INTO public.servicios (id, nombre, descripcion, lugar, precio_por_persona, duracion_minutos, capacidad_maxima) VALUES
('servicio_spa', 'Spa de autor', 'Rituales con esencias locales, 60-120 min. Masajes, sauna y rituales holísticos que combinan técnicas ancestrales y modernas para revitalizar tu cuerpo y mente en un ambiente de serenidad absoluta.', 'Mantra SPA', 150000.00, 90, 10),
('servicio_trekking', 'Trekking al amanecer', 'Panorámicas únicas y picnic gourmet. Caminatas ecológicas guiadas para disfrutar de la naturaleza al amanecer.', 'Punto de encuentro en el lobby del hotel', 200000.00, 240, 10),
('servicio_restaurante', 'Restaurante Gourmet', 'Gastronomía de autor con productos frescos y locales. Menús de temporada y maridajes seleccionados para una experiencia culinaria exclusiva.', 'Restaurante el gran cañón', 90.00, 90, 50),
('servicio_transporte', 'Transporte Privado', 'Traslados al aeropuerto y movilidad en vehículos de lujo con chofer privado disponible las 24 horas, garantizando seguridad y confort.', 'Punto de encuentro en el lobby', 100000.00, 70, 3),
('servicio_wifi', 'Wi-Fi & Cowork', 'Espacio premium de Wi-Fi y coworking, con conexión ultrarrápida y ambiente inspirador, disponible 24/7 para maximizar tu productividad.', 'Sala de eventos 3, 4 y 5', 30.00, 10, 100)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.imagenes (id, servicio_id, tipo_habitacion_id, url) VALUES
('img_normal_1', NULL, 'tipo_normal', 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fsuite_normal.jpg?alt=media&token=028bd170-dcb7-4e49-a08b-55c834083ff3'),
('img_normal_2', NULL, 'tipo_normal', 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fba%C3%B1o_normal.jpg?alt=media&token=bcbadfca-0e07-482b-a084-94b0611dd94d'),
('img_executive_1', NULL, 'tipo_executive', 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fhabitacion_ejecutiva_cama.jpg?alt=media&token=441f9315-8097-4533-aac5-e830f76953f2'),
('img_executive_2', NULL, 'tipo_executive', 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fsuite_ejecutiva.jpg?alt=media&token=5d108710-8492-47b2-8773-ba0527dae9a7'),
('img_vip_1', NULL, 'tipo_vip', 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fhabitacion_lujo.jpg?alt=media&token=e613caac-19de-457e-948a-f47690690877'),
('img_vip_2', NULL, 'tipo_vip', 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fsuite-lujo.jpg?alt=media&token=19adcd3a-1327-402a-b145-6418b5d04316'),
('img_luxury_1', NULL, 'tipo_luxury', 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fsuite_vip.jpg?alt=media&token=5b69a74e-85b5-40eb-8956-84cc3e6e0343'),
('img_luxury_2', NULL, 'tipo_luxury', 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fsala_luxury.jpg?alt=media&token=c0b4544e-4f82-4c48-a22f-66ba7bcdb3a7'),
('img_spa_1', 'servicio_spa', NULL, 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fspa_2.jpg?alt=media&token=8a4ef544-440c-4e34-90e1-3f9bc2511b8f'),
('img_spa_2', 'servicio_spa', NULL, 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fspa_3.jpg?alt=media&token=7c2dc267-2927-4b32-8164-ce7b93f9b3bf'),
('img_trekking_1', 'servicio_trekking', NULL, 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Ftrecking.jpg?alt=media&token=0a5ca1f7-d034-4d91-a312-3715537a8eaa'),
('img_trekking_2', 'servicio_trekking', NULL, 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Ftrecking_2.jpeg?alt=media&token=edccf3d9-d809-4f0b-848c-a503adc1c233'),
('img_rest_1', 'servicio_restaurante', NULL, 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Frestaurante_2.jpg?alt=media&token=568ea8b9-ec34-4090-98b8-ba0df452765c'),
('img_rest_2', 'servicio_restaurante', NULL, 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Frestaurante_3.jpg?alt=media&token=26b988b8-9e7e-4f9d-9578-95ce6ccaac9d'),
('img_trans_1', 'servicio_transporte', NULL, 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Ftransporte_2.jpg?alt=media&token=01a46ff9-6aa6-4be2-92d4-eaaccbf2d765'),
('img_trans_2', 'servicio_transporte', NULL, 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Ftransporte_3.jpg?alt=media&token=3b80c248-104a-4792-bb0b-a105218889fb'),
('img_wifi_1', 'servicio_wifi', NULL, 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fcatacafe.jpg?alt=media&token=63d843d1-2633-4148-b46c-8a02cf4cad9a'),
('img_wifi_2', 'servicio_wifi', NULL, 'https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fwifi_hotel2.jpg?alt=media&token=196af0e0-1e9d-4cdc-b2f9-9cb9d2d94242'),
('img_connecting_1', NULL, 'tipo_connecting', 'https://www.hilton.com/im/en/NoHotel/15621541/1252-corp-connecting-rooms-ohw-room.jpg?impolicy=crop&cw=4500&ch=1250&gravity=NorthWest&xposition=0&yposition=875&rw=3840&rh=1068'),
('img_connecting_2', NULL, 'tipo_connecting', 'https://img.lavdg.com/sc/7KboYBlOqMPLCeg3EDsGYC8ZaIo=/768x/2020/07/17/00121594983853338592141/Foto/pazo4.jpg')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.servicio_disponibilidad (id, servicio_id, fecha, hora_inicio, hora_fin, capacidad_disponible) VALUES
('disp_spa_1', 'servicio_spa', '2025-09-01', '09:00:00', '21:00:00', 10),
('disp_spa_2', 'servicio_spa', '2025-09-02', '09:00:00', '21:00:00', 10),
('disp_trekking_1', 'servicio_trekking', '2025-09-01', '05:00:00', '10:00:00', 10),
('disp_trekking_2', 'servicio_trekking', '2025-09-02', '05:00:00', '10:00:00', 10),
('disp_rest_1', 'servicio_restaurante', '2025-09-01', '07:00:00', '23:00:00', 50),
('disp_rest_2', 'servicio_restaurante', '2025-09-02', '07:00:00', '23:00:00', 50),
('disp_trans_1', 'servicio_transporte', '2025-09-01', '00:00:00', '23:59:59', 3),
('disp_trans_2', 'servicio_transporte', '2025-09-02', '00:00:00', '23:59:59', 3),
('disp_wifi_1', 'servicio_wifi', '2025-09-01', '00:00:00', '23:59:59', 100),
('disp_wifi_2', 'servicio_wifi', '2025-09-02', '00:00:00', '23:59:59', 100)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.reservas (id, codigo, cliente_id, fecha_creacion, estado, notas) VALUES
('res_0001', 'R-2025-0001', 'cli_001', '2025-10-07 10:41:37.742121', 'CONFIRMADA', 'Reserva de 3 noches, tipo_normal')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.estancias (id, reserva_id, tipo_habitacion_id, check_in, check_out, entrada, salida, numero_huespedes, precio_por_noche, total_estadia, habitacion_asignada) VALUES
('est_0001', 'res_0001', 'tipo_normal', false, false, '2025-10-10', '2025-10-13', 2, 250000.00, 750000.00, 'hab_101')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.reservas_servicios (id, reserva_id, servicio_id, fecha, hora_inicio, numero_personas, precio_por_persona, total_servicio, contratado_por_empleado_id) VALUES
('rs_0001', 'res_0001', 'servicio_spa', '2025-10-11', '09:00:00', 2, 150000.00, 300000.00, 'emp_002'),
('rs_0002', 'res_0001', 'servicio_transporte', '2025-10-10', '07:00:00', 2, 100000.00, 200000.00, 'emp_002')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.pagos (id, reserva_id, tipo, monto, fecha, metodo_pago, estado, concepto) VALUES
('pay_0001', 'res_0001', 'ANTICIPO', 500000.00, '2025-10-07 10:41:37.742121', 'TARJETA', 'COMPLETADO', 'Abono inicial')
ON CONFLICT (id) DO NOTHING;

INSERT INTO public.resumen_pagos (reserva_id, total_habitaciones, total_servicios, total_reserva, total_pagado, saldo_pendiente, ultima_actualizacion) VALUES
('res_0001', 750000.00, 500000.00, 1250000.00, 500000.00, 750000.00, '2025-10-07 10:41:37.742121')
ON CONFLICT (reserva_id) DO NOTHING;

-- ÍNDICES
CREATE INDEX IF NOT EXISTS idx_estancias_fechas ON public.estancias USING btree (entrada, salida);
CREATE INDEX IF NOT EXISTS idx_estancias_habitacion_fechas ON public.estancias USING btree (habitacion_asignada, entrada, salida);
CREATE INDEX IF NOT EXISTS idx_estancias_tipo ON public.estancias USING btree (tipo_habitacion_id);
CREATE INDEX IF NOT EXISTS idx_habitaciones_numero ON public.habitaciones USING btree (numero);
CREATE INDEX IF NOT EXISTS idx_habitaciones_tipo ON public.habitaciones USING btree (tipo_id);
CREATE INDEX IF NOT EXISTS idx_habitaciones_tipo_activa ON public.habitaciones USING btree (tipo_id, activa);
CREATE INDEX IF NOT EXISTS idx_pagos_estado ON public.pagos USING btree (estado);
CREATE INDEX IF NOT EXISTS idx_pagos_reserva ON public.pagos USING btree (reserva_id);
CREATE INDEX IF NOT EXISTS idx_reservas_cliente ON public.reservas USING btree (cliente_id);
CREATE INDEX IF NOT EXISTS idx_reservas_codigo ON public.reservas USING btree (codigo);
CREATE INDEX IF NOT EXISTS idx_reservas_estado ON public.reservas USING btree (estado);
CREATE INDEX IF NOT EXISTS idx_reservas_servicios_fecha ON public.reservas_servicios USING btree (fecha, hora_inicio);
CREATE INDEX IF NOT EXISTS idx_resumen_pagos_actualizacion ON public.resumen_pagos USING btree (ultima_actualizacion);
CREATE INDEX IF NOT EXISTS idx_resumen_pagos_saldo ON public.resumen_pagos USING btree (saldo_pendiente);
CREATE INDEX IF NOT EXISTShttp://localhost:4200/ idx_servicio_disponibilidad_fecha ON public.servicio_disponibilidad USING btree (fecha, hora_inicio);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON public.usuarios USING btree (email);


-- TRIGGERS
DROP TRIGGER IF EXISTS trigger_actualizar_resumen_estancias ON public.estancias;
CREATE TRIGGER trigger_actualizar_resumen_estancias 
    AFTER INSERT OR DELETE OR UPDATE ON public.estancias 
    FOR EACH ROW EXECUTE FUNCTION public.actualizar_resumen_despues_cambio();

DROP TRIGGER IF EXISTS trigger_actualizar_resumen_pagos ON public.pagos;
CREATE TRIGGER trigger_actualizar_resumen_pagos 
    AFTER INSERT OR DELETE OR UPDATE ON public.pagos 
    FOR EACH ROW EXECUTE FUNCTION public.actualizar_resumen_despues_cambio();

DROP TRIGGER IF EXISTS trigger_actualizar_resumen_reservas ON public.reservas;
CREATE TRIGGER trigger_actualizar_resumen_reservas 
    AFTER INSERT OR DELETE OR UPDATE ON public.reservas 
    FOR EACH ROW EXECUTE FUNCTION public.actualizar_resumen_despues_cambio();

DROP TRIGGER IF EXISTS trigger_actualizar_resumen_reservas_servicios ON public.reservas_servicios;
CREATE TRIGGER trigger_actualizar_resumen_reservas_servicios 
    AFTER INSERT OR DELETE OR UPDATE ON public.reservas_servicios 
    FOR EACH ROW EXECUTE FUNCTION public.actualizar_resumen_despues_cambio();

DROP TRIGGER IF EXISTS trigger_asignar_habitacion ON public.reservas;
CREATE TRIGGER trigger_asignar_habitacion 
    AFTER UPDATE ON public.reservas 
    FOR EACH ROW EXECUTE FUNCTION public.asignar_habitacion_al_confirmar();

-- FOREIGN KEYS
--ALTER TABLE ONLY public.clientes_perfil
--    ADD CONSTRAINT clientes_perfil_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;

--ALTER TABLE ONLY public.empleados_perfil
--    ADD CONSTRAINT empleados_perfil_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;

--ALTER TABLE ONLY public.estancias
--    ADD CONSTRAINT estancias_asignada_por_fk FOREIGN KEY (asignada_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;

--ALTER TABLE ONLY public.estancias
--    ADD CONSTRAINT estancias_checkin_por_fk FOREIGN KEY (checkin_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;

--ALTER TABLE ONLY public.estancias
--    ADD CONSTRAINT estancias_checkout_por_fk FOREIGN KEY (checkout_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;

--ALTER TABLE ONLY public.estancias
--    ADD CONSTRAINT estancias_habitacion_asignada_fkey FOREIGN KEY (habitacion_asignada) REFERENCES public.habitaciones(id);

--ALTER TABLE ONLY public.estancias
--    ADD CONSTRAINT estancias_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id) ON DELETE CASCADE;

--ALTER TABLE ONLY public.estancias
--    ADD CONSTRAINT estancias_tipo_habitacion_id_fkey FOREIGN KEY (tipo_habitacion_id) REFERENCES public.habitaciones_tipos(id);

--ALTER TABLE ONLY public.habitaciones
--    ADD CONSTRAINT habitaciones_tipo_id_fkey FOREIGN KEY (tipo_id) REFERENCES public.habitaciones_tipos(id) ON DELETE CASCADE;

--ALTER TABLE ONLY public.imagenes
--    ADD CONSTRAINT imagenes_servicio_id_fkey FOREIGN KEY (servicio_id) REFERENCES public.servicios(id) ON DELETE CASCADE;

--ALTER TABLE ONLY public.imagenes
--    ADD CONSTRAINT imagenes_tipo_habitacion_id_fkey FOREIGN KEY (tipo_habitacion_id) REFERENCES public.habitaciones_tipos(id) ON DELETE CASCADE;

--ALTER TABLE ONLY public.pagos
--    ADD CONSTRAINT pagos_registrado_por_fk FOREIGN KEY (registrado_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;

--ALTER TABLE ONLY public.pagos
--    ADD CONSTRAINT pagos_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id) ON DELETE CASCADE;

--ALTER TABLE ONLY public.reservas
--    ADD CONSTRAINT reservas_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;

--ALTER TABLE ONLY public.reservas_servicios
--    ADD CONSTRAINT reservas_servicios_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id) ON DELETE CASCADE;

--ALTER TABLE ONLY public.reservas_servicios
--    ADD CONSTRAINT reservas_servicios_servicio_id_fkey FOREIGN KEY (servicio_id) REFERENCES public.servicios(id);

--ALTER TABLE ONLY public.resumen_pagos
--    ADD CONSTRAINT resumen_pagos_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id) ON DELETE CASCADE;

--ALTER TABLE ONLY public.reservas_servicios
--    ADD CONSTRAINT rs_contratado_por_fk FOREIGN KEY (contratado_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;

--ALTER TABLE ONLY public.servicio_disponibilidad
--    ADD CONSTRAINT servicio_disponibilidad_servicio_id_fkey FOREIGN KEY (servicio_id) REFERENCES public.servicios(id) ON DELETE CASCADE;