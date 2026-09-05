package br.com.elotech.config.security;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.elotech.config.exception.TitulosHttp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TratadorErroSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public TratadorErroSeguranca(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
        HttpServletRequest requisicao,
        HttpServletResponse resposta,
        AuthenticationException excecao
    ) throws IOException {
        escreverProblema(requisicao, resposta, HttpStatus.UNAUTHORIZED, "Autenticação obrigatória");
    }

    @Override
    public void handle(
        HttpServletRequest requisicao,
        HttpServletResponse resposta,
        org.springframework.security.access.AccessDeniedException excecao
    ) throws IOException {
        escreverProblema(requisicao, resposta, HttpStatus.FORBIDDEN, "Acesso negado");
    }

    private void escreverProblema(
        HttpServletRequest requisicao,
        HttpServletResponse resposta,
        HttpStatus status,
        String detalhe
    ) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(TitulosHttp.obter(status));
        problema.setType(URI.create("about:blank"));
        problema.setInstance(URI.create(requisicao.getRequestURI()));

        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(resposta.getOutputStream(), problema);
    }
}