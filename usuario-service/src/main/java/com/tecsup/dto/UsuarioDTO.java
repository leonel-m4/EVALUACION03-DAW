package com.tecsup.dto;

import jakarta.validation.constraints.*;

// Un record es un DTO inmutable; Java 17 genera constructor y accesores.
public record UsuarioDTO(
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Máximo 100 caracteres") String nombres,
        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Máximo 100 caracteres") String apellidos,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "Máximo 150 caracteres") String correo) {
}
