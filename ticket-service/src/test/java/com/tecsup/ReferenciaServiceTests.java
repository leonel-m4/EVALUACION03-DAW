package com.tecsup;

import com.sun.net.httpserver.HttpServer;
import com.tecsup.services.ReferenciaService;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.*;

class ReferenciaServiceTests {
    private HttpServer server;
    private ReferenciaService service;
    private final AtomicInteger usuarioStatus = new AtomicInteger(200);
    private final AtomicInteger tecnicoStatus = new AtomicInteger(200);

    @BeforeEach
    void iniciar() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/usuarios/", exchange -> {
            exchange.sendResponseHeaders(usuarioStatus.get(), -1);
            exchange.close();
        });
        server.createContext("/api/tecnicos/", exchange -> {
            exchange.sendResponseHeaders(tecnicoStatus.get(), -1);
            exchange.close();
        });
        server.start();
        String url = "http://127.0.0.1:" + server.getAddress().getPort();
        service = new ReferenciaService(url, url);
    }

    @AfterEach
    void detener() { server.stop(0); }

    @Test
    void aceptaReferenciasExistentesPorHttp() {
        assertThatCode(() -> service.validar(1L, 2L)).doesNotThrowAnyException();
    }

    @Test
    void usuarioInexistenteDevuelve422() {
        usuarioStatus.set(404);
        comprobarError(422, "Usuario referenciado no existe");
    }

    @Test
    void tecnicoInexistenteDevuelve422() {
        tecnicoStatus.set(404);
        comprobarError(422, "Técnico referenciado no existe");
    }

    @Test
    void errorRemotoDevuelve503SinExponerLaRespuestaInterna() {
        tecnicoStatus.set(500);
        comprobarError(503, "no está disponible");
    }

    @Test
    void conexionRechazadaDevuelve503() {
        server.stop(0);
        comprobarError(503, "no está disponible");
    }

    private void comprobarError(int codigo, String mensaje) {
        assertThatThrownBy(() -> service.validar(1L, 2L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode().value()).isEqualTo(codigo);
                    assertThat(ex.getReason()).contains(mensaje);
                });
    }
}
