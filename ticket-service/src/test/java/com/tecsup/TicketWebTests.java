package com.tecsup;

import com.tecsup.controller.TicketController;
import com.tecsup.model.*;
import com.tecsup.repository.TicketRepository;
import com.tecsup.services.*;
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

@WebMvcTest(TicketController.class)
@Import(TicketService.class)
class TicketWebTests {
    private static final String BODY = "{\"idUsuario\":1,\"idTecnico\":2,\"asunto\":\" Sin conexión \",\"descripcion\":\"Sin Internet\",\"prioridad\":\"MEDIA\",\"estado\":\"ABIERTO\"}";
    @Autowired MockMvc mvc;
    @MockitoBean TicketRepository repository;

    @Test
    void crudDeTickets() throws Exception {
        Ticket ticket = ticket();
        when(repository.findById(1L)).thenReturn(Optional.of(ticket));
        when(repository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(ticket));
        when(repository.save(any())).thenAnswer(invocation -> {
            Ticket saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });
        mvc.perform(post("/api/tickets").contentType(APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/tickets/1"))
                .andExpect(jsonPath("$.asunto").value("Sin conexión"));
        mvc.perform(get("/api/tickets")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/tickets/1")).andExpect(status().isOk());
        mvc.perform(put("/api/tickets/1").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isOk());
        mvc.perform(delete("/api/tickets/1")).andExpect(status().isNoContent());
        verify(repository).delete(ticket);
    }

    @Test
    void cambioDeEstado() throws Exception {
        Ticket ticket = ticket();
        when(repository.findById(1L)).thenReturn(Optional.of(ticket));
        when(repository.save(ticket)).thenReturn(ticket);
        mvc.perform(patch("/api/tickets/1/estado").contentType(APPLICATION_JSON).content("{\"estado\":\"CERRADO\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CERRADO"));
        assertThat(ticket.getAsunto()).isEqualTo("Original");
    }

    @Test
    void validaCamposAntesDeGuardar() throws Exception {
        mvc.perform(post("/api/tickets").contentType(APPLICATION_JSON)
                .content("{\"idUsuario\":0,\"idTecnico\":-1,\"asunto\":\" \",\"descripcion\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.idUsuario").exists())
                .andExpect(jsonPath("$.errores.idTecnico").exists()).andExpect(jsonPath("$.errores.asunto").exists())
                .andExpect(jsonPath("$.errores.descripcion").exists()).andExpect(jsonPath("$.errores.prioridad").exists())
                .andExpect(jsonPath("$.errores.estado").exists());
        verifyNoInteractions(repository);
    }

    @Test
    void validaLongitudesYJson() throws Exception {
        mvc.perform(post("/api/tickets").contentType(APPLICATION_JSON)
                .content(BODY.replace(" Sin conexión ", "x".repeat(161)).replace("Sin Internet", "x".repeat(4001))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.asunto").exists())
                .andExpect(jsonPath("$.errores.descripcion").exists());
        mvc.perform(post("/api/tickets").contentType(APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"URGENTE", "1", ""})
    void prioridadInvalida(String valor) throws Exception {
        mvc.perform(post("/api/tickets").contentType(APPLICATION_JSON).content(BODY.replace("MEDIA", valor)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void prioridadNumericaNoAceptada() throws Exception {
        mvc.perform(post("/api/tickets").contentType(APPLICATION_JSON).content(BODY.replace("\"MEDIA\"", "1")))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RESUELTO", "1", ""})
    void estadoInvalido(String valor) throws Exception {
        mvc.perform(patch("/api/tickets/1/estado").contentType(APPLICATION_JSON)
                .content("{\"estado\":\"" + valor + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void estadoObligatorio() throws Exception {
        mvc.perform(patch("/api/tickets/1/estado").contentType(APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.estado").exists());
    }

    @Test
    void inexistenteEnTodasLasOperaciones() throws Exception {
        mvc.perform(get("/api/tickets/1")).andExpect(status().isNotFound());
        mvc.perform(put("/api/tickets/1").contentType(APPLICATION_JSON).content(BODY)).andExpect(status().isNotFound());
        mvc.perform(patch("/api/tickets/1/estado").contentType(APPLICATION_JSON).content("{\"estado\":\"CERRADO\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/tickets/1")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    void idInvalido(String id) throws Exception {
        mvc.perform(get("/api/tickets/{id}", id)).andExpect(status().isBadRequest());
    }

    private Ticket ticket() {
        Ticket ticket = new Ticket();
        ReflectionTestUtils.setField(ticket, "id", 1L);
        ticket.setIdUsuario(1L);
        ticket.setIdTecnico(2L);
        ticket.setAsunto("Original");
        ticket.setDescripcion("Detalle");
        ticket.setPrioridad(PrioridadTicket.MEDIA);
        ticket.setEstado(EstadoTicket.ABIERTO);
        return ticket;
    }
}
