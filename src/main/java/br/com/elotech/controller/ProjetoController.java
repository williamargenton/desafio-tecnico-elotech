package br.com.elotech.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.dto.MembroRequest;
import br.com.elotech.dto.ProjetoRequest;
import br.com.elotech.dto.ProjetoResponse;
import br.com.elotech.service.ProjetoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/projetos")
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @PostMapping
    public ResponseEntity<ProjetoResponse> criar(
        @Valid @RequestBody ProjetoRequest request,
        @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado
    ) {
        ProjetoResponse projeto = projetoService.criar(request, usuarioAutenticado);
        return ResponseEntity.created(URI.create("/projetos/" + projeto.id())).body(projeto);
    }

    @GetMapping
    public List<ProjetoResponse> listar(
        @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado
    ) {
        return projetoService.listar(usuarioAutenticado);
    }

    @GetMapping("/{projetoId}")
    public ProjetoResponse buscar(
        @PathVariable("projetoId") Long projetoId,
        @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado
    ) {
        return projetoService.buscar(projetoId, usuarioAutenticado);
    }

    @PutMapping("/{projetoId}")
    public ProjetoResponse atualizar(
        @PathVariable("projetoId") Long projetoId,
        @Valid @RequestBody ProjetoRequest request,
        @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado
    ) {
        return projetoService.atualizar(projetoId, request, usuarioAutenticado);
    }

    @DeleteMapping("/{projetoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
        @PathVariable("projetoId") Long projetoId,
        @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado
    ) {
        projetoService.excluir(projetoId, usuarioAutenticado);
    }

    @PostMapping("/{projetoId}/membros")
    public ProjetoResponse adicionarMembro(
        @PathVariable("projetoId") Long projetoId,
        @Valid @RequestBody MembroRequest request,
        @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado
    ) {
        return projetoService.adicionarMembro(projetoId, request.usuarioId(), usuarioAutenticado);
    }

    @DeleteMapping("/{projetoId}/membros/{usuarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerMembro(
        @PathVariable("projetoId") Long projetoId,
        @PathVariable("usuarioId") Long usuarioId,
        @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado
    ) {
        projetoService.removerMembro(projetoId, usuarioId, usuarioAutenticado);
    }
}