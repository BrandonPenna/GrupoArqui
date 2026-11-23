package com.ticketya.catalogo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @Schema(description = "Identificador del usuario", example = "cliente-1")
    private String username;

    @Schema(description = "Contraseña del usuario", example = "1234")
    private String password;

    @Schema(description = "Rol solicitado (CLIENTE, ORGANIZADOR, ADMIN, PERSONAL_ACCESO)", example = "CLIENTE")
    private String rol;
}
