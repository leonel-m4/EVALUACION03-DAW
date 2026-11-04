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

**Problema abordado:** representar al usuario y al técnico de cada ticket sin
compartir tablas ni crear relaciones JPA entre las bases de los microservicios.

**Solución implementada:** Ticket guarda únicamente `idUsuario` e `idTecnico` como
valores numéricos positivos. Al registrar un ticket se usan los IDs obtenidos al
crear primero el usuario y el técnico desde Postman; cada servicio persiste solo
sus propios datos.

**Límite reconocido:** el servicio Ticket no comprueba la existencia de esos IDs;
si se borra el usuario o técnico, el ticket conserva el número. El manejo de ID
inexistente solicitado se aplica a los recursos consultados en cada API (404).
