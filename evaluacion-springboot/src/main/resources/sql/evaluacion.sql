-- =====================================================================
-- Base de datos: evaluacion
-- Sistema de Gestión de Usuarios y Solicitudes (Spring Boot + JPA)
-- Motor: MySQL 8.0.16 o superior (necesario para que se apliquen los CHECK)
--
-- Las tablas coinciden con el mapeo JPA de las entidades:
--   usuarios     -> Usuario (abstracta), Administrador, Empleado, Cliente
--                   (herencia SINGLE_TABLE: una sola tabla con columna "tipo")
--   solicitudes  -> Solicitud (relación: un Cliente tiene muchas Solicitudes)
--
-- Uso: abrir en MySQL Workbench y ejecutar todo el script (rayo ⚡),
--      o desde consola:  mysql -u root -p < evaluacion.sql
-- =====================================================================

DROP DATABASE IF EXISTS evaluacion;
CREATE DATABASE evaluacion
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE evaluacion;

-- ==========================================
-- 1. TABLA DE USUARIOS (herencia SINGLE_TABLE)
-- ==========================================
-- La columna "tipo" es el discriminador: indica si la fila es un
-- ADMINISTRADOR, un EMPLEADO o un CLIENTE.
-- Las columnas propias de cada rol aceptan NULL porque, por ejemplo,
-- un cliente no tiene salario ni un empleado tiene puntos de fidelidad.
CREATE TABLE usuarios (
    id               INT          NOT NULL AUTO_INCREMENT,
    tipo             VARCHAR(20)  NOT NULL,
    nombre           VARCHAR(100) NOT NULL,
    correo           VARCHAR(120) NOT NULL,

    -- Administrador
    nivel_acceso     INT          NULL,

    -- Empleado
    cargo            VARCHAR(80)  NULL,
    salario          DOUBLE       NULL,

    -- Cliente
    puntos_fidelidad INT          NULL,

    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_correo UNIQUE (correo),

    -- Validaciones también a nivel de base de datos
    CONSTRAINT ck_usuarios_tipo
        CHECK (tipo IN ('ADMINISTRADOR', 'EMPLEADO', 'CLIENTE')),
    CONSTRAINT ck_usuarios_nivel_acceso
        CHECK (nivel_acceso IS NULL OR nivel_acceso BETWEEN 1 AND 5),
    CONSTRAINT ck_usuarios_salario
        CHECK (salario IS NULL OR salario > 0),
    CONSTRAINT ck_usuarios_puntos
        CHECK (puntos_fidelidad IS NULL OR puntos_fidelidad >= 0),

    -- Cada rol debe tener sus datos obligatorios
    CONSTRAINT ck_usuarios_datos_admin
        CHECK (tipo <> 'ADMINISTRADOR' OR nivel_acceso IS NOT NULL),
    CONSTRAINT ck_usuarios_datos_empleado
        CHECK (tipo <> 'EMPLEADO' OR (cargo IS NOT NULL AND salario IS NOT NULL))
) ENGINE = InnoDB;

-- ==========================================
-- 2. TABLA DE SOLICITUDES
-- ==========================================
-- cliente_id  -> llave foránea al cliente que creó la solicitud (obligatoria)
-- empleado_id -> llave foránea al empleado que la atendió (NULL si está pendiente)
CREATE TABLE solicitudes (
    id             INT          NOT NULL AUTO_INCREMENT,
    descripcion    VARCHAR(500) NOT NULL,
    prioridad      VARCHAR(10)  NOT NULL,
    estado         VARCHAR(15)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_atencion DATETIME(6)  NULL,
    cliente_id     INT          NOT NULL,
    empleado_id    INT          NULL,

    CONSTRAINT pk_solicitudes PRIMARY KEY (id),

    CONSTRAINT fk_solicitudes_cliente
        FOREIGN KEY (cliente_id) REFERENCES usuarios (id),
    CONSTRAINT fk_solicitudes_empleado
        FOREIGN KEY (empleado_id) REFERENCES usuarios (id),

    CONSTRAINT ck_solicitudes_prioridad
        CHECK (prioridad IN ('ALTA', 'MEDIA', 'BAJA')),
    CONSTRAINT ck_solicitudes_estado
        CHECK (estado IN ('PENDIENTE', 'ATENDIDA')),
    CONSTRAINT ck_solicitudes_descripcion
        CHECK (CHAR_LENGTH(descripcion) >= 5),
    -- Una solicitud atendida debe tener empleado y fecha de atención
    CONSTRAINT ck_solicitudes_atendida
        CHECK (estado <> 'ATENDIDA' OR (empleado_id IS NOT NULL AND fecha_atencion IS NOT NULL))
) ENGINE = InnoDB;

-- Índice para listar por estado en orden de llegada (la cola de pendientes).
-- MySQL ya crea automáticamente un índice para cada llave foránea (cliente_id, empleado_id).
CREATE INDEX idx_solicitudes_estado ON solicitudes (estado, fecha_creacion);

-- ==========================================
-- 3. DATOS DE PRUEBA
-- ==========================================
-- Los ids coinciden con la colección de Postman:
-- admin = 1, empleados = 2 y 3, clientes = 4, 5 y 6.
-- Al arrancar, la aplicación carga las solicitudes pendientes en la cola (CargaInicialDatos.java).

INSERT INTO usuarios (id, tipo, nombre, correo, nivel_acceso, cargo, salario, puntos_fidelidad) VALUES
(1, 'ADMINISTRADOR', 'Ana Martínez',  'admin@empresa.com',   5,    NULL,                  NULL,       NULL),
(2, 'EMPLEADO',      'Pedro Ramírez', 'pedro@empresa.com',   NULL, 'Soporte técnico',     2800000.0,  NULL),
(3, 'EMPLEADO',      'Sofía Torres',  'sofia@empresa.com',   NULL, 'Atención al cliente', 2500000.0,  NULL),
(4, 'CLIENTE',       'Carlos Pérez',  'cliente1@correo.com', NULL, NULL,                  NULL,       400),
(5, 'CLIENTE',       'Laura Gómez',   'cliente2@correo.com', NULL, NULL,                  NULL,       800),
(6, 'CLIENTE',       'Andrés Ruiz',   'cliente3@correo.com', NULL, NULL,                  NULL,       1200);

INSERT INTO solicitudes (id, descripcion, prioridad, estado, fecha_creacion, cliente_id) VALUES
(1, 'No puedo iniciar sesión en la plataforma',           'ALTA',  'PENDIENTE', NOW(6) - INTERVAL 50 MINUTE, 4),
(2, 'Solicito cambio de dirección de facturación',        'MEDIA', 'PENDIENTE', NOW(6) - INTERVAL 40 MINUTE, 5),
(3, 'El pago con tarjeta fue rechazado',                  'BAJA',  'PENDIENTE', NOW(6) - INTERVAL 30 MINUTE, 6),
(4, 'Quiero actualizar mi número de teléfono',            'ALTA',  'PENDIENTE', NOW(6) - INTERVAL 20 MINUTE, 4),
(5, 'La aplicación se cierra al abrir el historial',      'MEDIA', 'PENDIENTE', NOW(6) - INTERVAL 10 MINUTE, 5),
(6, 'Solicito información sobre mis puntos de fidelidad', 'BAJA',  'PENDIENTE', NOW(6),                      6);

-- Los siguientes ids automáticos empiezan después de los datos de prueba
ALTER TABLE usuarios    AUTO_INCREMENT = 7;
ALTER TABLE solicitudes AUTO_INCREMENT = 7;

-- ==========================================
-- 4. VISTA: solicitudes con el nombre del cliente y del empleado
-- ==========================================
CREATE OR REPLACE VIEW vista_solicitudes AS
SELECT s.id,
       s.descripcion,
       s.prioridad,
       s.estado,
       s.fecha_creacion,
       s.fecha_atencion,
       c.id     AS cliente_id,
       c.nombre AS cliente,
       e.nombre AS empleado
FROM solicitudes s
         INNER JOIN usuarios c ON c.id = s.cliente_id
         LEFT JOIN usuarios e ON e.id = s.empleado_id;

-- ==========================================
-- 5. CONSULTAS DE VERIFICACIÓN
-- ==========================================
SELECT * FROM usuarios;
SELECT * FROM solicitudes;

-- Usuarios por rol (cómo se ve la herencia SINGLE_TABLE)
SELECT tipo, COUNT(*) AS cantidad
FROM usuarios
GROUP BY tipo;

-- Relación Cliente -> Solicitudes: cuántas solicitudes tiene cada cliente
SELECT c.id, c.nombre, COUNT(s.id) AS total_solicitudes
FROM usuarios c
         LEFT JOIN solicitudes s ON s.cliente_id = c.id
WHERE c.tipo = 'CLIENTE'
GROUP BY c.id, c.nombre;

-- Cola de pendientes en orden de llegada (lo mismo que GET /solicitudes/cola)
SELECT * FROM vista_solicitudes
WHERE estado = 'PENDIENTE'
ORDER BY fecha_creacion, id;
