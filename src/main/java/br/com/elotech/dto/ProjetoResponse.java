package br.com.elotech.dto;

import java.util.Comparator;
import java.util.List;

import br.com.elotech.entity.Projeto;

public record ProjetoResponse(
    Long id,
    String nome,
    String descricao,
    UsuarioResponse dono,
    List<UsuarioResponse> membros
) {
    public static ProjetoResponse de(Projeto projeto) {
        List<UsuarioResponse> membros = projeto.getMembros().stream()
            .map(UsuarioResponse::de)
            .sorted(Comparator.comparing(UsuarioResponse::id))
            .toList();

        return new ProjetoResponse(
            projeto.getId(),
            projeto.getNome(),
            projeto.getDescricao(),
            UsuarioResponse.de(projeto.getDono()),
            membros
        );
    }
}