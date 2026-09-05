package br.com.elotech.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.elotech.config.exception.ElotechException;
import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.dto.FiltroTarefaRequest;
import br.com.elotech.dto.TarefaRequest;
import br.com.elotech.dto.TarefaResponse;
import br.com.elotech.entity.Projeto;
import br.com.elotech.entity.Tarefa;
import br.com.elotech.entity.Usuario;
import br.com.elotech.repository.TarefaRepository;
import br.com.elotech.repository.UsuarioRepository;

@Service
@Transactional
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AcessoProjetoService acessoProjetoService;
    private final ValidadorTarefa validadorTarefa;
    private final FabricaConsultaTarefa fabricaConsultaTarefa;

    public TarefaService(
        TarefaRepository tarefaRepository,
        UsuarioRepository usuarioRepository,
        AcessoProjetoService acessoProjetoService,
        ValidadorTarefa validadorTarefa,
        FabricaConsultaTarefa fabricaConsultaTarefa
    ) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.acessoProjetoService = acessoProjetoService;
        this.validadorTarefa = validadorTarefa;
        this.fabricaConsultaTarefa = fabricaConsultaTarefa;
    }

    public TarefaResponse criar(
        Long projetoId,
        TarefaRequest request,
        UsuarioAutenticado usuarioAutenticado
    ) {
        Projeto projeto =
            acessoProjetoService.buscarProjetoAcessivel(
                projetoId,
                usuarioAutenticado
            );

        Usuario responsavel =
            buscarResponsavel(request.responsavelId());

        validadorTarefa.validarResponsavel(
            projeto,
            responsavel
        );

        validadorTarefa.validarCriacao(
            request.status(),
            request.prioridade(),
            responsavel,
            usuarioAutenticado,
            projeto
        );

        Tarefa tarefa = new Tarefa(
            request.titulo(),
            request.descricao(),
            request.status(),
            request.prioridade(),
            request.prazo(),
            projeto,
            responsavel
        );

        return TarefaResponse.de(
            tarefaRepository.save(tarefa)
        );
    }

    public TarefaResponse atualizar(
        Long projetoId,
        Long tarefaId,
        TarefaRequest request,
        UsuarioAutenticado usuarioAutenticado
    ) {
        Projeto projeto =
            acessoProjetoService.buscarProjetoAcessivel(
                projetoId,
                usuarioAutenticado
            );

        Tarefa tarefa =
            buscarTarefa(projetoId, tarefaId);

        Usuario responsavel =
            buscarResponsavel(request.responsavelId());

        validadorTarefa.validarResponsavel(
            projeto,
            responsavel
        );

        validadorTarefa.validarAtualizacao(
            tarefa,
            request.status(),
            request.prioridade(),
            responsavel,
            usuarioAutenticado,
            projeto
        );

        tarefa.atualizar(
            request.titulo(),
            request.descricao(),
            request.status(),
            request.prioridade(),
            request.prazo(),
            responsavel
        );

        return TarefaResponse.de(tarefa);
    }

    @Transactional(readOnly = true)
    public TarefaResponse buscar(
        Long projetoId,
        Long tarefaId,
        UsuarioAutenticado usuarioAutenticado
    ) {
        acessoProjetoService.buscarProjetoAcessivel(
            projetoId,
            usuarioAutenticado
        );

        return TarefaResponse.de(
            buscarTarefa(projetoId, tarefaId)
        );
    }

    public void excluir(
        Long projetoId,
        Long tarefaId,
        UsuarioAutenticado usuarioAutenticado
    ) {
        acessoProjetoService.buscarProjetoAcessivel(
            projetoId,
            usuarioAutenticado
        );

        tarefaRepository.delete(
            buscarTarefa(projetoId, tarefaId)
        );
    }

    @Transactional(readOnly = true)
    public List<TarefaResponse> listar(
        Long projetoId,
        FiltroTarefaRequest filtro,
        UsuarioAutenticado usuarioAutenticado
    ) {
        acessoProjetoService.buscarProjetoAcessivel(
            projetoId,
            usuarioAutenticado
        );

        Specification<Tarefa> especificacao =
            fabricaConsultaTarefa.criarEspecificacao(
                projetoId,
                filtro
            );

        Sort ordenacao =
            fabricaConsultaTarefa.criarOrdenacao(filtro);

        return tarefaRepository
            .findAll(especificacao, ordenacao)
            .stream()
            .map(TarefaResponse::de)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TarefaResponse> buscarPorTexto(
        Long projetoId,
        String texto,
        UsuarioAutenticado usuarioAutenticado
    ) {
        acessoProjetoService.buscarProjetoAcessivel(
            projetoId,
            usuarioAutenticado
        );

        if (texto == null || texto.isBlank()) {
            throw new ElotechException(
                HttpStatus.BAD_REQUEST,
                "O texto de busca não pode estar vazio"
            );
        }

        return tarefaRepository
            .buscarPorTexto(
                projetoId,
                texto.trim()
            )
            .stream()
            .map(TarefaResponse::de)
            .toList();
    }

    private Tarefa buscarTarefa(
        Long projetoId,
        Long tarefaId
    ) {
        return tarefaRepository
            .findByIdAndProjetoId(
                tarefaId,
                projetoId
            )
            .orElseThrow(
                () -> new ElotechException(
                    HttpStatus.NOT_FOUND,
                    "Tarefa não encontrada"
                )
            );
    }

    private Usuario buscarResponsavel(
        Long usuarioId
    ) {
        return usuarioRepository
            .findById(usuarioId)
            .orElseThrow(
                () -> new ElotechException(
                    HttpStatus.NOT_FOUND,
                    "Responsável não encontrado"
                )
            );
    }
}