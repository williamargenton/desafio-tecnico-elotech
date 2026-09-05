package br.com.elotech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import br.com.elotech.config.exception.ElotechException;
import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.entity.Projeto;
import br.com.elotech.entity.Usuario;
import br.com.elotech.entity.enums.Perfil;
import br.com.elotech.repository.ProjetoRepository;

@ExtendWith(MockitoExtension.class)
class AcessoProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    private AcessoProjetoService acessoProjetoService;
    private Projeto projeto;
    private UsuarioAutenticado administradorDono;

    @BeforeEach
    void prepararCenario() throws Exception {
        acessoProjetoService = new AcessoProjetoService(projetoRepository);

        Usuario dono = usuario(1L, "Administrador", "admin@test.com", Perfil.ADMIN);
        projeto = new Projeto("Projeto", "Descrição", dono);
        definirId(projeto, 10L);
        administradorDono = UsuarioAutenticado.de(dono);
    }

    @Test
    void deveRetornarProjetoAcessivel() {
        when(projetoRepository.buscarAcessivelPorId(10L, 1L)).thenReturn(Optional.of(projeto));

        Projeto resultado = acessoProjetoService.buscarProjetoAcessivel(10L, administradorDono);

        assertSame(projeto, resultado);
    }

    @Test
    void deveOcultarProjetoNaoAcessivelComoNaoEncontrado() {
        when(projetoRepository.buscarAcessivelPorId(10L, 1L)).thenReturn(Optional.empty());

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> acessoProjetoService.buscarProjetoAcessivel(10L, administradorDono)
        );

        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatus());
    }

    @Test
    void devePermitirAdministracaoPeloAdministradorDono() {
        when(projetoRepository.buscarAcessivelPorId(10L, 1L)).thenReturn(Optional.of(projeto));

        Projeto resultado = acessoProjetoService.buscarProjetoAdministrado(10L, administradorDono);

        assertSame(projeto, resultado);
    }

    @Test
    void deveRecusarAdministracaoPorMembro() throws Exception {
        Usuario membro = usuario(2L, "Membro", "membro@test.com", Perfil.MEMBER);
        projeto.adicionarMembro(membro);
        UsuarioAutenticado membroAutenticado = UsuarioAutenticado.de(membro);
        when(projetoRepository.buscarAcessivelPorId(10L, 2L)).thenReturn(Optional.of(projeto));

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> acessoProjetoService.buscarProjetoAdministrado(10L, membroAutenticado)
        );

        assertEquals(HttpStatus.FORBIDDEN, excecao.getStatus());
    }

    @Test
    void deveRecusarAdministracaoPorAdministradorQueNaoEODono() throws Exception {
        Usuario outroAdministrador = usuario(2L, "Outro ADMIN", "admin2@test.com", Perfil.ADMIN);
        projeto.adicionarMembro(outroAdministrador);
        UsuarioAutenticado outroAdministradorAutenticado = UsuarioAutenticado.de(outroAdministrador);
        when(projetoRepository.buscarAcessivelPorId(10L, 2L)).thenReturn(Optional.of(projeto));

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> acessoProjetoService.buscarProjetoAdministrado(10L, outroAdministradorAutenticado)
        );

        assertEquals(HttpStatus.FORBIDDEN, excecao.getStatus());
    }

    private static Usuario usuario(
        Long id,
        String nome,
        String email,
        Perfil perfil
    ) throws Exception {
        Usuario usuario = new Usuario(nome, email, "senha", perfil);
        definirId(usuario, id);
        return usuario;
    }

    private static void definirId(Object alvo, Long id) throws Exception {
        Field campo = alvo.getClass().getDeclaredField("id");
        campo.setAccessible(true);
        campo.set(alvo, id);
    }
}
