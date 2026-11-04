package com.tecsup.controller;

import com.tecsup.dto.*;
import com.tecsup.services.TecnicoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tecnicos")
@Validated
public class TecnicoController {
    private final TecnicoService service;

    public TecnicoController(TecnicoService service) { this.service = service; }

    @GetMapping
    public List<TecnicoResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public TecnicoResponse obtener(@PathVariable @Positive Long id) { return service.obtener(id); }

    @PostMapping
    public ResponseEntity<TecnicoResponse> registrar(@Valid @RequestBody TecnicoDTO dto) {
        TecnicoResponse tecnico = service.registrar(dto);
        return ResponseEntity.created(URI.create("/api/tecnicos/" + tecnico.id())).body(tecnico);
    }

    @PutMapping("/{id}")
    public TecnicoResponse actualizar(@PathVariable @Positive Long id, @Valid @RequestBody TecnicoDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
