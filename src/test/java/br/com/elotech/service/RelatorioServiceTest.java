package br.com.elotech.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.elotech.config.security.UsuarioAutenticado;
import br.com.elotech.dto.RelatorioResponse;
import br.com.elotech.entity.enums.Perfil;
import br.com.elotech.entity.enums.PrioridadeTarefa;
import br.com.elotech.entity.enums.StatusTarefa;
import br.com.elotech.repository.TarefaRepository;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private AcessoProjetoService acessoProjetoService;

    private RelatorioService relatorioService;
    private UsuarioAutenticado usuarioAutenticado;

    @BeforeEach
    void prepararCenario() {
        relatorioService = new RelatorioService(tarefaRepository, acessoProjetoService);
        usuarioAutenticado = new UsuarioAutenticado(
            1L,
            "admin@test.com",
            "senha",
            Perfil.ADMIN
        );
    }

    @Test
    void deveGerarContadoresPreenchendoComZeroOsValoresAusentes() {
        when(tarefaRepository.contarPorStatus(10L)).thenReturn(List.of(
            new Object[] {StatusTarefa.TODO, 3L},
            new Object[] {StatusTarefa.DONE, 2L}
        ));
        when(tarefaRepository.contarPorPrioridade(10L)).thenReturn(List.of(
            new Object[] {PrioridadeTarefa.HIGH, 4L},
            new Object[] {PrioridadeTarefa.CRITICAL, 1L}
        ));

        RelatorioResponse resposta = relatorioService.gerar(10L, usuarioAutenticado);

        assertEquals(3L, resposta.porStatus().get(StatusTarefa.TODO));
        assertEquals(0L, resposta.porStatus().get(StatusTarefa.IN_PROGRESS));
        assertEquals(2L, resposta.porStatus().get(StatusTarefa.DONE));
        assertEquals(0L, resposta.porPrioridade().get(PrioridadeTarefa.LOW));
        assertEquals(0L, resposta.porPrioridade().get(PrioridadeTarefa.MEDIUM));
        assertEquals(4L, resposta.porPrioridade().get(PrioridadeTarefa.HIGH));
        assertEquals(1L, resposta.porPrioridade().get(PrioridadeTarefa.CRITICAL));
        verify(acessoProjetoService).buscarProjetoAcessivel(10L, usuarioAutenticado);
    }
}
