package com.tecsup.dto;

import com.tecsup.model.EstadoTicket;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoDTO(@NotNull(message = "El estado es obligatorio") EstadoTicket estado) {
}
