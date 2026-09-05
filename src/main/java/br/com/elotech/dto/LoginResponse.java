package br.com.elotech.dto;

public record LoginResponse(
    String token,
    String tipo,
    Long expiraEmSegundos
) {

}