package com.tecsup.services;

import com.tecsup.dto.*;
import com.tecsup.exception.ResourceNotFoundException;
import com.tecsup.model.Usuario;
import com.tecsup.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public List<UsuarioResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(this::respuesta).toList();
    }

    public UsuarioResponse obtener(Long id) {
        return respuesta(buscar(id));
    }

    @Transactional
    public UsuarioResponse registrar(UsuarioDTO dto) {
        String correo = dto.correo().strip().toLowerCase(Locale.ROOT);
        if (repository.existsByCorreo(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        Usuario usuario = new Usuario();
        copiar(dto, usuario, correo);
        return respuesta(repository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioDTO dto) {
        Usuario usuario = buscar(id);
        String correo = dto.correo().strip().toLowerCase(Locale.ROOT);
        if (repository.existsByCorreoAndIdNot(correo, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        copiar(dto, usuario, correo);
        return respuesta(repository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    private Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    private void copiar(UsuarioDTO dto, Usuario usuario, String correo) {
        usuario.setNombres(dto.nombres().strip());
        usuario.setApellidos(dto.apellidos().strip());
        usuario.setCorreo(correo);
    }

    private UsuarioResponse respuesta(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombres(), usuario.getApellidos(), usuario.getCorreo());
    }
}
