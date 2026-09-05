package br.com.elotech.config.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest requisicao,
        HttpServletResponse resposta,
        FilterChain cadeiaDeFiltros
    ) throws ServletException, IOException {
        String autorizacao = requisicao.getHeader("Authorization");
        boolean possuiBearer = autorizacao != null && autorizacao.startsWith("Bearer ");
        boolean naoAutenticado = SecurityContextHolder.getContext().getAuthentication() == null;

        if (possuiBearer && naoAutenticado) {
            String token = autorizacao.substring(7);
            try {
                var usuario = userDetailsService.loadUserByUsername(jwtService.extrairEmail(token));
                if (jwtService.valido(token, usuario)) {
                    var autenticacao = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.getAuthorities()
                    );
                    autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(requisicao));
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                }
            } catch (Exception excecao) {
                SecurityContextHolder.clearContext();
                LOGGER.debug("Falha ao autenticar JWT: {}", excecao.getMessage());
            }
        }

        cadeiaDeFiltros.doFilter(requisicao, resposta);
    }
}