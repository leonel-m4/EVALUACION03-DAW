package com.tecsup.services;

import com.tecsup.dto.*;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.model.*;
import com.tecsup.repository.TicketRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TicketService {
    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    public List<TicketResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(this::respuesta).toList();
    }

    public TicketResponse obtener(Long id) { return respuesta(buscar(id)); }

    @Transactional
    public TicketResponse registrar(TicketDTO dto) {
        Ticket ticket = new Ticket();
        copiar(dto, ticket);
        return respuesta(repository.save(ticket));
    }

    @Transactional
    public TicketResponse actualizar(Long id, TicketDTO dto) {
        Ticket ticket = buscar(id);
        copiar(dto, ticket);
        return respuesta(repository.save(ticket));
    }

    @Transactional
    public TicketResponse actualizarEstado(Long id, EstadoTicket estado) {
        Ticket ticket = buscar(id);
        ticket.setEstado(estado);
        return respuesta(repository.save(ticket));
    }

    @Transactional
    public void eliminar(Long id) { repository.delete(buscar(id)); }

    private Ticket buscar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Ticket no encontrado con ID: " + id));
    }

    private void copiar(TicketDTO dto, Ticket ticket) {
        ticket.setIdUsuario(dto.idUsuario());
        ticket.setIdTecnico(dto.idTecnico());
        ticket.setAsunto(dto.asunto().strip());
        ticket.setDescripcion(dto.descripcion().strip());
        ticket.setPrioridad(dto.prioridad());
        ticket.setEstado(dto.estado());
    }

    private TicketResponse respuesta(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getIdUsuario(), ticket.getIdTecnico(),
                ticket.getAsunto(), ticket.getDescripcion(), ticket.getPrioridad(), ticket.getEstado());
    }
}
