-- ============================================================
--  SISTEMA DE FACTURACIÓN — FASE 1 + PROYECCIÓN FASE 2 (SRI)
--  Archivo: 02_bd_con_sri.sql
--  Mismo esquema que 01_crear_bd.sql pero con todos los
--  campos del API del SRI ya presentes (en NULL).
--  Cuando se integre la Fase 2 no se altera el esquema,
--  solo se activan esos campos desde la aplicación.
--  Motor: MySQL 8.x  |  Charset: utf8mb4
-- ============================================================

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
--    cod_establecimiento y cod_punto_emision: 3 dígitos cada
--    uno, identifican el emisor en el XML del comprobante SRI.
-- ============================================================
CREATE TABLE sucursal (
    id_sucursal         SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre              VARCHAR(40)       NOT NULL,
    ciudad              VARCHAR(30)       NOT NULL,
    direccion           VARCHAR(120)      NULL,
    telefono            VARCHAR(15)       NULL,
    activo              TINYINT(1)        NOT NULL DEFAULT 1,
    -- SRI Fase 2 ------------------------------------------------
    cod_establecimiento CHAR(3)           NULL,
    cod_punto_emision   CHAR(3)           NULL,
    -- -----------------------------------------------------------
    CONSTRAINT pk_sucursal PRIMARY KEY (id_sucursal)
) ENGINE=InnoDB;

-- ============================================================
-- 3. USUARIO
-- ============================================================
CREATE TABLE usuario (
    id_usuario     INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(30)       NOT NULL,
    apellido       VARCHAR(30)       NOT NULL,
    username       VARCHAR(30)       NOT NULL,
    password_hash  VARCHAR(255)      NOT NULL,
    correo         VARCHAR(80)       NULL,
    id_rol         TINYINT UNSIGNED  NOT NULL,
    id_sucursal    SMALLINT UNSIGNED NULL,     -- NULL para ADMIN (sin sucursal asignada)
    activo         TINYINT(1)        NOT NULL DEFAULT 1,
    fecha_registro DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario          PRIMARY KEY (id_usuario),
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT fk_usuario_rol      FOREIGN KEY (id_rol)      REFERENCES rol      (id_rol),
    CONSTRAINT fk_usuario_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursal (id_sucursal)
) ENGINE=InnoDB;

-- ============================================================
-- 4. CLIENTE
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
--    Los campos SRI son necesarios para generar el XML
--    y firmar el comprobante electrónico en Fase 2.
-- ============================================================
CREATE TABLE configuracion_empresa (
    id_configuracion       TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
    razon_social           VARCHAR(100)     NOT NULL,
    nombre_comercial       VARCHAR(100)     NULL,
    ruc                    CHAR(13)         NOT NULL,
    direccion_matriz       VARCHAR(200)     NULL,
    telefono               VARCHAR(15)      NULL,
    correo                 VARCHAR(80)      NULL,
    logo_path              VARCHAR(255)     NULL,
    -- SRI Fase 2 ------------------------------------------------
    obligado_contabilidad  ENUM('SI','NO')  NULL,
    contribuyente_especial VARCHAR(15)      NULL,
    ambiente               TINYINT(1)       NULL,
    ruta_certificado       VARCHAR(255)     NULL,
    clave_certificado      VARCHAR(100)     NULL,
    -- -----------------------------------------------------------
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
--    tipo_sri: el SRI distingue BIEN de SERVICIO en el XML.
--    aplica_iva: define si el producto grava IVA o va a 0%.
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
    -- SRI Fase 2 ------------------------------------------------
    tipo_sri       ENUM('BIEN','SERVICIO') NOT NULL DEFAULT 'BIEN',
    -- -----------------------------------------------------------
    CONSTRAINT pk_producto        PRIMARY KEY (id_producto),
    CONSTRAINT uq_producto_codigo UNIQUE (codigo),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB;

-- ============================================================
-- 9. INVENTARIO  [tabla intermedia PRODUCTO ↔ SUCURSAL]
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
--     Fase 2: clave_acceso (49 dígitos que incluyen fecha,
--     RUC, tipo comprobante, establecimiento, secuencial,
--     ambiente y dígito verificador), XMLs y autorización.
-- ============================================================
CREATE TABLE factura (
    id_factura        INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_venta          INT UNSIGNED  NOT NULL,
    numero_secuencial VARCHAR(17)   NOT NULL,
    fecha_emision     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pdf_path          VARCHAR(255)  NULL,
    estado            ENUM('EMITIDA','ANULADA') NOT NULL DEFAULT 'EMITIDA',
    -- SRI Fase 2 ------------------------------------------------
    clave_acceso        CHAR(49)     NULL,
    xml_generado        LONGTEXT     NULL,
    xml_firmado         LONGTEXT     NULL,
    xml_autorizado      LONGTEXT     NULL,
    numero_autorizacion VARCHAR(49)  NULL,
    fecha_autorizacion  DATETIME     NULL,
    estado_sri          ENUM('NO_ENVIADO','ENVIADO','AUTORIZADO','RECHAZADO')
                                     NOT NULL DEFAULT 'NO_ENVIADO',
    mensaje_sri         VARCHAR(500) NULL,
    -- -----------------------------------------------------------
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
-- 13. FACTURA_PAGO  — Formas de pago por factura
--     Una factura puede tener varias formas de pago (ej:
--     $30 efectivo + $20 tarjeta). Necesario para SRI Fase 2.
-- ============================================================
CREATE TABLE factura_pago (
    id_pago     INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_factura  INT UNSIGNED  NOT NULL,
    metodo_pago ENUM('EFECTIVO','TARJETA','TRANSFERENCIA') NOT NULL,
    monto       DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_factura_pago         PRIMARY KEY (id_pago),
    CONSTRAINT fk_factura_pago_factura FOREIGN KEY (id_factura)
        REFERENCES factura (id_factura) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_factura_pago_factura ON factura_pago (id_factura);

-- ============================================================
-- DATOS INICIALES / DE PRUEBA
-- ============================================================

-- Roles
INSERT INTO rol (nombre, descripcion) VALUES
('ADMIN',     'ACCESO COMPLETO AL SISTEMA'),
('CAJERO',    'GESTION DE VENTAS Y FACTURACION'),
('BODEGUERO', 'CONTROL DE INVENTARIO Y STOCK');

-- Sucursales
INSERT INTO sucursal (nombre, ciudad, direccion, telefono, cod_establecimiento, cod_punto_emision) VALUES
('SUCURSAL QUITO',  'QUITO',  'AV. AMAZONAS N37-29',        '022000001', '001', '001'),
('SUCURSAL AMBATO', 'AMBATO', 'CALLE BOLIVAR 12-34',        '032000001', '002', '001'),
('SUCURSAL CUENCA', 'CUENCA', 'AV. SOLANO 4-50 Y REMIGIO',  '072000001', '003', '001');

-- IVA vigente
INSERT INTO configuracion_iva (porcentaje, vigencia_desde, activo) VALUES
(15.00, '2024-04-01', 1);

-- Empresa
INSERT INTO configuracion_empresa (
    razon_social, nombre_comercial, ruc,
    direccion_matriz, telefono, correo,
    obligado_contabilidad, ambiente
) VALUES (
    'EMPRESA EJEMPLO S.A.', 'EMPRESA EJEMPLO', '1792000000001',
    'QUITO, AV. AMAZONAS N37-29', '0999999999', 'info@empresa.com',
    'NO', 1
);

-- Categorias
INSERT INTO categoria (nombre, descripcion) VALUES
('ALIMENTOS',   'ALIMENTOS Y BEBIDAS DE CONSUMO MASIVO'),
('ELECTRONICA', 'ACCESORIOS Y DISPOSITIVOS ELECTRONICOS'),
('LIMPIEZA',    'PRODUCTOS DE LIMPIEZA Y ASEO DEL HOGAR'),
('OFICINA',     'SUMINISTROS Y ARTICULOS DE OFICINA'),
('SERVICIOS',   'SERVICIOS PRESTADOS POR LA EMPRESA');

-- ============================================================
-- USUARIOS
-- Contrasenas:
--   admin        -> Admin123!
--   cajeros      -> Cajero123!
--   bodegueros   -> Bodega123!
-- ============================================================
-- id_rol: 1=ADMIN  2=CAJERO  3=BODEGUERO
-- id_sucursal: 1=QUITO  2=AMBATO  3=CUENCA  NULL=ADMIN (sin sucursal)

INSERT INTO usuario (nombre, apellido, username, password_hash, correo, id_rol, id_sucursal) VALUES
-- Administrador global (sin sucursal)
('CARLOS',   'MENDOZA',   'admin',         '$2b$10$rNv5YuHR1.UcbeJNVEOwIektrcmcPiQD7pUgdMjyPzSTJz2FFz2ju', 'admin@empresa.com',         1, NULL),
-- Cajeros por sucursal
('ANA',      'TORRES',    'cajero.quito',  '$2b$10$B8IZQIfRibKS65TqclohN.GPUXZ6li3iFoCvqChrdfrW7ZVAi6Qzu', 'cajero.quito@empresa.com',  2, 1),
('LUIS',     'PAREDES',   'cajero.ambato', '$2b$10$B8IZQIfRibKS65TqclohN.GPUXZ6li3iFoCvqChrdfrW7ZVAi6Qzu', 'cajero.ambato@empresa.com', 2, 2),
('SOFIA',    'RAMOS',     'cajero.cuenca', '$2b$10$B8IZQIfRibKS65TqclohN.GPUXZ6li3iFoCvqChrdfrW7ZVAi6Qzu', 'cajero.cuenca@empresa.com', 2, 3),
-- Bodegueros por sucursal
('JORGE',    'SALAZAR',   'bodega.quito',  '$2b$10$GpcyjIpD.fvoZJKoYQE/4OGpJQkgWBxYia9ceFXDfa6a2wlDElLM6', 'bodega.quito@empresa.com',  3, 1),
('MARIANA',  'VEGA',      'bodega.ambato', '$2b$10$GpcyjIpD.fvoZJKoYQE/4OGpJQkgWBxYia9ceFXDfa6a2wlDElLM6', 'bodega.ambato@empresa.com', 3, 2),
('PEDRO',    'CARDENAS',  'bodega.cuenca', '$2b$10$GpcyjIpD.fvoZJKoYQE/4OGpJQkgWBxYia9ceFXDfa6a2wlDElLM6', 'bodega.cuenca@empresa.com', 3, 3);

-- ============================================================
-- CLIENTES
-- ============================================================
INSERT INTO cliente (tipo_identificacion, identificacion, nombres, apellidos, razon_social, direccion, telefono, correo) VALUES
-- Personas naturales (Cedula)
('CEDULA', '1712345678',    'MARIA JOSE',   'ALVAREZ RUIZ',    NULL,                          'QUITO, CALLE LAS FLORES 123',      '0991234567', 'mariajose@gmail.com'),
('CEDULA', '1798765432',    'DIEGO',        'SANCHEZ MORA',    NULL,                          'QUITO, AV. 6 DE DICIEMBRE N45',    '0987654321', 'diegosanchez@gmail.com'),
('CEDULA', '1803456789',    'PATRICIA',     'LEMA GUAMAN',     NULL,                          'AMBATO, CALLE CEVALLOS 56',        '0993456789', 'patricia.lema@hotmail.com'),
('CEDULA', '1856789012',    'ROBERTO',      'PIEDRA VACA',     NULL,                          'AMBATO, AV. INDOAMERICA 200',      '0996789012', NULL),
('CEDULA', '0101234567',    'LUCIA',        'ASTUDILLO PINOS', NULL,                          'CUENCA, CALLE LARGA 78',           '0978901234', 'lucia.astudillo@outlook.com'),
('CEDULA', '0198765432',    'FERNANDO',     'OCHOA TAPIA',     NULL,                          'CUENCA, AV. DE LAS AMERICAS 900',  '0987890123', NULL),
-- Empresas (RUC)
('RUC',    '1790123456001', 'IMPORTADORA',  'DEL NORTE S.A.',  'IMPORTADORA DEL NORTE S.A.',  'QUITO, PARQUE INDUSTRIAL LOTE 5',  '022345678',  'compras@importnorte.com'),
('RUC',    '1891234567001', 'DISTRIBUIDORA','ANDES CIA.',      'DISTRIBUIDORA ANDES CIA. LTDA.','AMBATO, AV. EL REY 450',         '032456789',  'info@distribandes.com'),
('RUC',    '0190123456001', 'COMERCIAL',    'AUSTRAL LTDA.',   'COMERCIAL AUSTRAL LTDA.',     'CUENCA, AV. HUAYNA CAPAC 112',     '072567890',  'contacto@comaustr.com'),
-- Consumidor Final
('CEDULA', '9999999999',    'CONSUMIDOR',   'FINAL',           NULL,                          NULL,                               NULL,         NULL);

-- ============================================================
-- PRODUCTOS
-- id_categoria: 1=ALIMENTOS  2=ELECTRONICA  3=LIMPIEZA  4=OFICINA  5=SERVICIOS
-- ============================================================
INSERT INTO producto (codigo, nombre, descripcion, id_categoria, precio_venta, unidad_medida, aplica_iva, tipo_sri) VALUES
-- Alimentos (cat 1)
('ALI-001', 'ARROZ BLANCO 5 KG',          'ARROZ BLANCO DE GRANO LARGO, SACO 5 KG',           1,  6.50, 'UNIDAD',  0, 'BIEN'),
('ALI-002', 'AZUCAR BLANCA 2 KG',         'AZUCAR BLANCA REFINADA, BOLSA 2 KG',               1,  2.80, 'UNIDAD',  0, 'BIEN'),
('ALI-003', 'ACEITE VEGETAL 1 L',         'ACEITE DE GIRASOL BOTELLA 1 LITRO',                1,  3.20, 'UNIDAD',  1, 'BIEN'),
('ALI-004', 'ATUN EN LATA 170 G',         'ATUN EN AGUA, LATA 170 G',                         1,  1.95, 'UNIDAD',  1, 'BIEN'),
('ALI-005', 'SAL DE MESA 1 KG',           'SAL YODADA BOLSA 1 KG',                            1,  0.55, 'UNIDAD',  0, 'BIEN'),
('ALI-006', 'LECHE ENTERA 1 L',           'LECHE ENTERA PASTEURIZADA 1 LITRO',                1,  1.20, 'LITRO',   0, 'BIEN'),
-- Electronica (cat 2)
('ELE-001', 'CABLE USB-C 1 M',            'CABLE DE CARGA Y DATOS USB-C TRENZADO 1 M',        2,  8.99, 'UNIDAD',  1, 'BIEN'),
('ELE-002', 'AURICULARES INALAMBRICOS',   'AURICULARES BLUETOOTH CON MICROFONO INCORPORADO',  2, 29.99, 'UNIDAD',  1, 'BIEN'),
('ELE-003', 'CARGADOR RAPIDO 20 W',       'CARGADOR DE PARED USB-C 20 W',                     2, 14.50, 'UNIDAD',  1, 'BIEN'),
('ELE-004', 'FUNDA CELULAR UNIVERSAL',    'FUNDA PROTECTORA SILICONA TALLA L',                2,  4.25, 'UNIDAD',  1, 'BIEN'),
-- Limpieza (cat 3)
('LIM-001', 'JABON LIQUIDO 500 ML',       'JABON LIQUIDO PARA MANOS, AROMA LAVANDA',          3,  3.75, 'UNIDAD',  1, 'BIEN'),
('LIM-002', 'DESINFECTANTE 1 L',          'DESINFECTANTE MULTIUSOS FRAGANCIA PINO 1 LITRO',   3,  5.10, 'LITRO',   1, 'BIEN'),
('LIM-003', 'ESPONJA FIBRA VERDE',        'ESPONJA DOBLE FUNCION FIBRA VERDE, PACK X3',       3,  1.80, 'PACK',    1, 'BIEN'),
-- Oficina (cat 4)
('OFI-001', 'PAPEL BOND A4 500 HOJAS',   'RESMA PAPEL BOND A4 75 G, 500 HOJAS',              4,  5.90, 'RESMA',   1, 'BIEN'),
('OFI-002', 'MARCADORES PERMANENTES',    'SET 4 MARCADORES PERMANENTES COLORES BASICOS',      4,  3.50, 'SET',     1, 'BIEN'),
('OFI-003', 'BOLIGRAFOS AZUL X10',       'CAJA 10 BOLIGRAFOS TINTA AZUL PUNTA MEDIA',        4,  2.20, 'CAJA',    1, 'BIEN'),
-- Servicios (cat 5)
('SRV-001', 'INSTALACION Y CONFIGURACION','SERVICIO DE INSTALACION Y CONFIGURACION DE EQUIPO',5, 25.00, 'HORA',    1, 'SERVICIO'),
('SRV-002', 'SOPORTE TECNICO',            'SOPORTE TECNICO REMOTO O PRESENCIAL',              5, 18.00, 'HORA',    1, 'SERVICIO');

-- ============================================================
-- STOCK INICIAL  (id_producto x id_sucursal)
-- Productos 1-18, sucursales 1-3
-- Alimentos tienen stock alto; electronica/limpieza mediano;
-- oficina mediano; servicios van a 0 (intangible)
-- ============================================================
INSERT INTO stock (id_producto, id_sucursal, cantidad) VALUES
-- ALI-001 ARROZ
(1,1,80),(1,2,60),(1,3,70),
-- ALI-002 AZUCAR
(2,1,100),(2,2,80),(2,3,90),
-- ALI-003 ACEITE
(3,1,50),(3,2,40),(3,3,0),
-- ALI-004 ATUN
(4,1,120),(4,2,0),(4,3,60),
-- ALI-005 SAL
(5,1,150),(5,2,130),(5,3,110),
-- ALI-006 LECHE
(6,1,60),(6,2,50),(6,3,45),
-- ELE-001 CABLE USB-C
(7,1,30),(7,2,20),(7,3,25),
-- ELE-002 AURICULARES
(8,1,15),(8,2,0),(8,3,10),
-- ELE-003 CARGADOR
(9,1,20),(9,2,18),(9,3,12),
-- ELE-004 FUNDA
(10,1,40),(10,2,35),(10,3,30),
-- LIM-001 JABON
(11,1,55),(11,2,45),(11,3,50),
-- LIM-002 DESINFECTANTE
(12,1,30),(12,2,0),(12,3,20),
-- LIM-003 ESPONJA
(13,1,70),(13,2,60),(13,3,0),
-- OFI-001 PAPEL
(14,1,25),(14,2,20),(14,3,18),
-- OFI-002 MARCADORES
(15,1,40),(15,2,35),(15,3,30),
-- OFI-003 BOLIGRAFOS
(16,1,50),(16,2,40),(16,3,45),
-- SRV-001 INSTALACION (intangible -> 0)
(17,1,0),(17,2,0),(17,3,0),
-- SRV-002 SOPORTE (intangible -> 0)
(18,1,0),(18,2,0),(18,3,0);
