-- Abrir en SQLyog conectado al servidor de XAMPP (localhost:3306).
-- Solo consultas: no crea, actualiza ni elimina registros.
SELECT SCHEMA_NAME AS base_de_datos
FROM information_schema.SCHEMATA
WHERE SCHEMA_NAME IN ('bd_usuario', 'bd_tecnico', 'bd_ticket')
ORDER BY SCHEMA_NAME;

SELECT id, nombres, apellidos, correo FROM bd_usuario.usuarios ORDER BY id;
SELECT id, nombres, apellidos, correo, especialidad FROM bd_tecnico.tecnicos ORDER BY id;
SELECT id, id_usuario, id_tecnico, asunto, descripcion, prioridad, estado
FROM bd_ticket.tickets ORDER BY id;

SELECT 'usuarios' AS tabla, COUNT(*) AS filas FROM bd_usuario.usuarios
UNION ALL SELECT 'tecnicos', COUNT(*) FROM bd_tecnico.tecnicos
UNION ALL SELECT 'tickets', COUNT(*) FROM bd_ticket.tickets;
