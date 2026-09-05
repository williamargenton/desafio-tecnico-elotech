# API Gerenciador de Tarefas

API REST para gerenciamento de projetos e tarefas, desenvolvida com Java 17,
Spring Boot 3, Spring Security, JWT, Spring Data JPA, H2, Bean Validation,
Problem Details e OpenAPI.

## Pré-requisitos

- JDK 17 ou superior
- Maven 3.9 ou superior

## Execução

Execute os testes e inicie a aplicação:

```bash
mvn clean test
mvn spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Console H2: `http://localhost:8080/h2-console`

Configuração do H2:

```text
JDBC URL: jdbc:h2:file:./data/taskmanager
User Name: sa
Password: deixe vazio
```

## Usuários de demonstração

Os usuários abaixo são criados automaticamente quando ainda não existem:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| ADMIN | `admin@elotech.com` | `admin123` |
| ADMIN | `admin1@elotech.com` | `admin1231` |
| MEMBER | `member@elotech.com` | `member123` |
| MEMBER | `member1@elotech.com` | `member1231` |

Os usuários de demonstração podem ser desabilitados com:

```yaml
app:
  dados-demonstracao:
    habilitados: false
```

## Autenticação

```http
POST /autenticacao/login
Content-Type: application/json
```

```json
{
  "email": "admin@elotech.com",
  "senha": "admin123"
}
```

Envie o token nas rotas protegidas:

```http
Authorization: Bearer <token>
```

Somente autenticação, Swagger, OpenAPI e console H2 são públicos. Todas as
demais rotas exigem um JWT válido.

## Endpoints

### Autenticação

- `POST /autenticacao/login`

### Projetos e membros

- `GET /projetos`
- `POST /projetos`
- `GET /projetos/{projetoId}`
- `PUT /projetos/{projetoId}`
- `DELETE /projetos/{projetoId}`
- `POST /projetos/{projetoId}/membros`
- `DELETE /projetos/{projetoId}/membros/{usuarioId}`

### Tarefas

- `GET /projetos/{projetoId}/tarefas`
- `POST /projetos/{projetoId}/tarefas`
- `GET /projetos/{projetoId}/tarefas/{tarefaId}`
- `PUT /projetos/{projetoId}/tarefas/{tarefaId}`
- `DELETE /projetos/{projetoId}/tarefas/{tarefaId}`
- `GET /projetos/{projetoId}/tarefas/busca?texto=...`

### Relatório

- `GET /projetos/{projetoId}/relatorio`

## Filtros e ordenação

A listagem de tarefas aceita os seguintes parâmetros opcionais:

| Parâmetro | Descrição |
| --- | --- |
| `status` | `TODO`, `IN_PROGRESS` ou `DONE` |
| `prioridade` | `LOW`, `MEDIUM`, `HIGH` ou `CRITICAL` |
| `responsavel` | Identificador do usuário responsável |
| `dataInicial` | Data inicial de criação no formato `yyyy-MM-dd` |
| `dataFinal` | Data final de criação no formato `yyyy-MM-dd` |
| `ordenarPor` | `prioridade`, `criadaEm` ou `prazo` |

Os filtros podem ser combinados:

```http
GET /projetos/1/tarefas?status=IN_PROGRESS&prioridade=HIGH&responsavel=2&dataInicial=2026-01-01&dataFinal=2026-12-31&ordenarPor=prioridade
```

A ordenação por prioridade segue a ordem:

```text
CRITICAL -> HIGH -> MEDIUM -> LOW
```

## Busca textual

A busca verifica simultaneamente o título e a descrição:

```http
GET /projetos/1/tarefas/busca?texto=autenticacao
```

A pesquisa é executada diretamente no banco, evitando o carregamento de todas
as tarefas para filtragem em memória. A implementação utiliza `LIKE` por ser
suficiente para o escopo e para o H2. Em uma base com grande volume, a opção
seria PostgreSQL Full Text Search, índice trigram ou um mecanismo de busca
especializado.

## Regras de negócio

- Um usuário somente acessa tarefas de projetos dos quais é membro.
- Somente usuários com perfil `ADMIN` podem criar projetos.
- Neste projeto, apenas o `ADMIN` que é dono pode atualizar ou excluir o
  projeto e administrar seus membros.
- O dono é incluído automaticamente na lista de membros.
- Usuários `MEMBER` podem administrar tarefas dos projetos dos quais participam.
- O responsável pela tarefa deve ser membro do projeto.
- O fluxo de status adotado é:
  - `TODO` pode permanecer em `TODO` ou avançar para `IN_PROGRESS`;
  - `IN_PROGRESS` pode permanecer no mesmo status, voltar para `TODO` ou avançar
    para `DONE`;
  - `DONE` pode permanecer em `DONE` ou voltar para `IN_PROGRESS`;
  - `TODO -> DONE` e `DONE -> TODO` não são permitidos diretamente.
- Uma tarefa `CRITICAL` somente pode ser concluída pelo `ADMIN` dono do projeto.
- Cada responsável pode possuir no máximo cinco tarefas simultâneas em
  `IN_PROGRESS`.
- O dono não pode ser removido do projeto.
- Um membro responsável por tarefas precisa ter essas tarefas reatribuídas antes
  de ser removido.

As violações relacionadas ao estado atual dos recursos, como membro duplicado,
remoção do dono e remoção de membro com tarefas, retornam `409 Conflict`.

## Arquitetura

O projeto utiliza uma arquitetura em camadas:

```text
controller -> service -> repository -> banco de dados
```

Responsabilidades principais:

- `controller`: contrato HTTP, validação dos DTOs e códigos de resposta;
- `service`: casos de uso e controle transacional;
- `repository`: persistência e consultas;
- `entity`: modelo persistente e comportamentos do domínio;
- `dto`: contratos de entrada e saída;
- `config.security`: autenticação e validação do JWT;
- `config.exception`: respostas de erro no formato Problem Details.

As responsabilidades de tarefas foram separadas em:

- `TarefaService`: orquestra os casos de uso;
- `ValidadorTarefa`: aplica as regras de responsável, transição, tarefa crítica
  e limite WIP;
- `FabricaConsultaTarefa`: cria filtros e ordenações;
- `TarefaRepository`: executa persistência, busca e agregações.

O usuário autenticado é recebido com `@AuthenticationPrincipal`. Essa é a
integração padrão com o `SecurityContext` do Spring Security e torna a
dependência da autenticação explícita nos casos de uso.

## Tratamento de erros

As respostas de erro seguem o formato Problem Details (`application/problem+json`).
São tratados, entre outros:

- erros de validação;
- JSON inválido;
- parâmetros incompatíveis;
- método HTTP não suportado;
- autenticação ausente ou inválida;
- acesso negado;
- recursos inexistentes;
- conflitos de regras de negócio.

## Testes

A suíte contém testes unitários com JUnit 5 e Mockito para:

- `AutenticacaoService`;
- `AcessoProjetoService`;
- `ProjetoService`;
- `TarefaService`;
- `RelatorioService`.

Os testes de integração utilizam `@SpringBootTest`, MockMvc e banco H2 em
memória. Os fluxos cobertos incluem:

- autenticação e proteção das rotas;
- criação de projeto e autorização por perfil;
- ordenação semântica por prioridade;
- busca textual por título e descrição;
- conclusão de tarefa `CRITICAL`;
- limite de cinco tarefas `IN_PROGRESS`.

Execute a suíte completa com:

```bash
mvn clean test
```

## Decisões e trade-offs

- O H2 persistente simplifica a execução local. Em produção, seria substituído
  por PostgreSQL com migrações versionadas, por exemplo com Flyway.
- A associação entre projeto e membros não possui atributos próprios; por isso,
  foi modelada diretamente com `@ManyToMany`. Uma entidade associativa seria
  criada se a participação precisasse armazenar função, data de entrada ou
  permissões específicas.
- Os relacionamentos são `LAZY` para evitar carregamentos desnecessários. As
  consultas que precisam dos membros fazem o carregamento explicitamente.
- As atualizações de entidades gerenciadas utilizam o dirty checking do JPA
  dentro de transações, sem chamadas redundantes a `save`.
- O limite WIP é validado na camada de serviço. Em cenários de alta concorrência,
  seria necessário aplicar bloqueio ou nível de isolamento apropriado.
- A regra de administração foi deliberadamente restringida ao `ADMIN` dono do
  projeto. Para permissões diferentes por projeto, seria criada uma entidade de
  associação contendo o papel do usuário naquele projeto.

## Melhorias com mais tempo

- PostgreSQL e migrações com Flyway;
- paginação nas listagens e buscas;
- auditoria das alterações de tarefas;
- controle de concorrência para o limite WIP;
- busca textual especializada para grandes volumes;
- testes adicionais de contrato e concorrência.
