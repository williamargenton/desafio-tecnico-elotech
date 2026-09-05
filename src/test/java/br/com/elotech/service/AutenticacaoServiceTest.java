package br.com.elotech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import br.com.elotech.config.security.JwtService;
import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.dto.LoginRequest;
import br.com.elotech.dto.LoginResponse;
import br.com.elotech.entity.enums.Perfil;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AutenticacaoService autenticacaoService;

    @BeforeEach
    void prepararCenario() {
        autenticacaoService = new AutenticacaoService(authenticationManager, jwtService);
    }

    @Test
    void deveAutenticarEGerarToken() {
        LoginRequest request = new LoginRequest("admin@elotech.com", "admin123");
        UsuarioAutenticado usuario = new UsuarioAutenticado(
            1L,
            "admin@elotech.com",
            "senha-codificada",
            Perfil.ADMIN
        );
        Authentication autenticacao = mock(Authentication.class);

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(autenticacao);
        when(autenticacao.getPrincipal()).thenReturn(usuario);
        when(jwtService.gerarToken(usuario)).thenReturn("token-jwt");
        when(jwtService.expiracao()).thenReturn(3_600_000L);

        LoginResponse resposta = autenticacaoService.autenticar(request);

        assertEquals("token-jwt", resposta.token());
        assertEquals("Bearer", resposta.tipo());
        assertEquals(3_600L, resposta.expiraEmSegundos());
    }

    @Test
    void devePropagarErroQuandoAsCredenciaisForemInvalidas() {
        LoginRequest request = new LoginRequest("admin@elotech.com", "senha-incorreta");
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        assertThrows(
            BadCredentialsException.class,
            () -> autenticacaoService.autenticar(request)
        );
        verifyNoInteractions(jwtService);
    }
}
