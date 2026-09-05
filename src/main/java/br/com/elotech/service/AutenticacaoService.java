package br.com.elotech.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import br.com.elotech.config.security.JwtService;
import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.dto.LoginRequest;
import br.com.elotech.dto.LoginResponse;

@Service
public class AutenticacaoService {

    private static final String TIPO_TOKEN = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AutenticacaoService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse autenticar(LoginRequest request) {
        var resultado = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );
        var usuario = (UsuarioAutenticado) resultado.getPrincipal();
        return new LoginResponse(
            jwtService.gerarToken(usuario),
            TIPO_TOKEN,
            jwtService.expiracao() / 1000
        );
    }
}