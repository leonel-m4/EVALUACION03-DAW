# Evaluación 03 — Laboratorio 17: Sistema de soporte técnico

**Estudiante:** Retamozo De La Cruz Leonel Mathias. Completar la sección antes de entregar.

Tres microservicios Spring Boot con MariaDB/MySQL local, REST, JPA, DTO y validaciones.
Sin Docker, Eureka, OpenFeign ni bases de datos auxiliares. El ZIP del profesor se usa como referencia de
paquetes y capas, no como un tema diferente al asignado en el Word.

Repositorio: https://github.com/leonel-m4/EVALUACION03-DAW

```powershell
git clone https://github.com/leonel-m4/EVALUACION03-DAW.git soporte-tecnico
cd soporte-tecnico
```

## 1. Requisitos

- JDK **17 o superior** (se compila para Java 17; compatible con tu JDK 21).
- Maven **3.6.3 o superior**, o el Maven Wrapper incluido en cada servicio.
- **XAMPP con MariaDB 10.4+** (verificado con tu MariaDB **10.4.32**) o MySQL 8.0.16+,
  ejecutándose localmente en el puerto 3306. SQLyog/Workbench son clientes del servidor.
- Postman para ejecutar la colección.
- Internet en la primera compilación para descargar dependencias.

Spring Boot **3.5.15**, de la misma rama 3.5 que el ejemplo del profesor.
Los proyectos base se generaron con Spring Initializr y luego se adaptaron
a Java 17, Spring Boot 3.5 y los requisitos del laboratorio.

## 2. Arquitectura

| Proyecto | Puerto predeterminado | Base de datos | Responsabilidad |
|---|---:|---|---|
| `usuario-service` | 8081 | `bd_usuario` | CRUD de usuarios |
| `tecnico-service` | 8082 | `bd_tecnico` | CRUD de técnicos |
| `ticket-service` | 8083 | `bd_ticket` | CRUD de tickets y cambio de estado |

Cada proyecto tiene su propio `pom.xml`, aplicación Java y conexión a MySQL.
El `pom.xml` de la carpeta principal **solo permite compilar/probar los tres juntos**:
no es una cuarta aplicación y no arranca los servicios automáticamente.

En cada servicio:

```text
src/main/java/com/tecsup/
├── *ServiceApplication.java
├── model/        # Entidades JPA y enumeraciones
├── dto/          # Entrada validada y respuestas de la API
├── repository/   # JpaRepository
├── services/     # Reglas de negocio y transacciones
├── controller/   # Endpoints REST
└── exception/    # ResourceNotFoundException, ApiError y GlobalExceptionHandler
src/main/resources/
├── application.yml
├── application-dev.yml
└── application-prod.yml
src/test/java/    # Pruebas unitarias sin base de datos
```

Diagrama: [docs/diagrama.md](docs/diagrama.md) y [versión visual SVG](docs/diagrama.svg).

## 3. Preparar MariaDB/MySQL local con XAMPP y SQLyog

1. En XAMPP, inicia el módulo **MySQL** (internamente es MariaDB en tu instalación).
2. En SQLyog o Workbench, usa tu conexión habitual a `localhost:3306` y ejecuta
   con una cuenta administradora, en este orden:
   - `sql/soporte-tecnico.sql`: crea las tres bases y sus tablas.
   - `sql/02-usuarios-desarrollo.sql`: crea tres cuentas locales de ejemplo,
     cada una limitada a su propia base.
3. Las claves de ese segundo archivo son **ficticias y exclusivamente de desarrollo**.
   Coinciden con los valores predeterminados de `application-dev.yml`.

Alternativa con el cliente MySQL, estando en esta carpeta:

```text
mysql -u root -p --default-character-set=utf8mb4
SOURCE sql/soporte-tecnico.sql;
SOURCE sql/02-usuarios-desarrollo.sql;
```

Los scripts usan `IF NOT EXISTS`: no borran datos ni cambian contraseñas existentes.
Si ya existe una cuenta con otra clave, configura la clave correcta mediante variables
de entorno, no la publiques. No hace falta utilizar `root` desde los microservicios.

**En este equipo ya se ejecutaron los scripts y se verificó el funcionamiento con XAMPP.**
Para ver los registros desde SQLyog, abre `sql/03-consultas-sqlyog.sql` y ejecútalo
en tu conexión local. Se dejó un usuario, un técnico y el **ticket ID 2**, en estado
`EN_PROCESO`, como datos de demostración persistentes. Los detalles de esa ejecución
están en `evidencias/flujo-real.json`. No se modificaron otras bases de tu servidor.

## 4. Compilar y ejecutar

Abre PowerShell en la carpeta `soporte-tecnico`:

```powershell
mvn clean verify
```

Este comando ejecuta pruebas unitarias **sin conectarse a ninguna base de datos**
y genera los tres JAR. No existe dependencia H2 ni se crean bases auxiliares.
Después abre **tres terminales**, siempre desde esta carpeta:

```powershell
# Terminal 1
java -jar .\usuario-service\target\usuario-service-0.0.1-SNAPSHOT.jar
```

```powershell
# Terminal 2
java -jar .\tecnico-service\target\tecnico-service-0.0.1-SNAPSHOT.jar
```

```powershell
# Terminal 3
java -jar .\ticket-service\target\ticket-service-0.0.1-SNAPSHOT.jar
```

El perfil predeterminado es `dev`. Espera a ver `Started ...Application` en cada terminal.
Para detener una aplicación usa `Ctrl+C` en su terminal.

### Inicio y parada con scripts de Windows

Alternativa a las tres terminales, desde la carpeta principal:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\iniciar.ps1
```

El script inicia los tres JAR en segundo plano, deja sus logs en `evidencias/servicios/`
y guarda sus PID. No inicia ni detiene XAMPP y no cambia permanentemente la política
de PowerShell. Rechaza puertos ocupados en lugar de cerrar otras aplicaciones.

Para detener únicamente los procesos registrados de este proyecto:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\detener.ps1
```

No ejecutes el inicio por segunda vez mientras ya estén encendidos.

También puedes ejecutar la clase principal de cada proyecto desde IntelliJ.
Sin Maven instalado, entra en cada servicio y ejecuta ` .\mvnw.cmd verify `;
para arrancarlo desde su carpeta, ` .\mvnw.cmd spring-boot:run `.

Si la conexión local muestra `Public Key Retrieval is not allowed`, usa únicamente
en desarrollo una URL como `jdbc:mysql://localhost:3306/bd_usuario?serverTimezone=UTC&allowPublicKeyRetrieval=true&sslMode=DISABLED`.
No desactives TLS de esa manera en producción.

## 5. Configuración y perfiles

| Aspecto | `dev` | `prod` |
|---|---|---|
| Conexión | MySQL local, valores de ejemplo reemplazables | URL, usuario y clave obligatorios por entorno |
| `ddl-auto` | `update`: permite actualizar el esquema | `validate`: comprueba el esquema sin modificarlo |
| SQL en consola | Sí | No |
| Esquema inicial | Script SQL proporcionado | Script aplicado por un administrador |

Variables admitidas (no se carga un archivo `.env` automáticamente):

| Servicio | Variables |
|---|---|
| Usuario | `USUARIO_DB_URL`, `USUARIO_DB_USER`, `USUARIO_DB_PASSWORD`, `USUARIO_PORT` |
| Técnico | `TECNICO_DB_URL`, `TECNICO_DB_USER`, `TECNICO_DB_PASSWORD`, `TECNICO_PORT` |
| Ticket | `TICKET_DB_URL`, `TICKET_DB_USER`, `TICKET_DB_PASSWORD`, `TICKET_PORT` |
| Referencias de Ticket | `USUARIO_SERVICE_URL`, `TECNICO_SERVICE_URL` |
| Dialecto JPA | `DB_DIALECT` (predeterminado: `org.hibernate.dialect.MariaDBDialect`) |

Se conserva **MySQL Driver** como pide la guía; se comprobó su conexión con MariaDB
de XAMPP. El dialecto explícito evita interpretar MariaDB como una versión antigua
de MySQL. Si usas un servidor MySQL 8 genuino, establece antes de iniciar:

```powershell
$env:DB_DIALECT = 'org.hibernate.dialect.MySQLDialect'
```

Ejemplo para **una terminal de producción** (sustituir los marcadores en privado):

```powershell
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:USUARIO_DB_URL = 'jdbc:mysql://servidor:3306/bd_usuario?sslMode=VERIFY_IDENTITY&serverTimezone=UTC'
$env:USUARIO_DB_USER = '<usuario_privado>'
$env:USUARIO_DB_PASSWORD = '<clave_privada>'
java -jar .\usuario-service\target\usuario-service-0.0.1-SNAPSHOT.jar
```

Para los otros servicios se usan sus respectivas variables. Las variables se definen
en la terminal donde se inicia el servicio. No mostrar claves reales en capturas.
Esta entrega es académica: no incluye autenticación, autorización ni un despliegue
endurecido para Internet; `prod` no significa que esos controles ya existan.

## 6. Endpoints

Cada ruta se consulta en el puerto de su propio servicio:

| Método | Usuario (8081) | Técnico (8082) | Ticket (8083) | Éxito |
|---|---|---|---|---:|
| GET | `/api/usuarios` | `/api/tecnicos` | `/api/tickets` | 200 |
| GET | `/api/usuarios/{id}` | `/api/tecnicos/{id}` | `/api/tickets/{id}` | 200 |
| POST | `/api/usuarios` | `/api/tecnicos` | `/api/tickets` | 201 + `Location` |
| PUT | `/api/usuarios/{id}` | `/api/tecnicos/{id}` | `/api/tickets/{id}` | 200 |
| DELETE | `/api/usuarios/{id}` | `/api/tecnicos/{id}` | `/api/tickets/{id}` | 204 sin cuerpo |

Adicionales de Ticket:

- `PATCH /api/tickets/{id}/estado`: cambia únicamente el estado.
- `GET /api/tickets?estado=EN_PROCESO`: filtra por estado con un método de repositorio.

Enviar JSON con `Content-Type: application/json`. `PUT` requiere todos los campos
del DTO; `PATCH` solo necesita `estado`. No enviar el `id`: lo genera la base.

### Flujo mínimo de aceptación

**1. POST `http://localhost:8081/api/usuarios`**

```json
{"nombres":"Leonel Mathias","apellidos":"Retamozo De La Cruz","correo":"leonel@example.test"}
```

**2. POST `http://localhost:8082/api/tecnicos`**

```json
{"nombres":"Ana","apellidos":"Torres","correo":"ana@example.test","especialidad":"Redes"}
```

**3. POST `http://localhost:8083/api/tickets`**

Usar los IDs realmente devueltos en los dos pasos anteriores, no asumir que siempre son 1:

```json
{"idUsuario":1,"idTecnico":1,"asunto":"Sin conexión","descripcion":"La computadora no accede a Internet","prioridad":"ALTA","estado":"ABIERTO"}
```

**4. PATCH `http://localhost:8083/api/tickets/{id}/estado`**

```json
{"estado":"EN_PROCESO"}
```

Repetir con `CERRADO`. Comprobar con GET que el estado se guardó.

### Reglas y errores

- Nombres y apellidos: obligatorios, máximo 100 caracteres.
- Correo: obligatorio, formato válido, máximo 150 y único dentro de cada servicio.
  Se normaliza a minúsculas; nombres y textos se guardan sin espacios en los extremos.
- Especialidad: obligatoria, máximo 120 caracteres.
- Asunto: obligatorio, máximo 160; descripción: obligatoria, máximo 4000.
- IDs: enteros positivos. Prioridades: `BAJA`, `MEDIA`, `ALTA`.
- Estados: `ABIERTO`, `EN_PROCESO`, `CERRADO`. Se escriben exactamente en mayúsculas;
  no se aceptan valores numéricos como sustitutos de las enumeraciones.
- Se permite cambiar entre esos tres estados; la guía no impone una secuencia ni prohíbe reabrir.
- Ticket conserva **solo los IDs**, nunca entidades JPA de otra base. Antes de POST/PUT,
  `ReferenciaService` comprueba su existencia llamando a las API con `RestClient`,
  incluido en Spring Web: no requiere Feign ni descubrimiento de servicios.
- Consultar tickets o cambiar su estado no necesita que los otros dos servicios estén disponibles.
- No hay transacciones distribuidas ni borrado en cascada: borrar un usuario/técnico
  no borra tickets existentes. Pueden conservar IDs históricos sin referencia vigente.
  Para esta práctica, elimina primero los tickets si quieres limpiar sus referencias.
  La comprobación HTTP no garantiza integridad global ante eliminaciones concurrentes.

| Código | Caso |
|---:|---|
| 400 | DTO inválido, ID no positivo, JSON incorrecto, enum/filtro no permitido |
| 404 | El recurso solicitado por ID no existe |
| 409 | Correo duplicado o conflicto de integridad |
| 422 | Usuario o técnico referenciado por un ticket no existe |
| 503 | No se puede comprobar la referencia porque el servicio remoto falla/no responde |

Todas las respuestas de error controladas tienen este formato:

```json
{"timestamp":"2026-11-04T00:00:00Z","codigo":400,"mensaje":"Hay campos inválidos","ruta":"/api/usuarios","errores":{"nombres":"Los nombres son obligatorios"}}
```

No se devuelven trazas ni credenciales en los mensajes de error.

## 7. Postman, pruebas y evidencias

1. Importa `postman/SoporteTecnico.postman_collection.json` en Postman.
2. Confirma las variables de colección `usuarioBaseUrl`, `tecnicoBaseUrl`, `ticketBaseUrl`.
3. Con MySQL y los tres servicios encendidos, ejecuta las carpetas **01, 02 y 03 en orden**,
   usando Collection Runner, una iteración. Los IDs se capturan automáticamente.
4. Revisa los tests y toma capturas de crear ticket, cambiar estado, consultar por ID,
   datos inválidos e ID inexistente. No es necesario capturar contraseñas.
5. La carpeta **04 - Limpieza** elimina únicamente los IDs creados por esa ejecución.
   Ejecútala al terminar; si corres toda la colección, también se ejecuta la limpieza.
6. Para demostrar 503, detén Usuario e intenta crear un ticket con IDs positivos;
   la API responderá que no puede comprobar la referencia. Después reinicia Usuario.

`mvn verify` prueba controladores, validaciones y reglas de negocio con repositorios
simulados (Mockito), sin base auxiliar. Hay además pruebas HTTP de las referencias.
La **persistencia JPA real se comprueba con la colección Postman contra XAMPP**.

Opcional: con los tres servicios y XAMPP ya iniciados, Python 3, Node.js y `npx`:

```powershell
python scripts/verificar-postman-real.py
```

Ejecuta esa misma colección con Newman contra `8081/8082/8083`, descarga Newman
bajo demanda sin instalarlo globalmente y genera reportes en `evidencias/ejecucion-real-*`.
No crea otro servidor, no usa una base temporal y no detiene tus servicios.
La carpeta 04 limpia solo los registros creados por la ejecución de la colección;
no elimina bases ni registros previos. No intercalar ejecuciones concurrentes de
la misma colección porque sus variables compartidas contienen los IDs de limpieza.

Consulta el alcance real de la verificación en [docs/verificacion.md](docs/verificacion.md).
Conclusiones y preparación: [docs/conclusiones.md](docs/conclusiones.md),
[docs/sustentacion.md](docs/sustentacion.md).
