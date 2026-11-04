package com.tecsup.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> noEncontrado(ResourceNotFoundException ex, HttpServletRequest request) {
        return respuesta(404, ex.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> estado(ResponseStatusException ex, HttpServletRequest request) {
        return respuesta(ex.getStatusCode().value(), ex.getReason(), Map.of(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errores.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return respuesta(400, "Hay campos inválidos", errores, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> parametros(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(e -> errores.put(e.getPropertyPath().toString(), e.getMessage()));
        return respuesta(400, "Los parámetros deben tener valores válidos", errores, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> formato(Exception ex, HttpServletRequest request) {
        return respuesta(400, "Verifica el JSON, los tipos de datos y los valores permitidos", Map.of(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        return respuesta(409, "Los datos entran en conflicto con un registro existente", Map.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse error) {
            return respuesta(error.getStatusCode().value(),
                    "Revisa la ruta, el método HTTP y el tipo de contenido", Map.of(), request);
        }
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return respuesta(500, "Error interno. Contacta con el administrador", Map.of(), request);
    }

    private ResponseEntity<ApiError> respuesta(int codigo, String mensaje, Map<String, String> errores,
                                              HttpServletRequest request) {
        return ResponseEntity.status(codigo).body(
                new ApiError(Instant.now(), codigo, mensaje, request.getRequestURI(), errores));
    }
}
