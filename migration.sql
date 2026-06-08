-- ============================================================
--  MIGRACIÓN: Renombrar y fusionar tablas del sistema
--  Ejecutar en orden. Tener un backup antes de proceder.
--  Motor: MySQL 8.x
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- PASO 1: Corregir ENUM de factura.estado (fix inmediato)
-- ============================================================
ALTER TABLE factura
    MODIFY COLUMN estado ENUM('GUARDADA','EMITIDA','ANULADA') NOT NULL DEFAULT 'GUARDADA';

-- ============================================================
-- PASO 2: inventario → stock
-- ============================================================
CREATE TABLE stock (
    id_stock              INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    id_producto           INT UNSIGNED      NOT NULL,
    id_sucursal           SMALLINT UNSIGNED NOT NULL,
    cantidad              INT               NOT NULL DEFAULT 0,
    ultima_actualizacion  DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP
                                                     ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_stock           PRIMARY KEY (id_stock),
    CONSTRAINT uq_stock_prod_suc  UNIQUE      (id_producto, id_sucursal),
    CONSTRAINT fk_stock_producto  FOREIGN KEY (id_producto) REFERENCES producto  (id_producto),
    CONSTRAINT fk_stock_sucursal  FOREIGN KEY (id_sucursal) REFERENCES sucursal  (id_sucursal)
) ENGINE=InnoDB;

INSERT INTO stock (id_stock, id_producto, id_sucursal, cantidad, ultima_actualizacion)
SELECT id_inventario, id_producto, id_sucursal, cantidad, ultima_actualizacion
FROM inventario;

-- ============================================================
-- PASO 3: Crear tabla factura unificada (venta + factura)
--         Se usa la PK de la factura original como nueva PK
-- ============================================================
CREATE TABLE factura_nueva (
    id_factura           INT UNSIGNED      NOT NULL AUTO_INCREMENT,
    numero_secuencial    VARCHAR(17)       NOT NULL,
    id_cliente           INT UNSIGNED      NOT NULL,
    id_usuario           INT UNSIGNED      NOT NULL,
    id_sucursal          SMALLINT UNSIGNED NOT NULL,
    id_iva               TINYINT UNSIGNED  NOT NULL,
    fecha_factura        DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal             DECIMAL(12,2)     NOT NULL DEFAULT 0.00,
    iva_valor            DECIMAL(12,2)     NOT NULL DEFAULT 0.00,
    total                DECIMAL(12,2)     NOT NULL DEFAULT 0.00,
    metodo_pago          ENUM('EFECTIVO','TARJETA','TRANSFERENCIA') NOT NULL DEFAULT 'EFECTIVO',
    estado               ENUM('GUARDADA','EMITIDA','ANULADA')       NOT NULL DEFAULT 'GUARDADA',
    observacion          VARCHAR(200)      NULL,
    -- Campos SRI (Fase 2) ----------------------------------------
    fecha_emision        DATETIME          NULL,
    pdf_path             VARCHAR(255)      NULL,
    estado_sri           ENUM('NO_ENVIADO','ENVIADO','AUTORIZADO','RECHAZADO') NOT NULL DEFAULT 'NO_ENVIADO',
    clave_acceso         CHAR(49)          NULL,
    xml_generado         LONGTEXT          NULL,
    xml_firmado          LONGTEXT          NULL,
    xml_autorizado       LONGTEXT          NULL,
    numero_autorizacion  VARCHAR(49)       NULL,
    fecha_autorizacion   DATETIME          NULL,
    mensaje_sri          VARCHAR(500)      NULL,
    -- ------------------------------------------------------------
    CONSTRAINT pk_factura_nueva            PRIMARY KEY (id_factura),
    CONSTRAINT uq_factura_nueva_secuencial UNIQUE      (numero_secuencial)
) ENGINE=InnoDB;

-- Migrar datos de venta + factura a factura_nueva
-- Solo ventas que tienen factura asociada (deberían ser todas con el nuevo flujo)
INSERT INTO factura_nueva (
    id_factura, numero_secuencial, id_cliente, id_usuario, id_sucursal, id_iva,
    fecha_factura, subtotal, iva_valor, total, metodo_pago, estado, observacion,
    fecha_emision, pdf_path, estado_sri, clave_acceso,
    xml_generado, xml_firmado, xml_autorizado,
    numero_autorizacion, fecha_autorizacion, mensaje_sri
)
SELECT
    f.id_factura,
    f.numero_secuencial,
    v.id_cliente,
    v.id_usuario,
    v.id_sucursal,
    v.id_iva,
    v.fecha_venta,
    v.subtotal,
    v.iva_valor,
    v.total,
    v.metodo_pago,
    CASE f.estado
        WHEN 'EMITIDA' THEN 'EMITIDA'
        WHEN 'ANULADA' THEN 'ANULADA'
        ELSE 'GUARDADA'
    END,
    v.observacion,
    f.fecha_emision,
    f.pdf_path,
    f.estado_sri,
    f.clave_acceso,
    f.xml_generado,
    f.xml_firmado,
    f.xml_autorizado,
    f.numero_autorizacion,
    f.fecha_autorizacion,
    f.mensaje_sri
FROM factura f
JOIN venta v ON v.id_venta = f.id_venta;

-- ============================================================
-- PASO 4: detalle_venta → detalle_factura
--         id_venta ahora referencia id_factura de factura_nueva
-- ============================================================
CREATE TABLE detalle_factura (
    id_detalle        INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    id_factura        INT UNSIGNED  NOT NULL,
    id_producto       INT UNSIGNED  NOT NULL,
    cantidad          INT           NOT NULL,
    precio_unitario   DECIMAL(12,2) NOT NULL,
    subtotal_linea    DECIMAL(12,2) NOT NULL,
    CONSTRAINT pk_detalle_factura           PRIMARY KEY (id_detalle),
    CONSTRAINT fk_detalle_factura_factura   FOREIGN KEY (id_factura)  REFERENCES factura_nueva (id_factura) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_factura_producto  FOREIGN KEY (id_producto) REFERENCES producto      (id_producto)
) ENGINE=InnoDB;

-- Migrar: mapear id_venta → id_factura a través de la tabla factura original
INSERT INTO detalle_factura (id_detalle, id_factura, id_producto, cantidad, precio_unitario, subtotal_linea)
SELECT
    dv.id_detalle,
    f.id_factura,      -- id_factura de la tabla factura original
    dv.id_producto,
    dv.cantidad,
    dv.precio_unitario,
    dv.subtotal_linea
FROM detalle_venta dv
JOIN factura f ON f.id_venta = dv.id_venta;

-- ============================================================
-- PASO 5: Eliminar tablas antiguas y renombrar nuevas
-- ============================================================
DROP TABLE IF EXISTS detalle_venta;
DROP TABLE IF EXISTS factura;        -- tabla vieja (con FK a venta)
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS inventario;

ALTER TABLE factura_nueva RENAME TO factura;

-- Restaurar FKs en la nueva tabla factura
ALTER TABLE factura
    ADD CONSTRAINT fk_factura_cliente  FOREIGN KEY (id_cliente)  REFERENCES cliente          (id_cliente),
    ADD CONSTRAINT fk_factura_usuario  FOREIGN KEY (id_usuario)  REFERENCES usuario           (id_usuario),
    ADD CONSTRAINT fk_factura_sucursal FOREIGN KEY (id_sucursal) REFERENCES sucursal          (id_sucursal),
    ADD CONSTRAINT fk_factura_iva      FOREIGN KEY (id_iva)      REFERENCES configuracion_iva (id_iva);

-- Índices
CREATE INDEX idx_factura_fecha     ON factura (fecha_factura);
CREATE INDEX idx_factura_sucursal  ON factura (id_sucursal);
CREATE INDEX idx_factura_cliente   ON factura (id_cliente);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- FIN DE MIGRACIÓN
-- Verificar con:
-- SELECT COUNT(*) FROM factura;
-- SELECT COUNT(*) FROM detalle_factura;
-- SELECT COUNT(*) FROM stock;
-- ============================================================


-- ============================================================
-- MIGRACIÓN: Snapshot de datos de factura para auditoría
-- Ejecutar después de las migraciones anteriores.
-- Propósito: preservar datos del cliente, vendedor, sucursal
-- e IVA exactamente como estaban al momento de facturar.
-- Requerimiento: reconstrucción exacta de ventas por auditoría
-- ============================================================

-- Columnas snapshot en tabla factura
ALTER TABLE factura
    ADD COLUMN IF NOT EXISTS snap_cli_tipo_id        VARCHAR(10)     COMMENT 'Tipo ID cliente al facturar',
    ADD COLUMN IF NOT EXISTS snap_cli_identificacion VARCHAR(20)     COMMENT 'Identificación cliente al facturar',
    ADD COLUMN IF NOT EXISTS snap_cli_nombres        VARCHAR(60)     COMMENT 'Nombres cliente al facturar',
    ADD COLUMN IF NOT EXISTS snap_cli_apellidos      VARCHAR(60)     COMMENT 'Apellidos cliente al facturar',
    ADD COLUMN IF NOT EXISTS snap_cli_razon_social   VARCHAR(100)    COMMENT 'Razón social cliente al facturar',
    ADD COLUMN IF NOT EXISTS snap_cli_correo         VARCHAR(80)     COMMENT 'Correo cliente al facturar',
    ADD COLUMN IF NOT EXISTS snap_cli_telefono       VARCHAR(15)     COMMENT 'Teléfono cliente al facturar',
    ADD COLUMN IF NOT EXISTS snap_cli_direccion      VARCHAR(120)    COMMENT 'Dirección cliente al facturar',
    ADD COLUMN IF NOT EXISTS snap_usuario_nombre     VARCHAR(100)    COMMENT 'Nombre vendedor al facturar',
    ADD COLUMN IF NOT EXISTS snap_sucursal_nombre    VARCHAR(80)     COMMENT 'Nombre sucursal al facturar',
    ADD COLUMN IF NOT EXISTS snap_sucursal_ciudad    VARCHAR(80)     COMMENT 'Ciudad sucursal al facturar',
    ADD COLUMN IF NOT EXISTS snap_iva_porcentaje     DECIMAL(5,2)    COMMENT 'Porcentaje IVA aplicado';

-- Columnas snapshot en tabla detalle_factura
ALTER TABLE detalle_factura
    ADD COLUMN IF NOT EXISTS snap_producto_nombre VARCHAR(100) COMMENT 'Nombre del producto al facturar',
    ADD COLUMN IF NOT EXISTS snap_producto_codigo VARCHAR(30)  COMMENT 'Código del producto al facturar';

-- Rellenar snapshots en facturas existentes (retroactivo, usando datos actuales de FK)
-- NOTA: Esto captura el estado ACTUAL de los clientes/productos para facturas históricas.
-- Las facturas nuevas tendrán el snapshot correcto del momento exacto de la transacción.
UPDATE factura f
    JOIN cliente c  ON f.id_cliente  = c.id_cliente
    JOIN usuario u  ON f.id_usuario  = u.id_usuario
    JOIN sucursal s ON f.id_sucursal = s.id_sucursal
    JOIN configuracion_iva iv ON f.id_iva = iv.id_iva
SET
    f.snap_cli_tipo_id        = COALESCE(f.snap_cli_tipo_id,        c.tipo_identificacion),
    f.snap_cli_identificacion = COALESCE(f.snap_cli_identificacion, c.identificacion),
    f.snap_cli_nombres        = COALESCE(f.snap_cli_nombres,        c.nombres),
    f.snap_cli_apellidos      = COALESCE(f.snap_cli_apellidos,      c.apellidos),
    f.snap_cli_razon_social   = COALESCE(f.snap_cli_razon_social,   c.razon_social),
    f.snap_cli_correo         = COALESCE(f.snap_cli_correo,         c.correo),
    f.snap_cli_telefono       = COALESCE(f.snap_cli_telefono,       c.telefono),
    f.snap_cli_direccion      = COALESCE(f.snap_cli_direccion,      c.direccion),
    f.snap_usuario_nombre     = COALESCE(f.snap_usuario_nombre,     CONCAT(u.nombre, ' ', u.apellido)),
    f.snap_sucursal_nombre    = COALESCE(f.snap_sucursal_nombre,    s.nombre),
    f.snap_sucursal_ciudad    = COALESCE(f.snap_sucursal_ciudad,    s.ciudad),
    f.snap_iva_porcentaje     = COALESCE(f.snap_iva_porcentaje,     iv.porcentaje)
WHERE f.snap_cli_identificacion IS NULL;

UPDATE detalle_factura df
    JOIN producto p ON df.id_producto = p.id_producto
SET
    df.snap_producto_nombre = COALESCE(df.snap_producto_nombre, p.nombre),
    df.snap_producto_codigo = COALESCE(df.snap_producto_codigo, p.codigo)
WHERE df.snap_producto_nombre IS NULL;

SELECT 'Migración snapshot completada.' AS resultado;
