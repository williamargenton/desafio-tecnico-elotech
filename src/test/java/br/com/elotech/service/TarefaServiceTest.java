package br.com.elotech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import br.com.elotech.config.exception.ElotechException;
import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.dto.TarefaRequest;
import br.com.elotech.entity.Projeto;
import br.com.elotech.entity.Tarefa;
import br.com.elotech.entity.Usuario;
import br.com.elotech.entity.enums.Perfil;
import br.com.elotech.entity.enums.PrioridadeTarefa;
import br.com.elotech.entity.enums.StatusTarefa;
import br.com.elotech.repository.TarefaRepository;
import br.com.elotech.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AcessoProjetoService acessoProjetoService;

    @Mock
    private FabricaConsultaTarefa fabricaConsultaTarefa;

    private TarefaService tarefaService;

    private Usuario administrador;
    private Usuario membro;
    private Projeto projeto;
    private UsuarioAutenticado membroAutenticado;

    @BeforeEach
    void prepararCenario() throws Exception {
        ValidadorTarefa validadorTarefa = new ValidadorTarefa(tarefaRepository);
        tarefaService = new TarefaService(
            tarefaRepository,
            usuarioRepository,
            acessoProjetoService,
            validadorTarefa,
            fabricaConsultaTarefa
        );

        administrador = new Usuario("Administrador", "admin@test.com", "senha", Perfil.ADMIN);
        membro = new Usuario("Membro", "membro@test.com", "senha", Perfil.MEMBER);
        definirId(administrador, 1L);
        definirId(membro, 2L);

        projeto = new Projeto("Projeto", "Descrição", administrador);
        projeto.adicionarMembro(membro);
        definirId(projeto, 10L);

        membroAutenticado = UsuarioAutenticado.de(membro);
        when(acessoProjetoService.buscarProjetoAcessivel(10L, membroAutenticado)).thenReturn(projeto);
        lenient().when(usuarioRepository.findById(2L)).thenReturn(Optional.of(membro));
    }

    @Test
    void deveRejeitarResponsavelForaDoProjeto() throws Exception {
        Usuario externo = new Usuario("Externo", "externo@test.com", "senha", Perfil.MEMBER);
        definirId(externo, 3L);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(externo));

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> tarefaService.criar(
                10L,
                request(StatusTarefa.TODO, PrioridadeTarefa.LOW, 3L),
                membroAutenticado
            )
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getStatus());
    }

    @Test
    void deveRejeitarTransicaoDeConcluidaParaPendente() throws Exception {
        Tarefa tarefa = tarefa(StatusTarefa.DONE, PrioridadeTarefa.LOW);
        when(tarefaRepository.findByIdAndProjetoId(20L, 10L)).thenReturn(Optional.of(tarefa));

        assertThrows(
            ElotechException.class,
            () -> tarefaService.atualizar(
                10L,
                20L,
                request(StatusTarefa.TODO, PrioridadeTarefa.LOW, 2L),
                membroAutenticado
            )
        );
    }

    @Test
    void deveRejeitarTransicaoDiretaDePendenteParaConcluida() throws Exception {
        Tarefa tarefa = tarefa(StatusTarefa.TODO, PrioridadeTarefa.LOW);
        when(tarefaRepository.findByIdAndProjetoId(20L, 10L)).thenReturn(Optional.of(tarefa));

        assertThrows(
            ElotechException.class,
            () -> tarefaService.atualizar(
                10L,
                20L,
                request(StatusTarefa.DONE, PrioridadeTarefa.LOW, 2L),
                membroAutenticado
            )
        );
    }

    @Test
    void somenteAdministradorDoProjetoDeveConcluirTarefaCritica() throws Exception {
        Tarefa tarefa = tarefa(StatusTarefa.IN_PROGRESS, PrioridadeTarefa.CRITICAL);
        when(tarefaRepository.findByIdAndProjetoId(20L, 10L)).thenReturn(Optional.of(tarefa));

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> tarefaService.atualizar(
                10L,
                20L,
                request(StatusTarefa.DONE, PrioridadeTarefa.CRITICAL, 2L),
                membroAutenticado
            )
        );

        assertEquals(HttpStatus.FORBIDDEN, excecao.getStatus());
    }

    @Test
    void naoDeveContornarRegraCriticaAlterandoPrioridadeAoConcluir() throws Exception {
        Tarefa tarefa = tarefa(StatusTarefa.IN_PROGRESS, PrioridadeTarefa.CRITICAL);
        when(tarefaRepository.findByIdAndProjetoId(20L, 10L)).thenReturn(Optional.of(tarefa));

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> tarefaService.atualizar(
                10L,
                20L,
                request(StatusTarefa.DONE, PrioridadeTarefa.HIGH, 2L),
                membroAutenticado
            )
        );

        assertEquals(HttpStatus.FORBIDDEN, excecao.getStatus());
    }

    @Test
    void deveAplicarLimiteWip() {
        when(tarefaRepository.countByResponsavelIdAndStatus(2L, StatusTarefa.IN_PROGRESS))
            .thenReturn(5L);

        ElotechException excecao = assertThrows(
            ElotechException.class,
            () -> tarefaService.criar(
                10L,
                request(StatusTarefa.IN_PROGRESS, PrioridadeTarefa.HIGH, 2L),
                membroAutenticado
            )
        );

        assertTrue(excecao.getMessage().contains("WIP"));
    }

    private Tarefa tarefa(StatusTarefa status, PrioridadeTarefa prioridade) throws Exception {
        Tarefa tarefa = new Tarefa(
            "Tarefa",
            "Descrição",
            status,
            prioridade,
            LocalDate.now().plusDays(1),
            projeto,
            membro
        );
        definirId(tarefa, 20L);
        return tarefa;
    }

    private TarefaRequest request(StatusTarefa status, PrioridadeTarefa prioridade, Long responsavelId) {
        return new TarefaRequest(
            "Título",
            "Descrição",
            status,
            prioridade,
            LocalDate.now().plusDays(1),
            responsavelId
        );
    }

    private static void definirId(Object alvo, Long id) throws Exception {
        Field campo = alvo.getClass().getDeclaredField("id");
        campo.setAccessible(true);
        campo.set(alvo, id);
    }
}
