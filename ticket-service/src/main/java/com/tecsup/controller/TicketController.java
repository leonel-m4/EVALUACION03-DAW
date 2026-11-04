package com.tecsup.controller;

import com.tecsup.dto.*;
import com.tecsup.services.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@Validated
public class TicketController {
    private final TicketService service;

    public TicketController(TicketService service) { this.service = service; }

    @GetMapping
    public List<TicketResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public TicketResponse obtener(@PathVariable @Positive Long id) { return service.obtener(id); }

    @PostMapping
    public ResponseEntity<TicketResponse> registrar(@Valid @RequestBody TicketDTO dto) {
        TicketResponse ticket = service.registrar(dto);
        return ResponseEntity.created(URI.create("/api/tickets/" + ticket.id())).body(ticket);
    }

    @PutMapping("/{id}")
    public TicketResponse actualizar(@PathVariable @Positive Long id, @Valid @RequestBody TicketDTO dto) {
        return service.actualizar(id, dto);
    }

    @PatchMapping("/{id}/estado")
    public TicketResponse actualizarEstado(@PathVariable @Positive Long id,
                                           @Valid @RequestBody ActualizarEstadoDTO dto) {
        return service.actualizarEstado(id, dto.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
