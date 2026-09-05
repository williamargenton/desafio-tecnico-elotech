package br.com.elotech.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.elotech.dto.LoginRequest;
import br.com.elotech.dto.LoginResponse;
import br.com.elotech.service.AutenticacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/autenticacao")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    public LoginResponse autenticar(@Valid @RequestBody LoginRequest request) {
        return autenticacaoService.autenticar(request);
    }
}