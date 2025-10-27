--
-- PostgreSQL database dump
--

\restrict 9lojNINrxXzGYa9cmgCJ8R7FyFb5b0mRVkQGB0zXIFfzxhuq4UxJGT7cQPcwdOv

-- Dumped from database version 16.10 (Debian 16.10-1.pgdg13+1)
-- Dumped by pg_dump version 16.10 (Debian 16.10-1.pgdg13+1)

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

--
-- Name: actualizar_resumen_despues_cambio(); Type: FUNCTION; Schema: public; Owner: samiLeMeteAlFront
--

CREATE FUNCTION public.actualizar_resumen_despues_cambio() RETURNS trigger
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

--
-- Name: actualizar_resumen_pagos(character); Type: FUNCTION; Schema: public; Owner: samiLeMeteAlFront
--

CREATE FUNCTION public.actualizar_resumen_pagos(p_reserva_id character) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO resumen_pagos (reserva_id, total_habitaciones, total_servicios, total_reserva, total_pagado, saldo_pendiente, ultima_actualizacion)
    SELECT 
        r.id,
        COALESCE(SUM(e.total_estadia), 0),
        COALESCE(SUM(rs.total_servicio), 0),
        COALESCE(SUM(e.total_estadia), 0) + COALESCE(SUM(rs.total_servicio), 0),
        COALESCE(SUM(p.monto) FILTER (WHERE p.estado = 'completado'), 0),
        (COALESCE(SUM(e.total_estadia), 0) + COALESCE(SUM(rs.total_servicio), 0)) - 
        COALESCE(SUM(p.monto) FILTER (WHERE p.estado = 'completado'), 0),
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


ALTER FUNCTION public.actualizar_resumen_pagos(p_reserva_id character) OWNER TO "samiLeMeteAlFront";

--
-- Name: asignar_habitacion_al_confirmar(); Type: FUNCTION; Schema: public; Owner: samiLeMeteAlFront
--

CREATE FUNCTION public.asignar_habitacion_al_confirmar() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_habitacion_id CHAR(36);
    e_record RECORD;
BEGIN
    IF NEW.estado = 'confirmada' AND OLD.estado != 'confirmada' THEN
        FOR e_record IN 
            SELECT * FROM estancias WHERE reserva_id = NEW.id AND habitacion_asignada IS NULL
        LOOP
            v_habitacion_id := asignar_habitacion_disponible(
                e_record.tipo_habitacion_id, 
                e_record.check_in, 
                e_record.check_out
            );
            
            IF v_habitacion_id IS NOT NULL THEN
                UPDATE estancias
                SET habitacion_asignada = v_habitacion_id
                WHERE id = e_record.id;
            END IF;
        END LOOP;
    END IF;
    
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.asignar_habitacion_al_confirmar() OWNER TO "samiLeMeteAlFront";

--
-- Name: asignar_habitacion_disponible(character, date, date); Type: FUNCTION; Schema: public; Owner: samiLeMeteAlFront
--

CREATE FUNCTION public.asignar_habitacion_disponible(p_tipo_id character, p_check_in date, p_check_out date) RETURNS character
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_habitacion_id CHAR(36);
BEGIN
    SELECT h.id INTO v_habitacion_id
    FROM habitaciones h
    WHERE h.tipo_id = p_tipo_id
    AND h.activa = TRUE
    AND NOT EXISTS (
        SELECT 1
        FROM estancias e
        WHERE e.habitacion_asignada = h.id
        AND NOT (p_check_out <= e.check_in OR p_check_in >= e.check_out)
        AND e.habitacion_asignada IS NOT NULL
    )
    LIMIT 1;
    
    RETURN v_habitacion_id;
END;
$$;


ALTER FUNCTION public.asignar_habitacion_disponible(p_tipo_id character, p_check_in date, p_check_out date) OWNER TO "samiLeMeteAlFront";

--
-- Name: generar_habitaciones_automaticamente(); Type: FUNCTION; Schema: public; Owner: samiLeMeteAlFront
--

CREATE FUNCTION public.generar_habitaciones_automaticamente() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    tipo RECORD;
    i INT;
BEGIN
    FOR tipo IN SELECT * FROM habitaciones_tipos LOOP
        FOR i IN tipo.rango_inicio..tipo.rango_fin LOOP
            INSERT INTO habitaciones (id, tipo_id, numero, activa)
            VALUES (
                'hab_' || i,
                tipo.id,
                i,
                TRUE
            )
            ON CONFLICT (numero) DO NOTHING;
        END LOOP;
    END LOOP;
END;
$$;


ALTER FUNCTION public.generar_habitaciones_automaticamente() OWNER TO "samiLeMeteAlFront";

--
-- Name: inicializar_resumen_pagos(); Type: FUNCTION; Schema: public; Owner: samiLeMeteAlFront
--

CREATE FUNCTION public.inicializar_resumen_pagos() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN SELECT id FROM reservas LOOP
        PERFORM actualizar_resumen_pagos(r.id);
    END LOOP;
END;
$$;


ALTER FUNCTION public.inicializar_resumen_pagos() OWNER TO "samiLeMeteAlFront";

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: clientes_perfil; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.clientes_perfil (
    usuario_id character varying(36) NOT NULL,
    nombre_completo character varying(150) NOT NULL,
    telefono character varying(25),
    fecha_registro timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version bigint
);


ALTER TABLE public.clientes_perfil OWNER TO "samiLeMeteAlFront";

--
-- Name: empleados_perfil; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.empleados_perfil (
    usuario_id character varying(36) NOT NULL,
    nombre_completo character varying(150) NOT NULL,
    telefono character varying(25),
    cargo character varying(100) NOT NULL,
    salario numeric(12,2),
    fecha_contratacion date DEFAULT CURRENT_DATE NOT NULL,
    version bigint
);


ALTER TABLE public.empleados_perfil OWNER TO "samiLeMeteAlFront";

--
-- Name: estancias; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.estancias (
    id character varying(36) NOT NULL,
    reserva_id character varying(36) NOT NULL,
    tipo_habitacion_id character varying(36) NOT NULL,
    check_in boolean DEFAULT false NOT NULL,
    check_out boolean DEFAULT false NOT NULL,
    entrada date NOT NULL,
    salida date NOT NULL,
    numero_huespedes integer NOT NULL,
    precio_por_noche numeric(12,2) NOT NULL,
    total_estadia numeric(12,2) NOT NULL,
    habitacion_asignada character varying(36),
    asignada_por_empleado_id character varying(36),
    checkin_por_empleado_id character varying(36),
    checkout_por_empleado_id character varying(36),
    CONSTRAINT check_fechas_validas CHECK ((salida > entrada)),
    CONSTRAINT check_huespedes_positivo CHECK ((numero_huespedes > 0)),
    CONSTRAINT check_precio_positivo CHECK ((precio_por_noche >= (0)::numeric)),
    CONSTRAINT check_total_positivo CHECK ((total_estadia >= (0)::numeric))
);


ALTER TABLE public.estancias OWNER TO "samiLeMeteAlFront";

--
-- Name: habitaciones; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.habitaciones (
    id character varying(36) NOT NULL,
    tipo_id character varying(36) NOT NULL,
    numero integer NOT NULL,
    activa boolean DEFAULT true NOT NULL,
    CONSTRAINT check_numero_valido CHECK ((numero > 0))
);


ALTER TABLE public.habitaciones OWNER TO "samiLeMeteAlFront";

--
-- Name: habitaciones_tipos; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.habitaciones_tipos (
    id character varying(36) NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion text,
    aforo_maximo integer NOT NULL,
    precio_por_noche numeric(12,2) NOT NULL,
    activa boolean DEFAULT true NOT NULL,
    CONSTRAINT check_aforo_positivo CHECK ((aforo_maximo > 0)),
    CONSTRAINT check_precio_positivo CHECK ((precio_por_noche >= (0)::numeric))
);


ALTER TABLE public.habitaciones_tipos OWNER TO "samiLeMeteAlFront";

--
-- Name: imagenes; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.imagenes (
    id character varying(36) NOT NULL,
    servicio_id character varying(36),
    tipo_habitacion_id character varying(36),
    url character varying(500) NOT NULL,
    CONSTRAINT check_imagen_destino CHECK ((((servicio_id IS NOT NULL) AND (tipo_habitacion_id IS NULL)) OR ((servicio_id IS NULL) AND (tipo_habitacion_id IS NOT NULL))))
);


ALTER TABLE public.imagenes OWNER TO "samiLeMeteAlFront";

--
-- Name: pagos; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.pagos (
    id character varying(36) NOT NULL,
    reserva_id character varying(36) NOT NULL,
    tipo character varying(20) NOT NULL,
    monto numeric(12,2) NOT NULL,
    fecha timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    metodo_pago character varying(50),
    estado character varying(20) DEFAULT 'pendiente'::character varying NOT NULL,
    concepto character varying(200),
    registrado_por_empleado_id character varying(36),
    CONSTRAINT check_estado_pago_valido CHECK (((estado)::text = ANY (ARRAY[('pendiente'::character varying)::text, ('completado'::character varying)::text, ('fallido'::character varying)::text, ('reembolsado'::character varying)::text]))),
    CONSTRAINT check_monto_positivo CHECK ((monto > (0)::numeric)),
    CONSTRAINT check_tipo_valido CHECK (((tipo)::text = ANY (ARRAY[('anticipo'::character varying)::text, ('pago_parcial'::character varying)::text, ('pago_completo'::character varying)::text, ('reembolso'::character varying)::text])))
);


ALTER TABLE public.pagos OWNER TO "samiLeMeteAlFront";

--
-- Name: reservas; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.reservas (
    id character varying(36) NOT NULL,
    codigo character varying(32) NOT NULL,
    cliente_id character varying(36) NOT NULL,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    estado character varying(32) DEFAULT 'pendiente'::character varying NOT NULL,
    notas text,
    creada_por_empleado_id character varying(36),
    CONSTRAINT check_estado_valido CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'CONFIRMADA'::character varying, 'CANCELADA'::character varying, 'COMPLETADA'::character varying])::text[])))
);


ALTER TABLE public.reservas OWNER TO "samiLeMeteAlFront";

--
-- Name: reservas_servicios; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.reservas_servicios (
    id character varying(36) NOT NULL,
    reserva_id character varying(36) NOT NULL,
    servicio_id character varying(36) NOT NULL,
    fecha date NOT NULL,
    hora_inicio time without time zone NOT NULL,
    numero_personas integer NOT NULL,
    precio_por_persona numeric(12,2) NOT NULL,
    total_servicio numeric(12,2) NOT NULL,
    contratado_por_empleado_id character varying(36),
    CONSTRAINT check_personas_positivo CHECK ((numero_personas > 0)),
    CONSTRAINT check_precio_positivo CHECK ((precio_por_persona >= (0)::numeric)),
    CONSTRAINT check_total_positivo CHECK ((total_servicio >= (0)::numeric))
);


ALTER TABLE public.reservas_servicios OWNER TO "samiLeMeteAlFront";

--
-- Name: resumen_pagos; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.resumen_pagos (
    reserva_id character varying(36) NOT NULL,
    total_habitaciones numeric(12,2) DEFAULT 0 NOT NULL,
    total_servicios numeric(12,2) DEFAULT 0 NOT NULL,
    total_reserva numeric(12,2) DEFAULT 0 NOT NULL,
    total_pagado numeric(12,2) DEFAULT 0 NOT NULL,
    saldo_pendiente numeric(12,2) DEFAULT 0 NOT NULL,
    ultima_actualizacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.resumen_pagos OWNER TO "samiLeMeteAlFront";

--
-- Name: servicio_disponibilidad; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.servicio_disponibilidad (
    id character varying(36) NOT NULL,
    servicio_id character varying(36) NOT NULL,
    fecha date NOT NULL,
    hora_inicio time without time zone NOT NULL,
    hora_fin time without time zone NOT NULL,
    capacidad_disponible integer NOT NULL,
    CONSTRAINT check_capacidad_valida CHECK ((capacidad_disponible >= 0)),
    CONSTRAINT check_horas_validas CHECK ((hora_fin > hora_inicio))
);


ALTER TABLE public.servicio_disponibilidad OWNER TO "samiLeMeteAlFront";

--
-- Name: servicios; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.servicios (
    id character varying(36) NOT NULL,
    nombre character varying(120) NOT NULL,
    descripcion text,
    lugar character varying(120) NOT NULL,
    precio_por_persona numeric(12,2) NOT NULL,
    duracion_minutos integer NOT NULL,
    capacidad_maxima integer,
    CONSTRAINT check_precio_positivo CHECK ((precio_por_persona >= (0)::numeric))
);


ALTER TABLE public.servicios OWNER TO "samiLeMeteAlFront";

--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TABLE public.usuarios (
    id character varying(36) NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    rol character varying(50) DEFAULT 'CLIENTE'::character varying NOT NULL,
    CONSTRAINT check_rol_valido CHECK (((rol)::text = ANY (ARRAY[('ADMIN'::character varying)::text, ('CLIENTE'::character varying)::text, ('STAFF'::character varying)::text, ('RECEPCIONISTA'::character varying)::text])))
);


ALTER TABLE public.usuarios OWNER TO "samiLeMeteAlFront";

--
-- Data for Name: clientes_perfil; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.clientes_perfil (usuario_id, nombre_completo, telefono, fecha_registro, version) FROM stdin;
7325d128-3b86-40fb-865c-4584b25cb2f9	Aguirre	234567	2025-09-08 22:51:27.58619	\N
60fc894b-e09a-4265-b436-df53a490e2eb	samuel campos	321	2025-09-09 10:03:56.095483	\N
0bf298eb-bc81-4938-ad2c-b7f96a13a105	nuevo	3002112575	2025-09-09 10:57:58.800666	\N
cli_001	Carlos Pérez	3000000000	2025-10-07 10:41:37.742121	\N
ce1cd71a-5072-42ae-aab9-8a7ceedb30ad	admn la	444444444	2025-10-18 18:48:52.40404	0
03ce8cdd-3984-401c-a170-c361ea83e032	prueba pro		2025-10-20 12:16:05.090422	0
a40d9921-497b-4ca6-95ed-b43f872f1af6	andd landdd	444455	2025-10-18 18:45:52.940187	2
ffe512fa-35b6-4d28-a85a-efec7531f072	juan modificado	3002112575	2025-10-23 10:09:44.449419	1
\.


--
-- Data for Name: empleados_perfil; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.empleados_perfil (usuario_id, nombre_completo, telefono, cargo, salario, fecha_contratacion, version) FROM stdin;
emp_001	Mariana Rico	3001234567	Recepcionista	1800000.00	2022-05-10	\N
emp_002	Hania Campos	3019876543	Recepcionista	1750000.00	2023-01-15	\N
emp_003	Santiago Fortich	3204567890	Chef Ejecutivo	2800000.00	2021-08-20	\N
emp_004	Andrés Gómez	3107654321	Camarero	1500000.00	2022-11-01	\N
emp_005	Karen Manuela	3152345678	Camarera	1500000.00	2023-06-12	\N
emp_006	Paola Aguilera	3186543210	Sous Chef	2300000.00	2023-09-01	\N
emp_007	Sebastian Angarita	3205558899	Gerente General	4200000.00	2020-03-05	\N
emp_008	Camilo Triana	3114455667	Mantenimiento	1600000.00	2022-02-17	\N
958b1f5f-af33-462e-93a3-914efe625ffe	Juan Perez	12345656	RECEPCIONISTA	0.00	2025-10-19	0
\.


--
-- Data for Name: estancias; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.estancias (id, reserva_id, tipo_habitacion_id, check_in, check_out, entrada, salida, numero_huespedes, precio_por_noche, total_estadia, habitacion_asignada, asignada_por_empleado_id, checkin_por_empleado_id, checkout_por_empleado_id) FROM stdin;
est_0001	res_0001	tipo_normal	f	f	2025-10-10	2025-10-13	2	250000.00	750000.00	hab_101	emp_001	\N	\N
25abb754-e46e-4afd-95ea-7467ed159047	d5ecacd9-7b14-4e61-82da-a1835e704ee7	tipo_vip	f	f	2025-10-22	2025-11-05	1	520000.00	7280000.00	hab_102	\N	\N	\N
182fb8dd-6c09-400d-aed3-68eb179c9a47	abdf10dc-8094-400c-8dec-c69bdfdaf79d	tipo_luxury	f	f	2025-11-18	2025-11-21	3	700000.00	2100000.00	hab_501	\N	\N	\N
9632ed30-a47c-41c4-aa2e-18f9714402d5	27231d95-b8da-49dd-ba56-fd32847cd75f	tipo_normal	f	f	2025-10-30	2025-11-06	2	250000.00	1750000.00	hab_101	\N	\N	\N
d601ee3e-6c0f-4e03-8603-7b85cedd4683	e8fddbd5-3570-4f34-b6b3-7501ffe3383f	1a5fc0a9-1039-4318-8d24-b15fa8a4e5d3	f	f	2025-10-30	2025-11-06	1	123.00	861.00	a8775bc3-dfd4-42e7-b08f-b622c3c7587c	\N	\N	\N
3e9cc941-5165-4c19-89c7-05036cd47f97	68533335-007e-4b68-a6dd-539d06174579	tipo_executive	f	f	2025-10-27	2025-10-31	1	380000.00	1520000.00	hab_201	\N	\N	\N
ba1b08d7-1efb-4fb2-854c-84dc05c7d8a4	03f980c1-7ae6-422b-9e3c-37fcbdae1431	889fda32-a9e6-4c85-84f4-c7f25c8e8894	f	f	2025-10-27	2025-10-31	10	1000.00	4000.00	22381da8-4361-46e5-82bb-dc052df4cb80	\N	\N	\N
61d6da32-f3f0-4b4b-86c9-e87083408ae2	43b330b5-3c38-43c2-aca8-7f94bd495989	889fda32-a9e6-4c85-84f4-c7f25c8e8894	f	f	2025-11-03	2025-11-05	8	1000.00	2000.00	22381da8-4361-46e5-82bb-dc052df4cb80	\N	\N	\N
485d4044-ad87-4d33-9b8e-7b948f867101	06289157-f5ea-4009-9e85-771c889b14c8	tipo_connecting	f	f	2025-11-11	2025-11-15	1	600000.00	2400000.00	hab_501	\N	\N	\N
\.


--
-- Data for Name: habitaciones; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.habitaciones (id, tipo_id, numero, activa) FROM stdin;
hab_103	tipo_normal	103	t
hab_104	tipo_normal	104	t
hab_105	tipo_normal	105	t
hab_106	tipo_normal	106	t
hab_107	tipo_normal	107	t
hab_108	tipo_normal	108	t
hab_109	tipo_normal	109	t
hab_110	tipo_normal	110	t
hab_201	tipo_executive	201	t
hab_202	tipo_executive	202	t
hab_203	tipo_executive	203	t
hab_204	tipo_executive	204	t
hab_205	tipo_executive	205	t
hab_206	tipo_executive	206	t
hab_207	tipo_executive	207	t
hab_208	tipo_executive	208	t
hab_209	tipo_executive	209	t
hab_210	tipo_executive	210	t
hab_301	tipo_vip	301	t
hab_302	tipo_vip	302	t
hab_303	tipo_vip	303	t
hab_304	tipo_vip	304	t
hab_305	tipo_vip	305	t
hab_306	tipo_vip	306	t
hab_307	tipo_vip	307	t
hab_308	tipo_vip	308	t
hab_309	tipo_vip	309	t
hab_310	tipo_vip	310	t
hab_401	tipo_luxury	401	t
hab_402	tipo_luxury	402	t
hab_403	tipo_luxury	403	t
hab_404	tipo_luxury	404	t
hab_405	tipo_luxury	405	t
hab_406	tipo_luxury	406	t
hab_407	tipo_luxury	407	t
hab_408	tipo_luxury	408	t
hab_409	tipo_luxury	409	t
hab_410	tipo_luxury	410	t
hab_501	tipo_connecting	501	t
hab_502	tipo_connecting	502	t
hab_503	tipo_connecting	503	t
hab_504	tipo_connecting	504	t
hab_505	tipo_connecting	505	t
hab_506	tipo_connecting	506	t
hab_507	tipo_connecting	507	t
hab_508	tipo_connecting	508	t
hab_509	tipo_connecting	509	t
hab_510	tipo_connecting	510	t
8b22d527-1188-4d94-9d9e-7d741c3498e2	tipo_luxury	1001	t
hab_102	tipo_vip	102	t
hab_101	tipo_normal	101	t
a8775bc3-dfd4-42e7-b08f-b622c3c7587c	1a5fc0a9-1039-4318-8d24-b15fa8a4e5d3	999	t
22381da8-4361-46e5-82bb-dc052df4cb80	889fda32-a9e6-4c85-84f4-c7f25c8e8894	999111	t
\.


--
-- Data for Name: habitaciones_tipos; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.habitaciones_tipos (id, nombre, descripcion, aforo_maximo, precio_por_noche, activa) FROM stdin;
tipo_executive	Executive	Habitación con espacio adicional y escritorio de trabajo. Ideal para viajes de negocios.	2	380000.00	t
tipo_vip	VIP	Suite con amenidades premium y vista privilegiada. Incluye acceso a lounge VIP.	3	520000.00	t
tipo_luxury	Luxury	Suite de máximo confort con sala privada y terraza. Experiencia de lujo con servicios personalizados.	4	700000.00	t
tipo_connecting	Connecting	Habitaciones interconectadas perfectas para grupos familiares o que viajan juntos.	4	600000.00	t
tipo_normal	Normallll	Habitación funcional y cómoda para estancias cortas. Incluye amenities básicos y Wi-Fi gratuito.	2	250000.00	t
1a5fc0a9-1039-4318-8d24-b15fa8a4e5d3	nuevo	123	1	123.00	t
889fda32-a9e6-4c85-84f4-c7f25c8e8894	prueba	1	10	1000.00	t
\.


--
-- Data for Name: imagenes; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.imagenes (id, servicio_id, tipo_habitacion_id, url) FROM stdin;
img_normal_1	\N	tipo_normal	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fsuite_normal.jpg?alt=media&token=028bd170-dcb7-4e49-a08b-55c834083ff3
img_normal_2	\N	tipo_normal	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fba%C3%B1o_normal.jpg?alt=media&token=bcbadfca-0e07-482b-a084-94b0611dd94d
img_executive_1	\N	tipo_executive	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fhabitacion_ejecutiva_cama.jpg?alt=media&token=441f9315-8097-4533-aac5-e830f76953f2
img_executive_2	\N	tipo_executive	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fsuite_ejecutiva.jpg?alt=media&token=5d108710-8492-47b2-8773-ba0527dae9a7
img_vip_1	\N	tipo_vip	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fhabitacion_lujo.jpg?alt=media&token=e613caac-19de-457e-948a-f47690690877
img_vip_2	\N	tipo_vip	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fsuite-lujo.jpg?alt=media&token=19adcd3a-1327-402a-b145-6418b5d04316
img_luxury_1	\N	tipo_luxury	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fsuite_vip.jpg?alt=media&token=5b69a74e-85b5-40eb-8956-84cc3e6e0343
img_luxury_2	\N	tipo_luxury	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fsala_luxury.jpg?alt=media&token=c0b4544e-4f82-4c48-a22f-66ba7bcdb3a7
img_spa_1	servicio_spa	\N	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fspa_2.jpg?alt=media&token=8a4ef544-440c-4e34-90e1-3f9bc2511b8f
img_spa_2	servicio_spa	\N	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fspa_3.jpg?alt=media&token=7c2dc267-2927-4b32-8164-ce7b93f9b3bf
img_trekking_1	servicio_trekking	\N	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Ftrecking.jpg?alt=media&token=0a5ca1f7-d034-4d91-a312-3715537a8eaa
img_trekking_2	servicio_trekking	\N	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Ftrecking_2.jpeg?alt=media&token=edccf3d9-d809-4f0b-848c-a503adc1c233
img_trans_1	servicio_transporte	\N	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Ftransporte_2.jpg?alt=media&token=01a46ff9-6aa6-4be2-92d4-eaaccbf2d765
img_trans_2	servicio_transporte	\N	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Ftransporte_3.jpg?alt=media&token=3b80c248-104a-4792-bb0b-a105218889fb
img_wifi_1	servicio_wifi	\N	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fcatacafe.jpg?alt=media&token=63d843d1-2633-4148-b46c-8a02cf4cad9a
img_wifi_2	servicio_wifi	\N	https://firebasestorage.googleapis.com/v0/b/lifsecback-0.firebasestorage.app/o/aponia_images%2Fwifi_hotel2.jpg?alt=media&token=196af0e0-1e9d-4cdc-b2f9-9cb9d2d94242
img_connecting_1	\N	tipo_connecting	https://www.hilton.com/im/en/NoHotel/15621541/1252-corp-connecting-rooms-ohw-room.jpg?impolicy=crop&cw=4500&ch=1250&gravity=NorthWest&xposition=0&yposition=875&rw=3840&rh=1068
img_connecting_2	\N	tipo_connecting	https://img.lavdg.com/sc/7KboYBlOqMPLCeg3EDsGYC8ZaIo=/768x/2020/07/17/00121594983853338592141/Foto/pazo4.jpg
\.


--
-- Data for Name: pagos; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.pagos (id, reserva_id, tipo, monto, fecha, metodo_pago, estado, concepto, registrado_por_empleado_id) FROM stdin;
pay_0001	res_0001	anticipo	500000.00	2025-10-07 10:41:37.742121	TARJETA	pendiente	Abono inicial (pendiente por confirmar)	emp_001
\.


--
-- Data for Name: reservas; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.reservas (id, codigo, cliente_id, fecha_creacion, estado, notas, creada_por_empleado_id) FROM stdin;
res_0001	R-2025-0001	cli_001	2025-10-07 10:41:37.742121	CONFIRMADA	Reserva de 3 noches, tipo_normal	emp_001
d5ecacd9-7b14-4e61-82da-a1835e704ee7	RES-A68A5DFF	a40d9921-497b-4ca6-95ed-b43f872f1af6	2025-10-21 01:15:32.14299	CANCELADA	si	\N
abdf10dc-8094-400c-8dec-c69bdfdaf79d	RES-E6CEC087	60fc894b-e09a-4265-b436-df53a490e2eb	2025-10-21 01:48:14.358167	CONFIRMADA	play	\N
27231d95-b8da-49dd-ba56-fd32847cd75f	RES-52C1B6C8	60fc894b-e09a-4265-b436-df53a490e2eb	2025-10-21 09:32:30.405483	CONFIRMADA	nueva	\N
e8fddbd5-3570-4f34-b6b3-7501ffe3383f	RES-7F6A9D77	60fc894b-e09a-4265-b436-df53a490e2eb	2025-10-23 10:07:29.146572	CONFIRMADA		\N
68533335-007e-4b68-a6dd-539d06174579	RES-84D43099	ffe512fa-35b6-4d28-a85a-efec7531f072	2025-10-23 10:10:30.11268	CONFIRMADA	ok	\N
43b330b5-3c38-43c2-aca8-7f94bd495989	RES-0E774453	ffe512fa-35b6-4d28-a85a-efec7531f072	2025-10-23 10:15:43.712513	CONFIRMADA	mm	\N
03f980c1-7ae6-422b-9e3c-37fcbdae1431	RES-02B3F94D	ffe512fa-35b6-4d28-a85a-efec7531f072	2025-10-23 10:14:34.468672	CANCELADA	a	\N
06289157-f5ea-4009-9e85-771c889b14c8	RES-6C4AE71B	a40d9921-497b-4ca6-95ed-b43f872f1af6	2025-10-27 13:21:54.530827	CONFIRMADA		\N
\.


--
-- Data for Name: reservas_servicios; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.reservas_servicios (id, reserva_id, servicio_id, fecha, hora_inicio, numero_personas, precio_por_persona, total_servicio, contratado_por_empleado_id) FROM stdin;
rs_0001	res_0001	servicio_spa	2025-10-11	09:00:00	2	150000.00	300000.00	emp_002
rs_0002	res_0001	servicio_transporte	2025-10-10	07:00:00	2	100000.00	200000.00	emp_002
9c8335e4-2cc8-4030-9363-f1a0d88a380c	abdf10dc-8094-400c-8dec-c69bdfdaf79d	servicio_trekking	2025-11-19	14:00:00	1	200000.00	200000.00	\N
f7cd4705-cbf5-46a9-b83c-148f729cd8f6	abdf10dc-8094-400c-8dec-c69bdfdaf79d	servicio_transporte	2025-11-18	17:00:00	1	100000.00	100000.00	\N
b2c67e60-7347-4877-9ba2-4e70966119d0	abdf10dc-8094-400c-8dec-c69bdfdaf79d	servicio_trekking	2025-10-26	10:11:00	9	200000.00	1800000.00	\N
73652c8a-4982-49a9-8fcd-e9639e9f203c	43b330b5-3c38-43c2-aca8-7f94bd495989	servicio_trekking	2025-10-27	10:10:00	1	200000.00	200000.00	\N
24795ae9-c71b-4f20-91a5-2f4fbfcb7764	43b330b5-3c38-43c2-aca8-7f94bd495989	servicio_transporte	2025-10-28	10:10:00	1	100000.00	100000.00	\N
e432d30f-ee3b-4225-ad0b-fc2e6c15188d	43b330b5-3c38-43c2-aca8-7f94bd495989	3c4c6e2b-caae-45bb-b312-f943e83a4a8a	2025-10-29	11:10:00	1	1.00	1.00	\N
\.


--
-- Data for Name: resumen_pagos; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.resumen_pagos (reserva_id, total_habitaciones, total_servicios, total_reserva, total_pagado, saldo_pendiente, ultima_actualizacion) FROM stdin;
res_0001	1500000.00	500000.00	2000000.00	0.00	2000000.00	2025-10-21 04:03:31.080167
d5ecacd9-7b14-4e61-82da-a1835e704ee7	7280000.00	0.00	7280000.00	0.00	7280000.00	2025-10-21 01:43:21.85558
27231d95-b8da-49dd-ba56-fd32847cd75f	1750000.00	0.00	1750000.00	0.00	1750000.00	2025-10-21 09:32:30.248826
e8fddbd5-3570-4f34-b6b3-7501ffe3383f	861.00	0.00	861.00	0.00	861.00	2025-10-23 10:07:28.471317
68533335-007e-4b68-a6dd-539d06174579	1520000.00	0.00	1520000.00	0.00	1520000.00	2025-10-23 10:10:28.42996
abdf10dc-8094-400c-8dec-c69bdfdaf79d	6300000.00	2100000.00	8400000.00	0.00	8400000.00	2025-10-23 10:18:53.110413
03f980c1-7ae6-422b-9e3c-37fcbdae1431	4000.00	0.00	4000.00	0.00	4000.00	2025-10-23 10:19:08.684401
43b330b5-3c38-43c2-aca8-7f94bd495989	6000.00	300001.00	306001.00	0.00	306001.00	2025-10-23 10:22:47.030493
06289157-f5ea-4009-9e85-771c889b14c8	2400000.00	0.00	2400000.00	0.00	2400000.00	2025-10-27 13:21:54.402723
\.


--
-- Data for Name: servicio_disponibilidad; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.servicio_disponibilidad (id, servicio_id, fecha, hora_inicio, hora_fin, capacidad_disponible) FROM stdin;
disp_spa_1	servicio_spa	2025-09-01	09:00:00	21:00:00	10
disp_spa_2	servicio_spa	2025-09-02	09:00:00	21:00:00	10
disp_trekking_1	servicio_trekking	2025-09-01	05:00:00	10:00:00	10
disp_trekking_2	servicio_trekking	2025-09-02	05:00:00	10:00:00	10
disp_trans_1	servicio_transporte	2025-09-01	00:00:00	23:59:59	3
disp_trans_2	servicio_transporte	2025-09-02	00:00:00	23:59:59	3
disp_wifi_1	servicio_wifi	2025-09-01	00:00:00	23:59:59	100
disp_wifi_2	servicio_wifi	2025-09-02	00:00:00	23:59:59	100
\.


--
-- Data for Name: servicios; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.servicios (id, nombre, descripcion, lugar, precio_por_persona, duracion_minutos, capacidad_maxima) FROM stdin;
servicio_trekking	Trekking al amanecer	Panorámicas únicas y picnic gourmet. Caminatas ecológicas guiadas para disfrutar de la naturaleza al amanecer.	Punto de encuentro en el lobby del hotel	200000.00	240	10
servicio_transporte	Transporte Privado	Traslados al aeropuerto y movilidad en vehículos de lujo con chofer privado disponible las 24 horas, garantizando seguridad y confort.	Punto de encuentro en el lobby	100000.00	0	3
servicio_wifi	Wi-Fi & Cowork	Espacio premium de Wi-Fi y coworking, con conexión ultrarrápida y ambiente inspirador, disponible 24/7 para maximizar tu productividad.	Sala de eventos 3, 4 y 5	0.00	0	100
servicio_spa	Spa de autor modi	Rituales con esencias locales, 60-120 min. Masajes, sauna y rituales holísticos que combinan técnicas ancestrales y modernas para revitalizar tu cuerpo y mente en un ambiente de serenidad absoluta.	Mantra SPA	150000.00	90	10
152d903c-55e6-41ce-b47e-f652cc95f0be	nuevo	nuevo	nuevo	1000.00	60	2
string	string	string	string	0.00	0	0
3c4c6e2b-caae-45bb-b312-f943e83a4a8a	nuevo servicio	a	bogota	1.00	30	10
\.


--
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: samiLeMeteAlFront
--

COPY public.usuarios (id, email, password_hash, rol) FROM stdin;
7325d128-3b86-40fb-865c-4584b25cb2f9	juanfe@dropsy.com	1234	CLIENTE
4564fe1b-2dd7-4e56-8b31-8f88a62fd632	angarita@lemeteaweb.puj	1234	ADMIN
60fc894b-e09a-4265-b436-df53a490e2eb	samu@gmail.com	1234	CLIENTE
0bf298eb-bc81-4938-ad2c-b7f96a13a105	nuevo@gmail.com	123	CLIENTE
emp_003	chef1@hotel.com	1234	STAFF
emp_004	chef2@hotel.com	1234	STAFF
emp_005	camarera1@hotel.com	1234	STAFF
emp_006	camarera2@hotel.com	1234	STAFF
emp_007	gerente@hotel.com	1234	ADMIN
emp_008	mantenimiento1@hotel.com	1234	STAFF
cli_001	cliente1@hotel.com	1234	CLIENTE
ce1cd71a-5072-42ae-aab9-8a7ceedb30ad	andreadmin@gmail.com	1234	ADMIN
958b1f5f-af33-462e-93a3-914efe625ffe	recepcion3@hotel.com	$2a$10$QY8AFe2k4oeCb6jlW8HYju5NMXc9oouXwkrahfVi8flvTSx/ADeYO	RECEPCIONISTA
emp_002	recepcion2@hotel.com	1234	RECEPCIONISTA
emp_001	recepcion1@hotel.com	1234	RECEPCIONISTA
03ce8cdd-3984-401c-a170-c361ea83e032	prueba@hotel.com	1234	ADMIN
a40d9921-497b-4ca6-95ed-b43f872f1af6	andre@gmail.com	1234	CLIENTE
ffe512fa-35b6-4d28-a85a-efec7531f072	angarita@gmail.com	123123	CLIENTE
\.


--
-- Name: clientes_perfil clientes_perfil_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.clientes_perfil
    ADD CONSTRAINT clientes_perfil_pkey PRIMARY KEY (usuario_id);


--
-- Name: empleados_perfil empleados_perfil_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.empleados_perfil
    ADD CONSTRAINT empleados_perfil_pkey PRIMARY KEY (usuario_id);


--
-- Name: estancias estancias_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.estancias
    ADD CONSTRAINT estancias_pkey PRIMARY KEY (id);


--
-- Name: habitaciones habitaciones_numero_key; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.habitaciones
    ADD CONSTRAINT habitaciones_numero_key UNIQUE (numero);


--
-- Name: habitaciones habitaciones_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.habitaciones
    ADD CONSTRAINT habitaciones_pkey PRIMARY KEY (id);


--
-- Name: habitaciones_tipos habitaciones_tipos_nombre_key; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.habitaciones_tipos
    ADD CONSTRAINT habitaciones_tipos_nombre_key UNIQUE (nombre);


--
-- Name: habitaciones_tipos habitaciones_tipos_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.habitaciones_tipos
    ADD CONSTRAINT habitaciones_tipos_pkey PRIMARY KEY (id);


--
-- Name: imagenes imagenes_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.imagenes
    ADD CONSTRAINT imagenes_pkey PRIMARY KEY (id);


--
-- Name: pagos pagos_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT pagos_pkey PRIMARY KEY (id);


--
-- Name: reservas reservas_codigo_key; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.reservas
    ADD CONSTRAINT reservas_codigo_key UNIQUE (codigo);


--
-- Name: reservas reservas_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.reservas
    ADD CONSTRAINT reservas_pkey PRIMARY KEY (id);


--
-- Name: reservas_servicios reservas_servicios_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.reservas_servicios
    ADD CONSTRAINT reservas_servicios_pkey PRIMARY KEY (id);


--
-- Name: resumen_pagos resumen_pagos_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.resumen_pagos
    ADD CONSTRAINT resumen_pagos_pkey PRIMARY KEY (reserva_id);


--
-- Name: servicio_disponibilidad servicio_disponibilidad_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.servicio_disponibilidad
    ADD CONSTRAINT servicio_disponibilidad_pkey PRIMARY KEY (id);


--
-- Name: servicio_disponibilidad servicio_disponibilidad_servicio_id_fecha_hora_inicio_key; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.servicio_disponibilidad
    ADD CONSTRAINT servicio_disponibilidad_servicio_id_fecha_hora_inicio_key UNIQUE (servicio_id, fecha, hora_inicio);


--
-- Name: servicios servicios_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.servicios
    ADD CONSTRAINT servicios_pkey PRIMARY KEY (id);


--
-- Name: servicio_disponibilidad ukod1sk9uetqx1o4lywc5yvfqh8; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.servicio_disponibilidad
    ADD CONSTRAINT ukod1sk9uetqx1o4lywc5yvfqh8 UNIQUE (servicio_id, fecha, hora_inicio);


--
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: idx_estancias_asignada_por; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_estancias_asignada_por ON public.estancias USING btree (asignada_por_empleado_id);


--
-- Name: idx_estancias_checkin_por; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_estancias_checkin_por ON public.estancias USING btree (checkin_por_empleado_id);


--
-- Name: idx_estancias_checkout_por; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_estancias_checkout_por ON public.estancias USING btree (checkout_por_empleado_id);


--
-- Name: idx_estancias_fechas; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_estancias_fechas ON public.estancias USING btree (check_in, check_out);


--
-- Name: idx_estancias_tipo; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_estancias_tipo ON public.estancias USING btree (tipo_habitacion_id);


--
-- Name: idx_habitaciones_numero; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_habitaciones_numero ON public.habitaciones USING btree (numero);


--
-- Name: idx_habitaciones_tipo; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_habitaciones_tipo ON public.habitaciones USING btree (tipo_id);


--
-- Name: idx_pagos_estado; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_pagos_estado ON public.pagos USING btree (estado);


--
-- Name: idx_pagos_registrado_por; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_pagos_registrado_por ON public.pagos USING btree (registrado_por_empleado_id);


--
-- Name: idx_pagos_reserva; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_pagos_reserva ON public.pagos USING btree (reserva_id);


--
-- Name: idx_reservas_cliente; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_reservas_cliente ON public.reservas USING btree (cliente_id);


--
-- Name: idx_reservas_codigo; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_reservas_codigo ON public.reservas USING btree (codigo);


--
-- Name: idx_reservas_creada_por; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_reservas_creada_por ON public.reservas USING btree (creada_por_empleado_id);


--
-- Name: idx_reservas_estado; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_reservas_estado ON public.reservas USING btree (estado);


--
-- Name: idx_reservas_servicios_fecha; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_reservas_servicios_fecha ON public.reservas_servicios USING btree (fecha, hora_inicio);


--
-- Name: idx_resumen_pagos_actualizacion; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_resumen_pagos_actualizacion ON public.resumen_pagos USING btree (ultima_actualizacion);


--
-- Name: idx_resumen_pagos_saldo; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_resumen_pagos_saldo ON public.resumen_pagos USING btree (saldo_pendiente);


--
-- Name: idx_rs_contratado_por; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_rs_contratado_por ON public.reservas_servicios USING btree (contratado_por_empleado_id);


--
-- Name: idx_servicio_disponibilidad_fecha; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_servicio_disponibilidad_fecha ON public.servicio_disponibilidad USING btree (fecha, hora_inicio);


--
-- Name: idx_usuarios_email; Type: INDEX; Schema: public; Owner: samiLeMeteAlFront
--

CREATE INDEX idx_usuarios_email ON public.usuarios USING btree (email);


--
-- Name: estancias trigger_actualizar_resumen_estancias; Type: TRIGGER; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TRIGGER trigger_actualizar_resumen_estancias AFTER INSERT OR DELETE OR UPDATE ON public.estancias FOR EACH ROW EXECUTE FUNCTION public.actualizar_resumen_despues_cambio();


--
-- Name: pagos trigger_actualizar_resumen_pagos; Type: TRIGGER; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TRIGGER trigger_actualizar_resumen_pagos AFTER INSERT OR DELETE OR UPDATE ON public.pagos FOR EACH ROW EXECUTE FUNCTION public.actualizar_resumen_despues_cambio();


--
-- Name: reservas trigger_actualizar_resumen_reservas; Type: TRIGGER; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TRIGGER trigger_actualizar_resumen_reservas AFTER INSERT OR DELETE OR UPDATE ON public.reservas FOR EACH ROW EXECUTE FUNCTION public.actualizar_resumen_despues_cambio();


--
-- Name: reservas_servicios trigger_actualizar_resumen_reservas_servicios; Type: TRIGGER; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TRIGGER trigger_actualizar_resumen_reservas_servicios AFTER INSERT OR DELETE OR UPDATE ON public.reservas_servicios FOR EACH ROW EXECUTE FUNCTION public.actualizar_resumen_despues_cambio();


--
-- Name: reservas trigger_asignar_habitacion; Type: TRIGGER; Schema: public; Owner: samiLeMeteAlFront
--

CREATE TRIGGER trigger_asignar_habitacion AFTER UPDATE ON public.reservas FOR EACH ROW EXECUTE FUNCTION public.asignar_habitacion_al_confirmar();


--
-- Name: clientes_perfil clientes_perfil_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.clientes_perfil
    ADD CONSTRAINT clientes_perfil_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- Name: empleados_perfil empleados_perfil_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.empleados_perfil
    ADD CONSTRAINT empleados_perfil_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- Name: estancias estancias_asignada_por_fk; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.estancias
    ADD CONSTRAINT estancias_asignada_por_fk FOREIGN KEY (asignada_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: estancias estancias_checkin_por_fk; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.estancias
    ADD CONSTRAINT estancias_checkin_por_fk FOREIGN KEY (checkin_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: estancias estancias_checkout_por_fk; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.estancias
    ADD CONSTRAINT estancias_checkout_por_fk FOREIGN KEY (checkout_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: estancias estancias_habitacion_asignada_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.estancias
    ADD CONSTRAINT estancias_habitacion_asignada_fkey FOREIGN KEY (habitacion_asignada) REFERENCES public.habitaciones(id);


--
-- Name: estancias estancias_reserva_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.estancias
    ADD CONSTRAINT estancias_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id) ON DELETE CASCADE;


--
-- Name: estancias estancias_tipo_habitacion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.estancias
    ADD CONSTRAINT estancias_tipo_habitacion_id_fkey FOREIGN KEY (tipo_habitacion_id) REFERENCES public.habitaciones_tipos(id);


--
-- Name: habitaciones habitaciones_tipo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.habitaciones
    ADD CONSTRAINT habitaciones_tipo_id_fkey FOREIGN KEY (tipo_id) REFERENCES public.habitaciones_tipos(id) ON DELETE CASCADE;


--
-- Name: imagenes imagenes_servicio_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.imagenes
    ADD CONSTRAINT imagenes_servicio_id_fkey FOREIGN KEY (servicio_id) REFERENCES public.servicios(id) ON DELETE CASCADE;


--
-- Name: imagenes imagenes_tipo_habitacion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.imagenes
    ADD CONSTRAINT imagenes_tipo_habitacion_id_fkey FOREIGN KEY (tipo_habitacion_id) REFERENCES public.habitaciones_tipos(id) ON DELETE CASCADE;


--
-- Name: pagos pagos_registrado_por_fk; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT pagos_registrado_por_fk FOREIGN KEY (registrado_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: pagos pagos_reserva_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT pagos_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id) ON DELETE CASCADE;


--
-- Name: reservas reservas_cliente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.reservas
    ADD CONSTRAINT reservas_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES public.usuarios(id) ON DELETE CASCADE;


--
-- Name: reservas reservas_creada_por_empleado_fk; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.reservas
    ADD CONSTRAINT reservas_creada_por_empleado_fk FOREIGN KEY (creada_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: reservas_servicios reservas_servicios_reserva_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.reservas_servicios
    ADD CONSTRAINT reservas_servicios_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id) ON DELETE CASCADE;


--
-- Name: reservas_servicios reservas_servicios_servicio_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.reservas_servicios
    ADD CONSTRAINT reservas_servicios_servicio_id_fkey FOREIGN KEY (servicio_id) REFERENCES public.servicios(id);


--
-- Name: resumen_pagos resumen_pagos_reserva_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.resumen_pagos
    ADD CONSTRAINT resumen_pagos_reserva_id_fkey FOREIGN KEY (reserva_id) REFERENCES public.reservas(id) ON DELETE CASCADE;


--
-- Name: reservas_servicios rs_contratado_por_fk; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.reservas_servicios
    ADD CONSTRAINT rs_contratado_por_fk FOREIGN KEY (contratado_por_empleado_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: servicio_disponibilidad servicio_disponibilidad_servicio_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: samiLeMeteAlFront
--

ALTER TABLE ONLY public.servicio_disponibilidad
    ADD CONSTRAINT servicio_disponibilidad_servicio_id_fkey FOREIGN KEY (servicio_id) REFERENCES public.servicios(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

\unrestrict 9lojNINrxXzGYa9cmgCJ8R7FyFb5b0mRVkQGB0zXIFfzxhuq4UxJGT7cQPcwdOv

