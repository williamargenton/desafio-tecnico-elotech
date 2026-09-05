package br.com.elotech.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import br.com.elotech.config.exception.ElotechException;
import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.entity.Projeto;
import br.com.elotech.entity.Tarefa;
import br.com.elotech.entity.Usuario;
import br.com.elotech.entity.enums.Perfil;
import br.com.elotech.entity.enums.PrioridadeTarefa;
import br.com.elotech.entity.enums.StatusTarefa;
import br.com.elotech.repository.TarefaRepository;

@Component
public class ValidadorTarefa {

    private static final int LIMITE_TAREFAS_EM_ANDAMENTO = 5;

    private final TarefaRepository tarefaRepository;

    public ValidadorTarefa(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public void validarResponsavel(
        Projeto projeto,
        Usuario responsavel
    ) {
        if (!projeto.possuiMembro(responsavel.getId())) {
            throw new ElotechException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "O responsável deve ser membro do projeto"
            );
        }
    }

    public void validarCriacao(
        StatusTarefa status,
        PrioridadeTarefa prioridade,
        Usuario responsavel,
        UsuarioAutenticado usuarioAutenticado,
        Projeto projeto
    ) {
        validarConclusaoTarefaCritica(
            null,
            status,
            prioridade,
            usuarioAutenticado,
            projeto
        );

        validarLimiteWip(
            null,
            status,
            responsavel,
            null
        );
    }

    public void validarAtualizacao(
        Tarefa tarefaAtual,
        StatusTarefa proximoStatus,
        PrioridadeTarefa proximaPrioridade,
        Usuario responsavel,
        UsuarioAutenticado usuarioAutenticado,
        Projeto projeto
    ) {
        validarTransicaoStatus(
            tarefaAtual.getStatus(),
            proximoStatus
        );

        validarConclusaoTarefaCritica(
            tarefaAtual,
            proximoStatus,
            proximaPrioridade,
            usuarioAutenticado,
            projeto
        );

        validarLimiteWip(
            tarefaAtual.getStatus(),
            proximoStatus,
            responsavel,
            tarefaAtual
        );
    }

    private void validarTransicaoStatus(
        StatusTarefa statusAtual,
        StatusTarefa proximoStatus
    ) {
        if (!statusAtual.podeTransicionarPara(proximoStatus)) {
            throw new ElotechException(
                HttpStatus.CONFLICT,
                "Transição de status inválida de "
                    + statusAtual
                    + " para "
                    + proximoStatus
            );
        }
    }

    private void validarConclusaoTarefaCritica(
        Tarefa tarefaAtual,
        StatusTarefa proximoStatus,
        PrioridadeTarefa proximaPrioridade,
        UsuarioAutenticado usuarioAutenticado,
        Projeto projeto
    ) {
        boolean tarefaEraCritica = tarefaAtual != null
            && tarefaAtual.getPrioridade() == PrioridadeTarefa.CRITICAL;

        boolean tarefaSeraCritica =
            proximaPrioridade == PrioridadeTarefa.CRITICAL;

        boolean concluindo =
            proximoStatus == StatusTarefa.DONE;

        boolean administradorDono =
            usuarioAutenticado.perfil() == Perfil.ADMIN
                && projeto.getDono()
                    .getId()
                    .equals(usuarioAutenticado.id());

        if (
            concluindo
                && (tarefaEraCritica || tarefaSeraCritica)
                && !administradorDono
        ) {
            throw new ElotechException(
                HttpStatus.FORBIDDEN,
                "Somente o ADMIN do projeto pode concluir uma tarefa CRITICAL"
            );
        }
    }

    private void validarLimiteWip(
        StatusTarefa statusAtual,
        StatusTarefa proximoStatus,
        Usuario responsavel,
        Tarefa tarefaAtual
    ) {
        boolean responsavelFoiAlterado =
            tarefaAtual != null
                && !tarefaAtual.getResponsavel()
                    .getId()
                    .equals(responsavel.getId());

        boolean entrandoEmAndamento =
            proximoStatus == StatusTarefa.IN_PROGRESS
                && (
                    statusAtual != StatusTarefa.IN_PROGRESS
                        || responsavelFoiAlterado
                );

        if (!entrandoEmAndamento) {
            return;
        }

        long quantidadeEmAndamento =
            tarefaRepository.countByResponsavelIdAndStatus(
                responsavel.getId(),
                StatusTarefa.IN_PROGRESS
            );

        if (quantidadeEmAndamento >= LIMITE_TAREFAS_EM_ANDAMENTO) {
            throw new ElotechException(
                HttpStatus.CONFLICT,
                "Limite WIP atingido: o responsável já possui "
                    + LIMITE_TAREFAS_EM_ANDAMENTO
                    + " tarefas IN_PROGRESS"
            );
        }
    }
}