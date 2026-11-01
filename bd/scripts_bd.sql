-- ==========================================================
-- 1. BASE DE DATOS: GESTIÓN DE USUARIOS
-- ==========================================================
CREATE DATABASE IF NOT EXISTS bdgu_2022630310;
USE bdgu_2022630310;

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(64) NOT NULL,
    token VARCHAR(40) NULL,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    PRIMARY KEY (id_usuario),
    UNIQUE INDEX idx_email (email)
);

-- Insertamos el usuario administrador directamente en la BD
-- El password de ejemplo es 'admin123' convertido a SHA-256 (64 caracteres hex)
INSERT INTO usuarios (email, password, token, nombre, apellidos, rol)
VALUES ('jpaniaguae1800@alumno.ipn.mx', SHA2('admin123', 256), NULL, 'Admin', 'Sistema', 'administrador');


-- ==========================================================
-- 2. BASE DE DATOS: GESTIÓN DE LUGARES
-- ==========================================================
CREATE DATABASE IF NOT EXISTS bdgl_2022630310;
USE bdgl_2022630310;

CREATE TABLE lugares (
    id_lugar INT AUTO_INCREMENT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(256) NOT NULL,
    PRIMARY KEY (id_lugar)
);


-- ==========================================================
-- 3. BASE DE DATOS: GESTIÓN DE EVENTOS
-- ==========================================================
CREATE DATABASE IF NOT EXISTS bdge_2022630310;
USE bdge_2022630310;

CREATE TABLE eventos (
    id_evento INT AUTO_INCREMENT NOT NULL,
    nombre VARCHAR(256) NOT NULL,
    descripcion VARCHAR(256) NOT NULL,
    id_lugar INT NOT NULL,
    fecha_hora_evento DATETIME NOT NULL,
    precio_boleto DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id_evento)
);

CREATE TABLE fotos_eventos (
    id_foto INT AUTO_INCREMENT NOT NULL,
    foto LONGBLOB NOT NULL,
    id_evento INT NOT NULL,
    PRIMARY KEY (id_foto)
);


-- ==========================================================
-- 4. BASE DE DATOS: GESTIÓN DE COMPRAS
-- ==========================================================
CREATE DATABASE IF NOT EXISTS bdgc_2022630310;
USE bdgc_2022630310;

CREATE TABLE carrito_compra (
    id_usuario INT NOT NULL,
    id_evento INT NOT NULL,
    cantidad_boletos INT NOT NULL,
    UNIQUE INDEX idx_usuario_evento (id_usuario, id_evento)
);

CREATE TABLE boletos (
    id_evento INT NOT NULL,
    boletos_disponibles INT NOT NULL,
    PRIMARY KEY (id_evento)
);