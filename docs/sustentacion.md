# Guion de sustentación — 5 a 8 minutos

## Secuencia sugerida (aproximadamente 7 minutos)

1. **0:00–0:45 — Problema y arquitectura.** Una empresa necesita usuarios, técnicos
   y tickets. Mostrar el diagrama, los tres puertos y las tres bases independientes.
2. **0:45–2:00 — Capas.** Mostrar `Ticket`, `TicketDTO`, `TicketRepository`,
   `TicketService` y `TicketController`. Explicar por dónde pasa una petición POST.
3. **2:00–4:00 — Demostración.** Crear usuario y técnico; usar sus IDs para crear
   ticket ABIERTO. Cambiar a EN_PROCESO y luego CERRADO; volver a consultar por ID.
4. **4:00–5:00 — Errores.** Mostrar asunto vacío (400), prioridad inválida (400),
   ticket inexistente (404) y referencia inexistente (422). Explicar el handler.
5. **5:00–6:00 — Persistencia y perfiles.** Mostrar las tablas en MySQL, explicar
   `update` frente a `validate` y las variables de entorno, sin exhibir claves reales.
6. **6:00–7:00 — Conclusiones y límites.** Explicar cinco aprendizajes y la dificultad
   de validar referencias independientes. Distinguir pruebas unitarias sin base
   de las pruebas integrales contra MariaDB real de XAMPP.

## Preguntas frecuentes

- **¿Por qué tres servicios?** Cada uno gestiona una responsabilidad y sus propios
  datos; Ticket usa IDs, no las tablas internas de Usuario/Técnico.
- **¿Qué hace una Entity?** Define el mapeo objeto-tabla. `@Id` identifica la fila;
  `@GeneratedValue` genera el identificador y `@Column` define restricciones.
- **¿Qué hace Repository?** Extiende `JpaRepository`; Spring Data implementa métodos
  CRUD y consultas derivadas como `findByEstado` y `existsByCorreo`.
- **¿Qué hace Service?** Comprueba reglas, normaliza datos y coordina repositorios
  y transacciones. Se inyecta por constructor para facilitar sus pruebas.
- **¿Qué hace Controller?** Recibe HTTP, valida el DTO y devuelve cuerpo/código HTTP.
- **¿Qué pasa con datos inválidos?** No se guardan; el handler devuelve 400 con
  los errores por campo. Una enumeración desconocida también es 400.
- **¿Por qué el DTO es un record?** Es una clase inmutable de Java 17 que genera
  constructor y accesores. No es una entidad JPA ni reemplaza el modelo persistente.
- **¿Cómo se comunican?** Ticket usa `RestClient` de Spring Web y URLs configurables.
  No se necesita Eureka, OpenFeign ni Docker.
- **¿Por qué XAMPP dice MySQL pero la versión es MariaDB?** XAMPP distribuye MariaDB,
  un motor compatible con MySQL. La conexión está verificada con el driver MySQL y
  el dialecto MariaDB de Hibernate, usando tres bases persistentes independientes.
- **¿Por qué 404 y 422?** 404: el recurso solicitado en esa API no existe.
  422: el ticket es sintácticamente válido pero referencia un usuario/técnico inexistente.
- **¿Hay autenticación?** No: no es un requisito de esta guía. No presentar el
  proyecto como una API pública lista para producción.

## Modificaciones pequeñas para practicar

- Cambiar el máximo del asunto en el DTO y en la entidad; ajustar SQL antes de usar `prod`.
- Añadir un filtro por prioridad con un método derivado del repositorio.
- Cambiar el puerto mediante `TICKET_PORT` y actualizar Postman.
- Añadir una validación y una prueba que compruebe su respuesta 400.

No agregar un estado nuevo solo al JSON: primero habría que actualizar el enum,
la restricción CHECK del SQL, las pruebas y la documentación.
