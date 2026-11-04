# Conclusiones individuales

Propuesta de conclusiones técnicas: leer, comprobar en la ejecución y adaptar a tu
propia explicación antes de la sustentación; no memorizar resultados que no hayas observado.

1. **Separación de responsabilidades.** Usuario, Técnico y Ticket pueden ejecutarse
   por separado porque cada uno contiene su API, lógica y persistencia. Tener tres
   bases distintas evita que un servicio dependa de las tablas internas de otro.
2. **Persistencia con JPA.** Las entidades representan tablas, `JpaRepository`
   proporciona operaciones de persistencia y `@Transactional` delimita los cambios.
   `GenerationType.IDENTITY` deja a MySQL generar los IDs.
3. **DTO y validaciones.** Separar los datos de entrada de la entidad evita que el
   cliente asigne campos internos. `@Valid`, `@NotBlank`, `@Email`, `@Positive` y los
   enum permiten rechazar entradas inválidas antes de guardar datos.
4. **API REST y errores.** Los métodos HTTP y sus códigos expresan el resultado:
   201 al crear, 204 al eliminar, 400 al validar y 404 cuando falta un recurso.
   `GlobalExceptionHandler` transforma excepciones en respuestas JSON uniformes.
5. **Configuración y pruebas.** Los perfiles separan desarrollo y producción;
   `update` y `validate` tienen propósitos diferentes. Las pruebas JUnit con Mockito
   verifican el comportamiento aislado sin bases auxiliares; Postman comprueba el
   flujo completo y la persistencia real en MariaDB de XAMPP.

## Dificultad técnica y solución

**Problema abordado:** comprobar que los IDs de usuario y técnico sean válidos sin
crear relaciones JPA o claves foráneas entre bases de microservicios.

**Solución implementada:** Ticket guarda únicamente los IDs. Antes de crear o
actualizar un ticket, `ReferenciaService` consulta las API de Usuario y Técnico con
`RestClient`. Una referencia inexistente produce 422 y un servicio no disponible
produce 503, sin guardar la operación fallida. Los tiempos de espera están limitados.

**Límite reconocido:** esta validación no es una transacción distribuida: después
se podría eliminar una referencia. Para el alcance del laboratorio se documenta
este comportamiento y no se realiza borrado en cascada entre servicios.
