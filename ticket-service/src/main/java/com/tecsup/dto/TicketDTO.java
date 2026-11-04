package com.tecsup.dto;

import com.tecsup.model.*;
import jakarta.validation.constraints.*;

public record TicketDTO(
        @NotNull(message = "El usuario es obligatorio")
        @Positive(message = "El ID de usuario debe ser positivo") Long idUsuario,
        @NotNull(message = "El técnico es obligatorio")
        @Positive(message = "El ID de técnico debe ser positivo") Long idTecnico,
        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 160, message = "Máximo 160 caracteres") String asunto,
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 4000, message = "Máximo 4000 caracteres") String descripcion,
        @NotNull(message = "La prioridad es obligatoria") PrioridadTicket prioridad,
        @NotNull(message = "El estado es obligatorio") EstadoTicket estado) {
}
