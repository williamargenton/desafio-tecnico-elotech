package br.com.elotech.controller;

import java.net.URI;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.dto.FiltroTarefaRequest;
import br.com.elotech.dto.TarefaRequest;
import br.com.elotech.dto.TarefaResponse;
import br.com.elotech.service.TarefaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/projetos/{projetoId}/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(
        TarefaService tarefaService
    ) {
        this.tarefaService = tarefaService;
    }

    @PostMapping
    public ResponseEntity<TarefaResponse> criar(
        @PathVariable("projetoId") Long projetoId,
        @Valid @RequestBody TarefaRequest request,
        @AuthenticationPrincipal
        UsuarioAutenticado usuarioAutenticado
    ) {
        TarefaResponse tarefa = tarefaService.criar(
            projetoId,
            request,
            usuarioAutenticado
        );

        URI localizacao = URI.create(
            "/projetos/"
                + projetoId
                + "/tarefas/"
                + tarefa.id()
        );

        return ResponseEntity
            .created(localizacao)
            .body(tarefa);
    }

    @GetMapping
    public List<TarefaResponse> listar(
        @PathVariable("projetoId") Long projetoId,
        @Valid
        @ParameterObject
        @ModelAttribute
        FiltroTarefaRequest filtro,
        @AuthenticationPrincipal
        UsuarioAutenticado usuarioAutenticado
    ) {
        return tarefaService.listar(
            projetoId,
            filtro,
            usuarioAutenticado
        );
    }

    @GetMapping("/busca")
    public List<TarefaResponse> buscarPorTexto(
        @PathVariable("projetoId") Long projetoId,
        @RequestParam("texto") String texto,
        @AuthenticationPrincipal
        UsuarioAutenticado usuarioAutenticado
    ) {
        return tarefaService.buscarPorTexto(
            projetoId,
            texto,
            usuarioAutenticado
        );
    }

    @GetMapping("/{tarefaId}")
    public TarefaResponse buscar(
        @PathVariable("projetoId") Long projetoId,
        @PathVariable("tarefaId") Long tarefaId,
        @AuthenticationPrincipal
        UsuarioAutenticado usuarioAutenticado
    ) {
        return tarefaService.buscar(
            projetoId,
            tarefaId,
            usuarioAutenticado
        );
    }

    @PutMapping("/{tarefaId}")
    public TarefaResponse atualizar(
        @PathVariable("projetoId") Long projetoId,
        @PathVariable("tarefaId") Long tarefaId,
        @Valid @RequestBody TarefaRequest request,
        @AuthenticationPrincipal
        UsuarioAutenticado usuarioAutenticado
    ) {
        return tarefaService.atualizar(
            projetoId,
            tarefaId,
            request,
            usuarioAutenticado
        );
    }

    @DeleteMapping("/{tarefaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
        @PathVariable("projetoId") Long projetoId,
        @PathVariable("tarefaId") Long tarefaId,
        @AuthenticationPrincipal
        UsuarioAutenticado usuarioAutenticado
    ) {
        tarefaService.excluir(
            projetoId,
            tarefaId,
            usuarioAutenticado
        );
    }
}