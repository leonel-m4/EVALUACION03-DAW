package com.tecsup;

import com.tecsup.controller.UsuarioController;
import com.tecsup.model.Usuario;
import com.tecsup.repository.UsuarioRepository;
import com.tecsup.services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Pruebas unitarias HTTP + lógica, con repositorio simulado: ninguna base auxiliar.
// La persistencia real se comprueba ejecutando Postman contra MariaDB de XAMPP.
@WebMvcTest(UsuarioController.class)
@Import(UsuarioService.class)
class UsuarioWebTests {
    private static final String BODY = "{\"nombres\":\" Leonel \",\"apellidos\":\"Retamozo\",\"correo\":\"LEONEL@example.test\"}";
    @Autowired MockMvc mvc;
    @MockitoBean UsuarioRepository repository;

    @Test
    void crudYNormalizacion() throws Exception {
        Usuario usuario = usuario();
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(usuario));
        when(repository.save(any())).thenAnswer(invocation -> {
            Usuario saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });
        mvc.perform(post("/api/usuarios").contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/usuarios/1"))
                .andExpect(jsonPath("$.nombres").value("Leonel"))
                .andExpect(jsonPath("$.correo").value("leonel@example.test"));
        mvc.perform(get("/api/usuarios")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/usuarios/1")).andExpect(status().isOk());
        mvc.perform(put("/api/usuarios/1").contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/usuarios/1")).andExpect(status().isNoContent());
        verify(repository).delete(usuario);
        verify(repository, times(2)).save(any());
    }

    @Test
    void validaCamposAntesDeAccederAlRepositorio() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(APPLICATION_JSON)
                .content("{\"nombres\":\"\",\"apellidos\":\" \",\"correo\":\"mal\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.nombres").exists())
                .andExpect(jsonPath("$.errores.apellidos").exists()).andExpect(jsonPath("$.errores.correo").exists());
        verifyNoInteractions(repository);
    }

    @Test
    void validaLongitudYJson() throws Exception {
        mvc.perform(post("/api/usuarios").contentType(APPLICATION_JSON).content(BODY.replace(" Leonel ", "x".repeat(101))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.nombres").exists());
        mvc.perform(post("/api/usuarios").contentType(APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void correoDuplicadoAlCrearYActualizarNoGuarda() throws Exception {
        Usuario usuario = usuario();
        when(repository.existsByCorreo("leonel@example.test")).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));
        when(repository.existsByCorreoAndIdNot("leonel@example.test", 1L)).thenReturn(true);
        mvc.perform(post("/api/usuarios").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isConflict());
        mvc.perform(put("/api/usuarios/1").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isConflict());
        verify(repository, never()).save(any());
        assertThat(usuario.getNombres()).isEqualTo("Original");
    }

    @Test
    void inexistenteEnGetPutDelete() throws Exception {
        mvc.perform(get("/api/usuarios/1")).andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value(404));
        mvc.perform(put("/api/usuarios/1").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/usuarios/1")).andExpect(status().isNotFound());
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    void idInvalido(String id) throws Exception {
        mvc.perform(get("/api/usuarios/{id}", id)).andExpect(status().isBadRequest());
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 1L);
        usuario.setNombres("Original");
        usuario.setApellidos("Retamozo");
        usuario.setCorreo("leonel@example.test");
        return usuario;
    }
}
