package br.com.elotech.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import br.com.elotech.entity.enums.PrioridadeTarefa;
import br.com.elotech.entity.enums.StatusTarefa;

public record FiltroTarefaRequest(
    StatusTarefa status,
    PrioridadeTarefa prioridade,
    Long responsavel,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataInicial,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dataFinal,
    String ordenarPor
) {

}
