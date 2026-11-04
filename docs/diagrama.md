# Diagrama de microservicios

![Arquitectura](diagrama.svg)

```mermaid
flowchart TD
    P[Postman] --> U[Usuario Service :8081]
    P --> T[Técnico Service :8082]
    P --> K[Ticket Service :8083]
    U --> DU[(bd_usuario · usuarios)]
    T --> DT[(bd_tecnico · tecnicos)]
    K --> DK[(bd_ticket · tickets)]
    K -. GET por ID antes de POST/PUT .-> U
    K -. GET por ID antes de POST/PUT .-> T
```

- Cada servicio es dueño de sus tablas y tiene su propia cuenta de MySQL.
- Las tres bases pueden vivir en la misma instancia local de MySQL: no comparten tablas.
- Ticket almacena `idUsuario` e `idTecnico` como números. No usa `@ManyToOne` entre servicios.
- Las líneas discontinuas son consultas HTTP mediante Spring Web, no accesos a otra base.
- No existe un componente de descubrimiento, gateway ni contenedores.
