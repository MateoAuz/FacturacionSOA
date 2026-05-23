CREATE DATABASE IF NOT EXISTS facturacion_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE facturacion_db;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS factura;
DROP TABLE IF EXISTS detalle_venta;
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS inventario;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS usuario;
DROP TABLE IF EXISTS sucursal;
DROP TABLE IF EXISTS rol;
DROP TABLE IF EXISTS configuracion_iva;
DROP TABLE IF EXISTS configuracion_empresa;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. ROL
-- ============================================================
CREATE TABLE rol (
    id_rol      TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(20)      NOT NULL,
    descripcion VARCHAR(80)      NULL,
    CONSTRAINT pk_rol        PRIMARY KEY (id_rol),
    CONSTRAINT uq_rol_nombre UNIQUE      (nombre)
) ENGINE=InnoDB;

-- ============================================================
-- 2. SUCURSAL
-- ============================================================
CREATE TABLE sucursal (
    id_sucursal SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(40)       NOT NULL,
    ciudad      VARCHAR(30)       NOT NULL,
    direccion   VARCHAR(120)      NULL,
    telefono    VARCHAR(15)       NULL,
    activo      TINYINT(1)        NOT NULL DEFAULT 1,
    CONSTRAINT pk_sucursal PRIMARY KEY (id_sucursal)
) ENGINE=InnoDB;

-- ============================================================
-- 3. USUARIO
--    Pertenece a un rol y a una sucursal base.
-- ============================================================
CREATE TABLE usuario (
    id_usuario     INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(30)       NOT NULL,
    apellido       VARCHAR(30)       NOT NULL,
    username       VARCHAR(30)       NOT NULL,
    password_hash  VARCHAR(255)      NOT NULL,
    correo         VARCHAR(80)       NULL,
    id_rol         TINYINT UNSIGNED  NOT NULL,
    id_sucursal    SMALLINT UNSIGNED NOT NULL,
    activo         TINYINT(1)        NOT NULL DEFAULT 1,
    fecha_registro DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario          PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT fk_usuario_rol      FOREIGN KEY (id_rol)      REFERENCES rol      (id_rol),
    CONSTRAINT fk_usuario_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursal (id_sucursal)
) ENGINE=InnoDB;

-- ============================================================
-- 4. CLIENTE
--    Datos necesarios para imprimir la factura.
-- ============================================================
CREATE TABLE cliente (
    id_cliente          INT UNSIGNED NOT NULL AUTO_INCREMENT,
    tipo_identificacion ENUM('CEDULA','RUC','PASAPORTE') NOT NULL DEFAULT 'CEDULA',
    identificacion      VARCHAR(20)  NOT NULL,
    nombres             VARCHAR(60)  NOT NULL,
    apellidos           VARCHAR(60)  NULL,
    razon_social        VARCHAR(100) NULL,
    direccion           VARCHAR(120) NULL,
    telefono            VARCHAR(15)  NULL,
    correo              VARCHAR(80)  NULL,
    activo              TINYINT(1)   NOT NULL DEFAULT 1,
    fecha_registro      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cliente                PRIMARY KEY (id_cliente),
    CONSTRAINT uq_cliente_identificacion UNIQUE (tipo_identificacion, identificacion)
) ENGINE=InnoDB;

-- ============================================================
-- 5. CONFIGURACIÓN IVA
--    Solo un registro con activo = 1 a la vez.
--    Ecuador: 15% desde abril 2024.
-- ============================================================
CREATE TABLE configuracion_iva (
    id_iva         TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    porcentaje     DECIMAL(5,2)     NOT NULL,
    vigencia_desde DATE             NOT NULL,
    activo         TINYINT(1)       NOT NULL DEFAULT 0,
    CONSTRAINT pk_configuracion_iva PRIMARY KEY (id_iva)
) ENGINE=InnoDB;

-- ============================================================
-- 6. CONFIGURACIÓN EMPRESA
--    Un único registro con los datos del emisor.
--    Se usa para el encabezado del PDF de factura.
-- ============================================================
CREATE TABLE configuracion_empresa (
    id_configuracion TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    razon_social     VARCHAR(100)     NOT NULL,
    nombre_comercial VARCHAR(100)     NULL,
    ruc              CHAR(13)         NOT NULL,
    direccion_matriz VARCHAR(200)     NULL,
    telefono         VARCHAR(15)      NULL,
    correo           VARCHAR(80)      NULL,
    logo_path        VARCHAR(255)     NULL,
    CONSTRAINT pk_configuracion_empresa PRIMARY KEY (id_configuracion)
) ENGINE=InnoDB;

-- ============================================================
-- 7. CATEGORÍA
-- ============================================================
CREATE TABLE categoria (
    id_categoria SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(40)       NOT NULL,
    descripcion  VARCHAR(100)      NULL,
    activo       TINYINT(1)        NOT NULL DEFAULT 1,
    CONSTRAINT pk_categoria        PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- ============================================================
-- 8. PRODUCTO
--    Catálogo global compartido entre sucursales.
--    El stock real vive en la tabla inventario.
-- ============================================================
CREATE TABLE producto (
    id_producto    INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    codigo         VARCHAR(20)       NOT NULL,
    nombre         VARCHAR(80)       NOT NULL,
    descripcion    VARCHAR(200)      NULL,
    id_categoria   SMALLINT UNSIGNED NULL,
    precio_venta   DECIMAL(12,2)     NOT NULL,
    unidad_medida  VARCHAR(20)       NOT NULL DEFAULT 'UNIDAD',
    aplica_iva     TINYINT(1)        NOT NULL DEFAULT 1,
    activo         TINYINT(1)        NOT NULL DEFAULT 1,
    fecha_registro DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_producto        PRIMARY KEY (id_producto),
    CONSTRAINT uq_producto_codigo UNIQUE (codigo),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================
-- 9. INVENTARIO  [tabla intermedia PRODUCTO ↔ SUCURSAL]
--    Stock propio de cada sucursal por producto.
-- ============================================================
CREATE TABLE inventario (
    id_inventario        INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    id_producto          INT UNSIGNED      NOT NULL,
    id_sucursal          SMALLINT UNSIGNED NOT NULL,
    cantidad             INT               NOT NULL DEFAULT 0,
    stock_minimo         INT               NOT NULL DEFAULT 0,
    ultima_actualizacion DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                    ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_inventario          PRIMARY KEY (id_inventario),
    CONSTRAINT uq_inventario_prod_suc UNIQUE (id_producto, id_sucursal),
    CONSTRAINT fk_inventario_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
    CONSTRAINT fk_inventario_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursal (id_sucursal)
) ENGINE=InnoDB;

-- ============================================================
-- 10. VENTA
--     metodo_pago va aquí: un único pago por venta.
--     estado: PAGADA | ANULADA
-- ============================================================
CREATE TABLE venta (
    id_venta     INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    numero_venta VARCHAR(15)       NOT NULL,
    id_cliente   INT UNSIGNED      NOT NULL,
    id_usuario   INT UNSIGNED      NOT NULL,
    id_sucursal  SMALLINT UNSIGNED NOT NULL,
    id_iva       TINYINT UNSIGNED  NOT NULL,
    fecha_venta  DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal     DECIMAL(12,2)     NOT NULL DEFAULT 0.00,
    iva_valor    DECIMAL(12,2)     NOT NULL DEFAULT 0.00,
    total        DECIMAL(12,2)     NOT NULL DEFAULT 0.00,
    metodo_pago  ENUM('EFECTIVO','TARJETA','TRANSFERENCIA') NOT NULL DEFAULT 'EFECTIVO',
    estado       ENUM('PAGADA','ANULADA')                   NOT NULL DEFAULT 'PAGADA',
    observacion  VARCHAR(200)      NULL,
    CONSTRAINT pk_venta        PRIMARY KEY (id_venta),
    CONSTRAINT uq_venta_numero UNIQUE (numero_venta),
    CONSTRAINT fk_venta_cliente  FOREIGN KEY (id_cliente)  REFERENCES cliente          (id_cliente),
    CONSTRAINT fk_venta_usuario  FOREIGN KEY (id_usuario)  REFERENCES usuario           (id_usuario),
    CONSTRAINT fk_venta_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursal          (id_sucursal),
    CONSTRAINT fk_venta_iva      FOREIGN KEY (id_iva)      REFERENCES configuracion_iva (id_iva)
) ENGINE=InnoDB;

-- ============================================================
-- 11. DETALLE VENTA
--     Una fila por producto en la venta.
--     precio_unitario se guarda al momento de la venta.
-- ============================================================
CREATE TABLE detalle_venta (
    id_detalle      INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_venta        INT UNSIGNED  NOT NULL,
    id_producto     INT UNSIGNED  NOT NULL,
    cantidad        INT           NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    subtotal_linea  DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_detalle_venta    PRIMARY KEY (id_detalle),
    CONSTRAINT fk_detalle_venta    FOREIGN KEY (id_venta)    REFERENCES venta    (id_venta)    ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto)
) ENGINE=InnoDB;

-- ============================================================
-- 12. FACTURA
--     Documento tributario vinculado 1:1 a una venta.
--     numero_secuencial formato: 001-001-000000001
-- ============================================================
CREATE TABLE factura (
    id_factura        INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_venta          INT UNSIGNED  NOT NULL,
    numero_secuencial VARCHAR(17)   NOT NULL,
    fecha_emision     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pdf_path          VARCHAR(255)  NULL,
    estado            ENUM('EMITIDA','ANULADA') NOT NULL DEFAULT 'EMITIDA',
    CONSTRAINT pk_factura            PRIMARY KEY (id_factura),
    CONSTRAINT uq_factura_secuencial UNIQUE (numero_secuencial),
    CONSTRAINT uq_factura_venta      UNIQUE (id_venta),
    CONSTRAINT fk_factura_venta      FOREIGN KEY (id_venta) REFERENCES venta (id_venta)
) ENGINE=InnoDB;

-- ============================================================
-- ÍNDICES
-- ============================================================
CREATE INDEX idx_venta_fecha     ON venta    (fecha_venta);
CREATE INDEX idx_venta_sucursal  ON venta    (id_sucursal);
CREATE INDEX idx_venta_cliente   ON venta    (id_cliente);
CREATE INDEX idx_producto_nombre ON producto (nombre);

-- ============================================================
-- DATOS INICIALES
-- ============================================================

INSERT INTO rol (nombre, descripcion) VALUES
('ADMIN',     'Acceso completo al sistema'),
('CAJERO',    'Gestión de ventas y facturación'),
('BODEGUERO', 'Control de inventario y stock');

INSERT INTO sucursal (nombre, ciudad, direccion, telefono) VALUES
('Sucursal Quito',  'Quito',  'Av. Amazonas N37-29',         '022000001'),
('Sucursal Ambato', 'Ambato', 'Calle Bolívar 12-34',         '032000001'),
('Sucursal Cuenca', 'Cuenca', 'Av. Solano 4-50 y Remigio',   '072000001');

INSERT INTO configuracion_iva (porcentaje, vigencia_desde, activo) VALUES
(15.00, '2024-04-01', 1);

INSERT INTO categoria (nombre, descripcion) VALUES
('General',     'Productos varios'),
('Alimentos',   'Alimentos y bebidas'),
('Electrónica', 'Dispositivos electrónicos'),
('Limpieza',    'Productos de limpieza y aseo'),
('Servicios',   'Servicios prestados por la empresa');

INSERT INTO configuracion_empresa (razon_social, nombre_comercial, ruc, direccion_matriz, telefono, correo) VALUES
('EMPRESA EJEMPLO S.A.', 'EMPRESA EJEMPLO', '1792000000001',
 'Quito, Av. Amazonas N37-29', '0999999999', 'info@empresa.com');

-- ============================================================
-- VISTAS
-- ============================================================

-- Stock actual por producto y sucursal
CREATE OR REPLACE VIEW v_stock_actual AS
SELECT
    s.nombre   AS sucursal,
    p.codigo   AS codigo,
    p.nombre   AS producto,
    c.nombre   AS categoria,
    i.cantidad AS stock_actual,
    i.stock_minimo,
    CASE
        WHEN i.cantidad <= 0              THEN 'SIN STOCK'
        WHEN i.cantidad <= i.stock_minimo THEN 'STOCK BAJO'
        ELSE 'OK'
    END        AS estado_stock
FROM inventario i
JOIN producto  p ON p.id_producto  = i.id_producto
JOIN sucursal  s ON s.id_sucursal  = i.id_sucursal
LEFT JOIN categoria c ON c.id_categoria = p.id_categoria
WHERE p.activo = 1
ORDER BY s.nombre, p.nombre;

-- Resumen de ventas
CREATE OR REPLACE VIEW v_resumen_ventas AS
SELECT
    v.id_venta,
    v.numero_venta,
    s.nombre                                          AS sucursal,
    CONCAT(c.nombres, ' ', COALESCE(c.apellidos,'')) AS cliente,
    c.identificacion,
    v.fecha_venta,
    v.subtotal,
    v.iva_valor,
    v.total,
    v.metodo_pago,
    v.estado,
    f.numero_secuencial                               AS numero_factura,
    f.estado                                          AS estado_factura
FROM venta    v
JOIN sucursal s ON s.id_sucursal = v.id_sucursal
JOIN cliente  c ON c.id_cliente  = v.id_cliente
LEFT JOIN factura f ON f.id_venta = v.id_venta;
