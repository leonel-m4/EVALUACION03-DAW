# Registro histórico de verificación — XAMPP / MariaDB

Los resultados de este archivo y de `evidencias/` se obtuvieron **antes** de simplificar
Ticket: en aquella versión existían un filtro por estado y una comprobación HTTP
de referencias. Se conservan como historial; **las cifras y los casos 422/503 no
verifican la versión actual**. Los resultados de compilación de la versión actual
están en la sección 6; para la entrega, ejecuta la colección Postman actual con
tus bases locales.

Fecha: **4 de noviembre de 2026**. Entorno: Windows, JDK 21.0.12.1,
Maven 3.9.16 y **MariaDB 10.4.32 de XAMPP, puerto 3306**.

## 1. Bases reales y conexión

Se crearon únicamente las bases del laboratorio, que no existían en el servidor:

| Microservicio | Puerto HTTP | Base | Tabla |
|---|---:|---|---|
| Usuario | 8081 | `bd_usuario` | `usuarios` |
| Técnico | 8082 | `bd_tecnico` | `tecnicos` |
| Ticket | 8083 | `bd_ticket` | `tickets` |

Se ejecutaron `sql/soporte-tecnico.sql` y `sql/02-usuarios-desarrollo.sql`.
Las tres cuentas locales del proyecto solo tienen permisos sobre su propia base.
Los servicios conectaron mediante MySQL Connector/J y el dialecto MariaDB de JPA.
No se modificaron otras bases ni cuentas existentes.

No se utilizó H2 ni una base de datos auxiliar.

## 2. Compilación y pruebas unitarias de la versión anterior

Comando ejecutado: `mvn -B -ntp clean verify`. Resultado: **BUILD SUCCESS**.

| Clase | Casos | Fallos | Errores | Omitidos |
|---|---:|---:|---:|---:|
| `UsuarioWebTests` | 8 | 0 | 0 | 0 |
| `TecnicoWebTests` | 8 | 0 | 0 | 0 |
| `TicketWebTests` | 18 | 0 | 0 | 0 |
| `ReferenciaServiceTests` | 5 | 0 | 0 | 0 |
| **Total** | **39** | **0** | **0** | **0** |

Estas pruebas comprueban HTTP, DTO, validaciones, normalización, reglas de negocio
y errores usando Mockito. **No afirman comprobar JPA contra un repositorio simulado**:
la persistencia real se verificó en los pasos siguientes. No se midió cobertura de líneas.

Los tres JAR ejecutables se compilaron para Java 17. Los informes unitarios se
regeneran en `*/target/surefire-reports/` y el log de esta compilación está en
`evidencias/compilacion-real.log` en el equipo de trabajo.

## 3. Postman / Newman de la versión anterior contra las bases reales

Con XAMPP y los tres JAR en ejecución:

```powershell
python scripts/verificar-postman-real.py
```

Resultado: **41 solicitudes y 73 aserciones aprobadas**, sin fallos.
Se comprobaron CRUD, cambio de estado, filtro, datos inválidos, enumeraciones
incorrectas, JSON malformado, correos duplicados e IDs inexistentes.
Las llamadas entre Ticket y Usuario/Técnico fueron HTTP reales, sin mocks.

Reporte completo y resumen:

- `evidencias/ejecucion-real-20261104-015026-434876/newman.json`
- `evidencias/ejecucion-real-20261104-015026-434876/resumen.json`

La colección limpió únicamente los registros que acababa de crear; no borró bases
ni datos anteriores. Después se dejaron los registros de demostración del paso 4.

## 4. Persistencia después de reiniciar y perfiles dev/prod

Se registraron por API:

- Usuario **ID 2**: Leonel Mathias Retamozo De La Cruz.
- Técnico **ID 2**: Ana Torres, especialidad Redes y soporte.
- Ticket **ID 2**: prioridad ALTA, inicialmente ABIERTO, actualizado a EN_PROCESO.

Se consultaron las tablas directamente mediante el cliente SQL de XAMPP y se
comprobó que esos registros realmente estaban guardados.

Después se detuvieron y arrancaron **los tres microservicios** con `prod`, pasando
sus variables de conexión privadas al proceso y usando `ddl-auto=validate`.
Los tres iniciaron correctamente y el mismo ticket seguía EN_PROCESO.
Se repitió el reinicio con `dev` y se volvió a comprobar el estado y sus IDs.

El perfil `prod` se verificó **localmente con las cuentas de desarrollo del laboratorio**,
no como un despliegue público de producción. El servidor XAMPP no se reinició ni detuvo.

En la versión anterior se detuvo únicamente Usuario y se intentó crear un ticket:
la API entonces devolvió **503** y no guardó esa operación. La versión actual
ya no consulta por HTTP al servicio Usuario. Finalmente se restablecieron los tres servicios.

Evidencia: `evidencias/flujo-real.json`, con respuestas reales e información de los reinicios.
Estado al terminar aquella ejecución: una fila de demostración en cada tabla y
tres servicios activos en `dev`. No asumir que ese es el estado actual de XAMPP.

## 5. Cómo comprobarlo en SQLyog

Conecta al servidor local de XAMPP y ejecuta `sql/03-consultas-sqlyog.sql`.
Ese archivo solo contiene SELECT. También puedes consultar:

```sql
SELECT * FROM bd_usuario.usuarios;
SELECT * FROM bd_tecnico.tecnicos;
SELECT * FROM bd_ticket.tickets;
```

En aquella ejecución se consultó `http://localhost:8083/api/tickets/2`.

Para la entrega personal falta completar la sección y adaptar las conclusiones a
tu explicación. Puedes tomar capturas desde SQLyog y Postman sin mostrar claves.
La colección actual está en `postman/SoporteTecnico.postman_collection.json`;
importarla y ejecutarla antes de presentar resultados nuevos. No se han fabricado
capturas de ninguna aplicación gráfica.

## 6. Compilación y pruebas de la versión simplificada

El **4 de noviembre de 2026** se ejecutó el Maven Wrapper del proyecto con JDK
21.0.13.0.2 y el objetivo `verify` desde la raíz: **BUILD SUCCESS** en los tres
microservicios (8 pruebas de Usuario, 8 de Técnico y 16 de Ticket; **32 en total**,
sin fallos). Estas pruebas de controlador usan repositorios simulados; **no prueban
la conexión a MariaDB ni sustituyen las pruebas de Postman**.

La colección actual se validó como JSON y contiene **37 solicitudes**. Aún hay
que ejecutarla contra los tres servicios y las bases reales para obtener evidencia
de esta versión; los resultados Newman anteriores no corresponden a la colección actual.
