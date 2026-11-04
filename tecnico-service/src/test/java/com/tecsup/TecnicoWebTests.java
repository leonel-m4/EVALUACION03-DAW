package com.tecsup;

import com.tecsup.controller.TecnicoController;
import com.tecsup.model.Tecnico;
import com.tecsup.repository.TecnicoRepository;
import com.tecsup.services.TecnicoService;
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

@WebMvcTest(TecnicoController.class)
@Import(TecnicoService.class)
class TecnicoWebTests {
    private static final String BODY = "{\"nombres\":\" Ana \",\"apellidos\":\"Torres\",\"correo\":\"ANA@example.test\",\"especialidad\":\"Redes\"}";
    @Autowired MockMvc mvc;
    @MockitoBean TecnicoRepository repository;

    @Test
    void crudYNormalizacion() throws Exception {
        Tecnico tecnico = tecnico();
        when(repository.findById(1L)).thenReturn(Optional.of(tecnico));
        when(repository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(tecnico));
        when(repository.save(any())).thenAnswer(invocation -> {
            Tecnico saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });
        mvc.perform(post("/api/tecnicos").contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/tecnicos/1"))
                .andExpect(jsonPath("$.nombres").value("Ana"))
                .andExpect(jsonPath("$.correo").value("ana@example.test"));
        mvc.perform(get("/api/tecnicos")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/tecnicos/1")).andExpect(status().isOk());
        mvc.perform(put("/api/tecnicos/1").contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk()).andExpect(jsonPath("$.especialidad").value("Redes"));
        mvc.perform(delete("/api/tecnicos/1")).andExpect(status().isNoContent());
        verify(repository).delete(tecnico);
        verify(repository, times(2)).save(any());
    }

    @Test
    void validaCamposSinAccederAlRepositorio() throws Exception {
        mvc.perform(post("/api/tecnicos").contentType(APPLICATION_JSON)
                .content("{\"nombres\":\"\",\"apellidos\":\" \",\"correo\":\"mal\",\"especialidad\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.nombres").exists())
                .andExpect(jsonPath("$.errores.apellidos").exists()).andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.especialidad").exists());
        verifyNoInteractions(repository);
    }

    @Test
    void validaLongitudYJson() throws Exception {
        mvc.perform(post("/api/tecnicos").contentType(APPLICATION_JSON).content(BODY.replace("Redes", "x".repeat(121))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.especialidad").exists());
        mvc.perform(post("/api/tecnicos").contentType(APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void correoDuplicadoAlCrearYActualizarNoGuarda() throws Exception {
        Tecnico tecnico = tecnico();
        when(repository.existsByCorreo("ana@example.test")).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(tecnico));
        when(repository.existsByCorreoAndIdNot("ana@example.test", 1L)).thenReturn(true);
        mvc.perform(post("/api/tecnicos").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isConflict());
        mvc.perform(put("/api/tecnicos/1").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isConflict());
        verify(repository, never()).save(any());
        assertThat(tecnico.getNombres()).isEqualTo("Original");
    }

    @Test
    void inexistenteEnGetPutDelete() throws Exception {
        mvc.perform(get("/api/tecnicos/1")).andExpect(status().isNotFound());
        mvc.perform(put("/api/tecnicos/1").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/tecnicos/1")).andExpect(status().isNotFound());
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    void idInvalido(String id) throws Exception {
        mvc.perform(get("/api/tecnicos/{id}", id)).andExpect(status().isBadRequest());
    }

    private Tecnico tecnico() {
        Tecnico tecnico = new Tecnico();
        ReflectionTestUtils.setField(tecnico, "id", 1L);
        tecnico.setNombres("Original");
        tecnico.setApellidos("Torres");
        tecnico.setCorreo("ana@example.test");
        tecnico.setEspecialidad("Redes");
        return tecnico;
    }
}
