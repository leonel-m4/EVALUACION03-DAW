package com.tecsup.controller;

import com.tecsup.dto.*;
import com.tecsup.services.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Validated
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) { this.service = service; }

    @GetMapping
    public List<UsuarioResponse> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable @Positive Long id) { return service.obtener(id); }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioDTO dto) {
        UsuarioResponse usuario = service.registrar(dto);
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.id())).body(usuario);
    }

    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable @Positive Long id, @Valid @RequestBody UsuarioDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
