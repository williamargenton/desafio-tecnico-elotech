package br.com.elotech.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.elotech.config.exception.ElotechException;
import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.dto.ProjetoRequest;
import br.com.elotech.dto.ProjetoResponse;
import br.com.elotech.entity.Projeto;
import br.com.elotech.entity.Usuario;
import br.com.elotech.entity.enums.Perfil;
import br.com.elotech.repository.ProjetoRepository;
import br.com.elotech.repository.TarefaRepository;
import br.com.elotech.repository.UsuarioRepository;

@Service
@Transactional
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefaRepository;
    private final AcessoProjetoService acessoProjetoService;

    public ProjetoService(
        ProjetoRepository projetoRepository,
        UsuarioRepository usuarioRepository,
        TarefaRepository tarefaRepository,
        AcessoProjetoService acessoProjetoService
    ) {
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarefaRepository = tarefaRepository;
        this.acessoProjetoService = acessoProjetoService;
    }

    public ProjetoResponse criar(ProjetoRequest request, UsuarioAutenticado usuarioAutenticado) {
        exigirAdministrador(usuarioAutenticado);
        Usuario dono = buscarUsuario(usuarioAutenticado.id());
        Projeto projeto = new Projeto(request.nome(), request.descricao(), dono);
        return ProjetoResponse.de(projetoRepository.save(projeto));
    }

    @Transactional(readOnly = true)
    public List<ProjetoResponse> listar(UsuarioAutenticado usuarioAutenticado) {
        return projetoRepository.buscarTodosAcessiveisPeloUsuarioId(usuarioAutenticado.id()).stream()
            .map(ProjetoResponse::de)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProjetoResponse buscar(Long projetoId, UsuarioAutenticado usuarioAutenticado) {
        return ProjetoResponse.de(
            acessoProjetoService.buscarProjetoAcessivel(projetoId, usuarioAutenticado)
        );
    }

    public ProjetoResponse atualizar(
        Long projetoId,
        ProjetoRequest request,
        UsuarioAutenticado usuarioAutenticado
    ) {
        Projeto projeto = acessoProjetoService.buscarProjetoAdministrado(projetoId, usuarioAutenticado);
        projeto.atualizar(request.nome(), request.descricao());
        return ProjetoResponse.de(projeto);
    }

    public void excluir(Long projetoId, UsuarioAutenticado usuarioAutenticado) {
        Projeto projeto = acessoProjetoService.buscarProjetoAdministrado(projetoId, usuarioAutenticado);
        tarefaRepository.deleteByProjetoId(projetoId);
        projetoRepository.delete(projeto);
    }

    public ProjetoResponse adicionarMembro(
        Long projetoId,
        Long usuarioId,
        UsuarioAutenticado usuarioAutenticado
    ) {
        Projeto projeto = acessoProjetoService.buscarProjetoAdministrado(projetoId, usuarioAutenticado);
        Usuario usuario = buscarUsuario(usuarioId);
        if (!projeto.adicionarMembro(usuario)) {
            throw new ElotechException(HttpStatus.CONFLICT, "O usuário já é membro do projeto");
        }
        return ProjetoResponse.de(projeto);
    }

    public void removerMembro(
        Long projetoId,
        Long usuarioId,
        UsuarioAutenticado usuarioAutenticado
    ) {
        Projeto projeto = acessoProjetoService.buscarProjetoAdministrado(projetoId, usuarioAutenticado);
        Usuario usuario = buscarUsuario(usuarioId);

        if (!projeto.possuiMembro(usuarioId)) {
            throw new ElotechException(HttpStatus.NOT_FOUND, "O usuário não é membro do projeto");
        }
        if (projeto.getDono().getId().equals(usuarioId)) {
            throw new ElotechException(HttpStatus.CONFLICT, "O dono não pode ser removido do projeto");
        }
        if (tarefaRepository.existsByProjetoIdAndResponsavelId(projetoId, usuarioId)) {
            throw new ElotechException(
                HttpStatus.CONFLICT,
                "O membro ainda é responsável por tarefas do projeto"
            );
        }

        projeto.removerMembro(usuario);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new ElotechException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }

    private void exigirAdministrador(UsuarioAutenticado usuarioAutenticado) {
        if (usuarioAutenticado.perfil() != Perfil.ADMIN) {
            throw new ElotechException(
                HttpStatus.FORBIDDEN,
                "Somente usuários ADMIN podem criar projetos"
            );
        }
    }
}
