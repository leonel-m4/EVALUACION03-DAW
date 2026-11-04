package com.tecsup.dto;

import com.tecsup.model.*;

public record TicketResponse(Long id, Long idUsuario, Long idTecnico, String asunto,
                             String descripcion, PrioridadTicket prioridad, EstadoTicket estado) {
}
