package com.tecsup.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;

@Service
public class ReferenciaService {
    private final RestClient usuarios;
    private final RestClient tecnicos;

    public ReferenciaService(@Value("${servicios.usuario-url}") String usuarioUrl,
                             @Value("${servicios.tecnico-url}") String tecnicoUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        usuarios = RestClient.builder().baseUrl(usuarioUrl).requestFactory(factory).build();
        tecnicos = RestClient.builder().baseUrl(tecnicoUrl).requestFactory(factory).build();
    }

    public void validar(Long idUsuario, Long idTecnico) {
        comprobar(usuarios, "/api/usuarios/{id}", idUsuario, "Usuario");
        comprobar(tecnicos, "/api/tecnicos/{id}", idTecnico, "Técnico");
    }

    private void comprobar(RestClient client, String path, Long id, String recurso) {
        try {
            client.get().uri(path, id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        recurso + " referenciado no existe con ID: " + id);
            }
            throw noDisponible(recurso);
        } catch (ResourceAccessException ex) {
            throw noDisponible(recurso);
        }
    }

    private ResponseStatusException noDisponible(String recurso) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de " + recurso + " no está disponible. Intenta nuevamente más tarde");
    }
}
