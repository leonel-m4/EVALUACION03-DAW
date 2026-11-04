-- MariaDB 10.4+ (XAMPP) o MySQL 8.0.16+. No borra datos ni comparte tablas.
CREATE DATABASE IF NOT EXISTS bd_usuario CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bd_tecnico CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS bd_ticket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bd_usuario.usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_correo UNIQUE (correo)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bd_tecnico.tecnicos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    especialidad VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tecnico_correo UNIQUE (correo)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bd_ticket.tickets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    id_tecnico BIGINT NOT NULL,
    asunto VARCHAR(160) NOT NULL,
    descripcion VARCHAR(4000) NOT NULL,
    prioridad VARCHAR(10) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_ticket_usuario (id_usuario),
    INDEX idx_ticket_tecnico (id_tecnico),
    INDEX idx_ticket_estado (estado),
    CONSTRAINT ck_ticket_usuario CHECK (id_usuario > 0),
    CONSTRAINT ck_ticket_tecnico CHECK (id_tecnico > 0),
    CONSTRAINT ck_ticket_prioridad CHECK (prioridad IN ('BAJA', 'MEDIA', 'ALTA')),
    CONSTRAINT ck_ticket_estado CHECK (estado IN ('ABIERTO', 'EN_PROCESO', 'CERRADO'))
) ENGINE=InnoDB;

-- Referencias lógicas por ID: deliberadamente no hay claves foráneas entre bases.
