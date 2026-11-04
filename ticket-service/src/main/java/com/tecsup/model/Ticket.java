package com.tecsup.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "tickets", indexes = {
        @Index(name = "idx_ticket_usuario", columnList = "id_usuario"),
        @Index(name = "idx_ticket_tecnico", columnList = "id_tecnico"),
        @Index(name = "idx_ticket_estado", columnList = "estado")})
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencias por ID: no hay relaciones JPA entre bases de datos diferentes.
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;
    @Column(name = "id_tecnico", nullable = false)
    private Long idTecnico;
    @Column(nullable = false, length = 160)
    private String asunto;
    @Column(nullable = false, length = 4000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 10)
    private PrioridadTicket prioridad;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private EstadoTicket estado;

    public Long getId() { return id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Long getIdTecnico() { return idTecnico; }
    public void setIdTecnico(Long idTecnico) { this.idTecnico = idTecnico; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public PrioridadTicket getPrioridad() { return prioridad; }
    public void setPrioridad(PrioridadTicket prioridad) { this.prioridad = prioridad; }
    public EstadoTicket getEstado() { return estado; }
    public void setEstado(EstadoTicket estado) { this.estado = estado; }
}
