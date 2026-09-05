package br.com.elotech.config.exception;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class TratadorGlobalExcecoes {

    private static final Logger LOGGER = LoggerFactory.getLogger(TratadorGlobalExcecoes.class);

    @ExceptionHandler(ElotechException.class)
    public ProblemDetail tratarNegocio(
        ElotechException excecao,
        HttpServletRequest requisicao
    ) {
        return problema(excecao.getStatus(), excecao.getMessage(), requisicao);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarValidacao(
        MethodArgumentNotValidException excecao,
        HttpServletRequest requisicao
    ) {
        ProblemDetail problema = problema(
            HttpStatus.BAD_REQUEST,
            "Dados de entrada inválidos",
            requisicao
        );
        Map<String, String> erros = new LinkedHashMap<>();
        excecao.getBindingResult().getFieldErrors()
            .forEach(erro -> erros.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        problema.setProperty("erros", erros);
        return problema;
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class
    })
    public ProblemDetail tratarRequisicaoInvalida(
        Exception excecao,
        HttpServletRequest requisicao
    ) {
        return problema(
            HttpStatus.BAD_REQUEST,
            "Requisição malformada ou inválida",
            requisicao
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail tratarNaoEncontrado(
        NoResourceFoundException excecao,
        HttpServletRequest requisicao
    ) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", requisicao);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail tratarMetodoNaoPermitido(
        HttpRequestMethodNotSupportedException excecao,
        HttpServletRequest requisicao
    ) {
        return problema(
            HttpStatus.METHOD_NOT_ALLOWED,
            "Método HTTP não permitido para este endpoint",
            requisicao
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarConflito(
        DataIntegrityViolationException excecao,
        HttpServletRequest requisicao
    ) {
        return problema(
            HttpStatus.CONFLICT,
            "A operação conflita com dados existentes",
            requisicao
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail tratarAcessoNegado(
        AccessDeniedException excecao,
        HttpServletRequest requisicao
    ) {
        return problema(HttpStatus.FORBIDDEN, "Acesso negado", requisicao);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail tratarAutenticacao(
        AuthenticationException excecao,
        HttpServletRequest requisicao
    ) {
        return problema(
            HttpStatus.UNAUTHORIZED,
            "E-mail ou senha inválidos",
            requisicao
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarInesperado(
        Exception excecao,
        HttpServletRequest requisicao
    ) {
        LOGGER.error(
            "Erro inesperado ao processar {} {}",
            requisicao.getMethod(),
            requisicao.getRequestURI(),
            excecao
        );
        return problema(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno inesperado",
            requisicao
        );
    }

    private ProblemDetail problema(
        HttpStatus status,
        String detalhe,
        HttpServletRequest requisicao
    ) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(TitulosHttp.obter(status));
        problema.setType(URI.create("about:blank"));
        problema.setInstance(URI.create(requisicao.getRequestURI()));
        return problema;
    }
}