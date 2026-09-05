package br.com.elotech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import br.com.elotech.dto.ProjetoRequest;
import br.com.elotech.dto.ProjetoResponse;
import br.com.elotech.entity.Projeto;
import br.com.elotech.entity.Usuario;
import br.com.elotech.entity.enums.Perfil;
import br.com.elotech.repository.ProjetoRepository;
import br.com.elotech.repository.TarefaRepository;
import br.com.elotech.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private AcessoProjetoService acessoProjetoService;

    private ProjetoService projetoService;
    private Usuario administrador;
    private Usuario membro;
    private Projeto projeto;
    private UsuarioAutenticado administradorAutenticado;

    @BeforeEach
    void prepararCenario() throws Exception {
        projetoService = new ProjetoService(
            projetoRepository,
            usuarioRepository,
            tarefaRepository,
            acessoProjetoService
        );

        administrador = usuario(1L, "Administrador", "admin@test.com", Perfil.ADMIN);
        membro = usuario(2L, "Membro", "membro@test.com", Perfil.MEMBER);
        projeto = new Projeto("Projeto", "Descrição", administrador);
        definirId(projeto, 10L);
        administradorAutenticado = UsuarioAutenticado.de(administrador);
    }

    @Test
    void deveCriarProjetoParaAdministrador() {
        ProjetoRequest request = new ProjetoRequest("Novo projeto", "Descrição");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(administrador));
        when(projetoRepository.save(any(Projeto.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        ProjetoResponse resposta = projetoService.criar(request, administradorAutenticado);

        assertEquals("Novo projeto", resposta.nome());
        assertEquals(1L, resposta.dono().id());
        assertEquals(1, resposta.membros().size());
    }

    @Test
    void deveRecusarCriacaoDeProjetoPorMembro() {
        UsuarioAutenticado membroAutenticado = UsuarioAutenticado.de(membro);

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> projetoService.criar(
                new ProjetoRequest("Projeto", "Descrição"),
                membroAutenticado
            )
        );

        assertEquals(HttpStatus.FORBIDDEN, excecao.getStatus());
        verifyNoInteractions(projetoRepository, usuarioRepository, tarefaRepository);
    }

    @Test
    void deveRetornarConflitoAoAdicionarMembroDuplicado() {
        projeto.adicionarMembro(membro);
        when(acessoProjetoService.buscarProjetoAdministrado(10L, administradorAutenticado))
            .thenReturn(projeto);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(membro));

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> projetoService.adicionarMembro(10L, 2L, administradorAutenticado)
        );

        assertEquals(HttpStatus.CONFLICT, excecao.getStatus());
    }

    @Test
    void deveRetornarConflitoAoTentarRemoverODono() {
        when(acessoProjetoService.buscarProjetoAdministrado(10L, administradorAutenticado))
            .thenReturn(projeto);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(administrador));

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> projetoService.removerMembro(10L, 1L, administradorAutenticado)
        );

        assertEquals(HttpStatus.CONFLICT, excecao.getStatus());
        verify(tarefaRepository, never()).existsByProjetoIdAndResponsavelId(any(), any());
    }

    @Test
    void deveRetornarConflitoAoRemoverMembroResponsavelPorTarefas() {
        projeto.adicionarMembro(membro);
        when(acessoProjetoService.buscarProjetoAdministrado(10L, administradorAutenticado))
            .thenReturn(projeto);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(membro));
        when(tarefaRepository.existsByProjetoIdAndResponsavelId(10L, 2L)).thenReturn(true);

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> projetoService.removerMembro(10L, 2L, administradorAutenticado)
        );

        assertEquals(HttpStatus.CONFLICT, excecao.getStatus());
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
