package br.com.elotech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjetoRequest(
    @NotBlank(message = "é obrigatório")
    @Size(max = 150, message = "deve ter no máximo 150 caracteres")
    String nome,
    @Size(max = 2000, message = "deve ter no máximo 2000 caracteres")
    String descricao
) {

}
