package br.com.elotech;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegracaoTest {

    private static final long MEMBRO_ID = 3L;
    private static final long SEGUNDO_MEMBRO_ID = 4L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveAutenticarECriarProjeto() throws Exception {
        String token = autenticar("admin@elotech.com", "admin123");

        String respostaProjeto = mockMvc.perform(post("/projetos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nome": "Projeto de integração",
                      "descricao": "Criado pelo teste de integração"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.nome").value("Projeto de integração"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode projeto = objectMapper.readTree(respostaProjeto);
        long projetoId = projeto.get("id").asLong();
        long donoId = projeto.get("dono").get("id").asLong();

        for (String prioridade : new String[] {"LOW", "CRITICAL", "MEDIUM", "HIGH"}) {
            mockMvc.perform(post("/projetos/{projetoId}/tarefas", projetoId)
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "titulo": "Tarefa %s",
                          "descricao": "Ordenação por prioridade",
                          "status": "TODO",
                          "prioridade": "%s",
                          "responsavelId": %d
                        }
                        """.formatted(prioridade, prioridade, donoId)))
                .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/projetos/{projetoId}/tarefas", projetoId)
                .queryParam("ordenarPor", "prioridade")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].prioridade").value("CRITICAL"))
            .andExpect(jsonPath("$[1].prioridade").value("HIGH"))
            .andExpect(jsonPath("$[2].prioridade").value("MEDIUM"))
            .andExpect(jsonPath("$[3].prioridade").value("LOW"));
    }

    @Test
    void endpointProtegidoDeveExigirAutenticacao() throws Exception {
        mockMvc.perform(get("/projetos"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void metodoNaoSuportadoNoLoginDeveRetornar405() throws Exception {
        mockMvc.perform(get("/autenticacao/login"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void membroNaoDeveCriarProjeto() throws Exception {
        String token = autenticar("member@elotech.com", "member123");

        mockMvc.perform(post("/projetos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nome": "Projeto não permitido",
                      "descricao": "Um MEMBER não pode criar este projeto"
                    }
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void membroNaoDeveConcluirTarefaCritica() throws Exception {
        String tokenAdministrador = autenticar("admin@elotech.com", "admin123");
        String tokenMembro = autenticar("member@elotech.com", "member123");
        JsonNode projeto = criarProjeto(tokenAdministrador, "Projeto tarefa crítica");
        long projetoId = projeto.get("id").asLong();

        adicionarMembro(projetoId, MEMBRO_ID, tokenAdministrador);

        String respostaTarefa = mockMvc.perform(post("/projetos/{projetoId}/tarefas", projetoId)
                .header("Authorization", "Bearer " + tokenMembro)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "titulo": "Corrigir incidente",
                      "descricao": "Falha crítica em produção",
                      "status": "IN_PROGRESS",
                      "prioridade": "CRITICAL",
                      "responsavelId": %d
                    }
                    """.formatted(MEMBRO_ID)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        long tarefaId = objectMapper.readTree(respostaTarefa).get("id").asLong();
        String tarefaConcluida = """
            {
              "titulo": "Corrigir incidente",
              "descricao": "Falha crítica em produção",
              "status": "DONE",
              "prioridade": "CRITICAL",
              "responsavelId": %d
            }
            """.formatted(MEMBRO_ID);

        mockMvc.perform(put("/projetos/{projetoId}/tarefas/{tarefaId}", projetoId, tarefaId)
                .header("Authorization", "Bearer " + tokenMembro)
                .contentType(MediaType.APPLICATION_JSON)
                .content(tarefaConcluida))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));

        mockMvc.perform(put("/projetos/{projetoId}/tarefas/{tarefaId}", projetoId, tarefaId)
                .header("Authorization", "Bearer " + tokenAdministrador)
                .contentType(MediaType.APPLICATION_JSON)
                .content(tarefaConcluida))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void deveAplicarLimiteWipEmRequisicoesReais() throws Exception {
        String tokenAdministrador = autenticar("admin@elotech.com", "admin123");
        String tokenMembro = autenticar("member1@elotech.com", "member1231");
        JsonNode projeto = criarProjeto(tokenAdministrador, "Projeto limite WIP");
        long projetoId = projeto.get("id").asLong();

        adicionarMembro(projetoId, SEGUNDO_MEMBRO_ID, tokenAdministrador);

        for (int numero = 1; numero <= 5; numero++) {
            mockMvc.perform(post("/projetos/{projetoId}/tarefas", projetoId)
                    .header("Authorization", "Bearer " + tokenMembro)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "titulo": "Tarefa WIP %d",
                          "descricao": "Teste do limite WIP",
                          "status": "IN_PROGRESS",
                          "prioridade": "LOW",
                          "responsavelId": %d
                        }
                        """.formatted(numero, SEGUNDO_MEMBRO_ID)))
                .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/projetos/{projetoId}/tarefas", projetoId)
                .header("Authorization", "Bearer " + tokenMembro)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "titulo": "Sexta tarefa WIP",
                      "descricao": "Deve ser recusada",
                      "status": "IN_PROGRESS",
                      "prioridade": "LOW",
                      "responsavelId": %d
                    }
                    """.formatted(SEGUNDO_MEMBRO_ID)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("WIP")));
    }

    @Test
    void deveBuscarTarefasPorTituloOuDescricao() throws Exception {
        String token = autenticar("admin1@elotech.com", "admin1231");
        JsonNode projeto = criarProjeto(token, "Projeto busca textual");
        long projetoId = projeto.get("id").asLong();
        long responsavelId = projeto.get("dono").get("id").asLong();

        criarTarefa(
            projetoId,
            responsavelId,
            "ZXQ-TITULO-UNICO",
            "Descrição comum",
            token
        );
        criarTarefa(
            projetoId,
            responsavelId,
            "Título comum",
            "ZXQ-DESCRICAO-UNICA",
            token
        );

        mockMvc.perform(get("/projetos/{projetoId}/tarefas/busca", projetoId)
                .queryParam("texto", "ZXQ-TITULO-UNICO")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].titulo").value("ZXQ-TITULO-UNICO"));

        mockMvc.perform(get("/projetos/{projetoId}/tarefas/busca", projetoId)
                .queryParam("texto", "ZXQ-DESCRICAO-UNICA")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].descricao").value("ZXQ-DESCRICAO-UNICA"));
    }

    private JsonNode criarProjeto(String token, String nome) throws Exception {
        String resposta = mockMvc.perform(post("/projetos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "nome": "%s",
                      "descricao": "Criado pelo teste de integração"
                    }
                    """.formatted(nome)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(resposta);
    }

    private void adicionarMembro(long projetoId, long usuarioId, String token) throws Exception {
        mockMvc.perform(post("/projetos/{projetoId}/membros", projetoId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "usuarioId": %d
                    }
                    """.formatted(usuarioId)))
            .andExpect(status().isOk());
    }

    private void criarTarefa(
        long projetoId,
        long responsavelId,
        String titulo,
        String descricao,
        String token
    ) throws Exception {
        mockMvc.perform(post("/projetos/{projetoId}/tarefas", projetoId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "titulo": "%s",
                      "descricao": "%s",
                      "status": "TODO",
                      "prioridade": "MEDIUM",
                      "responsavelId": %d
                    }
                    """.formatted(titulo, descricao, responsavelId)))
            .andExpect(status().isCreated());
    }

    private String autenticar(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/autenticacao/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "%s",
                      "senha": "%s"
                    }
                    """.formatted(email, senha)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tipo").value("Bearer"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(resposta).get("token").asText();
    }
}
