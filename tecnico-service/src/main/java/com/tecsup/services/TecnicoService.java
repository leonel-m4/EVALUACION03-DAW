package com.tecsup.services;

import com.tecsup.dto.*;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.model.Tecnico;
import com.tecsup.repository.TecnicoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class TecnicoService {
    private final TecnicoRepository repository;

    public TecnicoService(TecnicoRepository repository) { this.repository = repository; }

    public List<TecnicoResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(this::respuesta).toList();
    }

    public TecnicoResponse obtener(Long id) { return respuesta(buscar(id)); }

    @Transactional
    public TecnicoResponse registrar(TecnicoDTO dto) {
        String correo = dto.correo().strip().toLowerCase(Locale.ROOT);
        if (repository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        Tecnico tecnico = new Tecnico();
        copiar(dto, tecnico, correo);
        return respuesta(repository.save(tecnico));
    }

    @Transactional
    public TecnicoResponse actualizar(Long id, TecnicoDTO dto) {
        Tecnico tecnico = buscar(id);
        String correo = dto.correo().strip().toLowerCase(Locale.ROOT);
        if (repository.existsByCorreoAndIdNot(correo, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        copiar(dto, tecnico, correo);
        return respuesta(repository.save(tecnico));
    }

    @Transactional
    public void eliminar(Long id) { repository.delete(buscar(id)); }

    private Tecnico buscar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Técnico no encontrado con ID: " + id));
    }

    private void copiar(TecnicoDTO dto, Tecnico tecnico, String correo) {
        tecnico.setNombres(dto.nombres().strip());
        tecnico.setApellidos(dto.apellidos().strip());
        tecnico.setCorreo(correo);
        tecnico.setEspecialidad(dto.especialidad().strip());
    }

    private TecnicoResponse respuesta(Tecnico tecnico) {
        return new TecnicoResponse(tecnico.getId(), tecnico.getNombres(), tecnico.getApellidos(),
                tecnico.getCorreo(), tecnico.getEspecialidad());
    }
}
