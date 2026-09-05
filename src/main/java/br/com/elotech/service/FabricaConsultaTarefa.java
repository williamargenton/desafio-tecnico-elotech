package br.com.elotech.service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import br.com.elotech.config.exception.ElotechException;
import br.com.elotech.dto.FiltroTarefaRequest;
import br.com.elotech.entity.Tarefa;
import br.com.elotech.entity.enums.PrioridadeTarefa;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

@Component
public class FabricaConsultaTarefa {

    private static final String ORDENACAO_PADRAO = "criadaEm";
    private static final String ORDENACAO_PRIORIDADE = "prioridade";

    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of(
        ORDENACAO_PRIORIDADE,
        ORDENACAO_PADRAO,
        "prazo"
    );

    public Specification<Tarefa> criarEspecificacao(
        Long projetoId,
        FiltroTarefaRequest filtro
    ) {
        validarPeriodo(
            filtro.dataInicial(),
            filtro.dataFinal()
        );

        return (raiz, consulta, construtor) -> {
            List<Predicate> predicados = new ArrayList<>();

            predicados.add(
                construtor.equal(
                    raiz.get("projeto").get("id"),
                    projetoId
                )
            );

            if (filtro.status() != null) {
                predicados.add(
                    construtor.equal(
                        raiz.get("status"),
                        filtro.status()
                    )
                );
            }

            if (filtro.prioridade() != null) {
                predicados.add(
                    construtor.equal(
                        raiz.get("prioridade"),
                        filtro.prioridade()
                    )
                );
            }

            if (filtro.responsavel() != null) {
                predicados.add(
                    construtor.equal(
                        raiz.get("responsavel").get("id"),
                        filtro.responsavel()
                    )
                );
            }

            if (filtro.dataInicial() != null) {
                predicados.add(
                    construtor.greaterThanOrEqualTo(
                        raiz.get("criadaEm"),
                        filtro.dataInicial()
                            .atStartOfDay(ZoneOffset.UTC)
                            .toInstant()
                    )
                );
            }

            if (filtro.dataFinal() != null) {
                predicados.add(
                    construtor.lessThan(
                        raiz.get("criadaEm"),
                        filtro.dataFinal()
                            .plusDays(1)
                            .atStartOfDay(ZoneOffset.UTC)
                            .toInstant()
                    )
                );
            }

            if (
                ORDENACAO_PRIORIDADE.equals(
                    filtro.ordenarPor()
                )
            ) {
                Expression<Integer> nivelPrioridade =
                    construtor
                        .<PrioridadeTarefa, Integer>selectCase(
                            raiz.get("prioridade")
                        )
                        .when(PrioridadeTarefa.LOW, 1)
                        .when(PrioridadeTarefa.MEDIUM, 2)
                        .when(PrioridadeTarefa.HIGH, 3)
                        .when(PrioridadeTarefa.CRITICAL, 4)
                        .otherwise(0);

                consulta.orderBy(
                    construtor.desc(nivelPrioridade)
                );
            }

            return construtor.and(
                predicados.toArray(Predicate[]::new)
            );
        };
    }

    public Sort criarOrdenacao(
        FiltroTarefaRequest filtro
    ) {
        String campoOrdenacao =
            filtro.ordenarPor() == null
                ? ORDENACAO_PADRAO
                : filtro.ordenarPor();

        validarCampoOrdenacao(campoOrdenacao);

        if (
            ORDENACAO_PRIORIDADE.equals(campoOrdenacao)
        ) {
            return Sort.unsorted();
        }

        Sort.Direction direcao =
            ORDENACAO_PADRAO.equals(campoOrdenacao)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(
            direcao,
            campoOrdenacao
        );
    }

    private void validarPeriodo(
        LocalDate dataInicial,
        LocalDate dataFinal
    ) {
        if (
            dataInicial != null
                && dataFinal != null
                && dataInicial.isAfter(dataFinal)
        ) {
            throw new ElotechException(
                HttpStatus.BAD_REQUEST,
                "A dataInicial não pode ser posterior à dataFinal"
            );
        }
    }

    private void validarCampoOrdenacao(
        String campoOrdenacao
    ) {
        if (!CAMPOS_ORDENAVEIS.contains(campoOrdenacao)) {
            throw new ElotechException(
                HttpStatus.BAD_REQUEST,
                "Ordenação inválida: use prioridade, criadaEm ou prazo"
            );
        }
    }
}