package br.com.elotech.dto;

import jakarta.validation.constraints.NotNull;

public record MembroRequest(
    @NotNull(message = "é obrigatório")
    Long usuarioId
) {

}
