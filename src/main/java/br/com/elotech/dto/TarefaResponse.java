package br.com.elotech.dto;

import java.time.Instant;
import java.time.LocalDate;

import br.com.elotech.entity.Tarefa;
import br.com.elotech.entity.enums.PrioridadeTarefa;
import br.com.elotech.entity.enums.StatusTarefa;

public record TarefaResponse(
    Long id,
    String titulo,
    String descricao,
    StatusTarefa status,
    PrioridadeTarefa prioridade,
    Instant criadaEm,
    Instant atualizadaEm,
    LocalDate prazo,
    Long projetoId,
    UsuarioResponse responsavel
) {
    public static TarefaResponse de(Tarefa tarefa) {
        return new TarefaResponse(
            tarefa.getId(),
            tarefa.getTitulo(),
            tarefa.getDescricao(),
            tarefa.getStatus(),
            tarefa.getPrioridade(),
            tarefa.getCriadaEm(),
            tarefa.getAtualizadaEm(),
            tarefa.getPrazo(),
            tarefa.getProjeto().getId(),
            UsuarioResponse.de(tarefa.getResponsavel())
        );
    }
}