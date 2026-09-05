package br.com.elotech.config.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.elotech.entity.Usuario;
import br.com.elotech.entity.enums.Perfil;

public record UsuarioAutenticado(
    Long id,
    String nomeDeUsuario,
    String senha,
    Perfil perfil
) implements UserDetails {

    public static UsuarioAutenticado de(Usuario usuario) {
        return new UsuarioAutenticado(
            usuario.getId(),
            usuario.getEmail(),
            usuario.getSenha(),
            usuario.getPerfil()
        );
    }

    @Override
    public String getUsername() {
        return nomeDeUsuario;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }
}