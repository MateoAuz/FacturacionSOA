-- ============================================================
-- DATOS DE PRUEBA (solo INSERTs — no modifica estructura)
-- Ejecutar sobre base de datos existente: facturacion_db
-- ============================================================
USE facturacion_db;

-- Limpiar datos previos respetando FK order
DELETE FROM stock;
DELETE FROM detalle_factura;
DELETE FROM factura_pago;
DELETE FROM factura;
DELETE FROM solicitud_stock;
DELETE FROM producto;
DELETE FROM categoria;
DELETE FROM cliente;
DELETE FROM usuario;
DELETE FROM sucursal;
DELETE FROM rol;
DELETE FROM configuracion_iva;
DELETE FROM configuracion_empresa;

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
