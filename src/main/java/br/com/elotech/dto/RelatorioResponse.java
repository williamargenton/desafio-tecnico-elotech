package br.com.elotech.dto;

import java.util.Map;

import br.com.elotech.entity.enums.PrioridadeTarefa;
import br.com.elotech.entity.enums.StatusTarefa;

public record RelatorioResponse(
    Map<StatusTarefa, Long> porStatus,
    Map<PrioridadeTarefa, Long> porPrioridade
) {

}