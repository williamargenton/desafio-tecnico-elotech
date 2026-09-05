package br.com.elotech.service;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.dto.RelatorioResponse;
import br.com.elotech.entity.enums.PrioridadeTarefa;
import br.com.elotech.entity.enums.StatusTarefa;
import br.com.elotech.repository.TarefaRepository;

@Service
public class RelatorioService {

    private final TarefaRepository tarefaRepository;
    private final AcessoProjetoService acessoProjetoService;

    public RelatorioService(
        TarefaRepository tarefaRepository,
        AcessoProjetoService acessoProjetoService
    ) {
        this.tarefaRepository = tarefaRepository;
        this.acessoProjetoService = acessoProjetoService;
    }

    @Transactional(readOnly = true)
    public RelatorioResponse gerar(Long projetoId, UsuarioAutenticado usuarioAutenticado) {
        acessoProjetoService.buscarProjetoAcessivel(projetoId, usuarioAutenticado);

        Map<StatusTarefa, Long> porStatus = new EnumMap<>(StatusTarefa.class);
        Map<PrioridadeTarefa, Long> porPrioridade = new EnumMap<>(PrioridadeTarefa.class);
        Arrays.stream(StatusTarefa.values()).forEach(status -> porStatus.put(status, 0L));
        Arrays.stream(PrioridadeTarefa.values()).forEach(prioridade -> porPrioridade.put(prioridade, 0L));

        tarefaRepository.contarPorStatus(projetoId)
            .forEach(resultado -> porStatus.put((StatusTarefa) resultado[0], (Long) resultado[1]));
        tarefaRepository.contarPorPrioridade(projetoId)
            .forEach(resultado -> porPrioridade.put((PrioridadeTarefa) resultado[0], (Long) resultado[1]));

        return new RelatorioResponse(porStatus, porPrioridade);
    }
}
