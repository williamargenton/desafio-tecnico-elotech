package br.com.elotech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @Email(message = "deve conter um e-mail válido")
    @NotBlank(message = "é obrigatório")
    String email,
    @NotBlank(message = "é obrigatória")
    String senha
) {

}
