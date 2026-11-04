-- SOLO desarrollo local. Estas claves son ejemplos ficticios, no secretos reales.
-- Ejecutar después de soporte-tecnico.sql con una cuenta administradora.
-- Cada cuenta solo tiene permisos sobre la base de su microservicio.
CREATE USER IF NOT EXISTS 'soporte_usuario'@'localhost' IDENTIFIED BY 'usuario_local_2026';
CREATE USER IF NOT EXISTS 'soporte_tecnico'@'localhost' IDENTIFIED BY 'tecnico_local_2026';
CREATE USER IF NOT EXISTS 'soporte_ticket'@'localhost' IDENTIFIED BY 'ticket_local_2026';

GRANT ALL PRIVILEGES ON bd_usuario.* TO 'soporte_usuario'@'localhost';
GRANT ALL PRIVILEGES ON bd_tecnico.* TO 'soporte_tecnico'@'localhost';
GRANT ALL PRIVILEGES ON bd_ticket.* TO 'soporte_ticket'@'localhost';

-- IF NOT EXISTS no cambia la contraseña de una cuenta que ya existía.
-- En producción usar claves privadas y permisos SELECT, INSERT, UPDATE, DELETE.
