package br.com.elotech.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.dto.RelatorioResponse;
import br.com.elotech.service.RelatorioService;

@RestController
@RequestMapping("/projetos/{projetoId}/relatorio")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping
    public RelatorioResponse gerar(
        @PathVariable("projetoId") Long projetoId,
        @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado
    ) {
        return relatorioService.gerar(projetoId, usuarioAutenticado);
    }
}