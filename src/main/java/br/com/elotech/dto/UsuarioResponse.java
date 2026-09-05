package br.com.elotech.dto;

import br.com.elotech.entity.Usuario;
import br.com.elotech.entity.enums.Perfil;

public record UsuarioResponse(
    Long id,
    String nome,
    String email,
    Perfil perfil
) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getPerfil()
        );
    }
}