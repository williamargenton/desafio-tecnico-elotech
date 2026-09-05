package br.com.elotech.config.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public final class TitulosHttp {

    private static final Map<HttpStatus, String> TITULOS = Map.ofEntries(
        Map.entry(HttpStatus.BAD_REQUEST, "Requisição inválida"),
        Map.entry(HttpStatus.UNAUTHORIZED, "Não autorizado"),
        Map.entry(HttpStatus.FORBIDDEN, "Proibido"),
        Map.entry(HttpStatus.NOT_FOUND, "Não encontrado"),
        Map.entry(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido"),
        Map.entry(HttpStatus.CONFLICT, "Conflito"),
        Map.entry(HttpStatus.UNPROCESSABLE_ENTITY, "Entidade não processável"),
        Map.entry(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor")
    );

    private TitulosHttp() {
    }

    public static String obter(HttpStatus status) {
        return TITULOS.getOrDefault(
            status,
            "Erro HTTP " + status.value()
        );
    }
}