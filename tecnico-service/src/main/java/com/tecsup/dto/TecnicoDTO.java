package com.tecsup.dto;

import jakarta.validation.constraints.*;

public record TecnicoDTO(
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Máximo 100 caracteres") String nombres,
        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Máximo 100 caracteres") String apellidos,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "Máximo 150 caracteres") String correo,
        @NotBlank(message = "La especialidad es obligatoria")
        @Size(max = 120, message = "Máximo 120 caracteres") String especialidad) {
}
