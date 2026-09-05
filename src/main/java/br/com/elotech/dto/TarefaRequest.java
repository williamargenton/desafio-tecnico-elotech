package br.com.elotech.dto;

import java.time.LocalDate;

import br.com.elotech.entity.enums.PrioridadeTarefa;
import br.com.elotech.entity.enums.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TarefaRequest(
    @NotBlank(message = "é obrigatório")
    @Size(max = 200, message = "deve ter no máximo 200 caracteres")
    String titulo,
    @Size(max = 4000, message = "deve ter no máximo 4000 caracteres")
    String descricao,
    @NotNull(message = "é obrigatório")
    StatusTarefa status,
    @NotNull(message = "é obrigatória")
    PrioridadeTarefa prioridade,
    LocalDate prazo,
    @NotNull(message = "é obrigatório")
    Long responsavelId
) {

}
