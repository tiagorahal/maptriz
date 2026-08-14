# Parte 1 — Revisão de código

Revisão do backend (Spring Boot) e do frontend (Angular) na base recebida. Cada item foi
confirmado **no código e no sistema rodando** (backend na 8080, Postgres local), com arquivo:linha
e evidência. A ordem de correção está no fim; nem tudo foi corrigido — o critério foi atacar
primeiro o que é grave e o que destrava a Parte 2, e justificar o resto.

## Resumo por gravidade

| # | Problema | Gravidade | Por quê é problema | Ação |
|---|----------|-----------|--------------------|------|
| 1 | **SQL Injection** nas escritas e leituras | 🔴 Crítico | `ImovelService` monta SQL nativo concatenando valores crus (`criar` l.55-65, `atualizar` l.83-95, `buscarPorId` l.41, `excluir` l.110). Confirmado: um `'` no nome já quebra o comando (ver evidência). Um `'); drop table imovel; --` executaria no banco. | Reescrever com Spring Data JPA (queries parametrizadas). |
| 2 | **A tela mente** (sucesso fantasma + falha silenciosa) | 🔴 Crítico | `DELETE` de id inexistente devolve `200 {"status":"ok"}` sem excluir nada → o front (`imoveis.ts:84`) mostra "Imóvel excluído!". Em erro de escrita, o back devolve `500`, e o front — sem callback de erro (`imoveis.ts:61`) — não mostra nada: o usuário age e não recebe confirmação nem erro. | Semântica de status correta + front tratando o ramo de erro. |
| 3 | **Seed reinserido a cada boot** | 🔴 Crítico | `data.sql` é INSERT sem guarda + `spring.sql.init.mode=always` (properties l.13) + `ddl-auto=update`; sem chave única para barrar. **Provado: 12 → 24 num boot**, "Maria Aparecida Souza" duplicada. A lista cresce sozinha. | Seed idempotente / `mode=never` / migração versionada. |
| 4 | **Stack trace vazada ao cliente** | 🟠 Alto | O corpo do `500` traz `"trace":"org.springframework..."` com classes internas e caminho do código — divulgação de informação útil a um atacante. | `@RestControllerAdvice` devolvendo erro enxuto (400/404/409/500 sem trace). |
| 5 | **`catch` que se contradiz com `@Transactional`** | 🟠 Alto | `criar`/`atualizar` capturam a exceção e retornam `"ok"` (l.70-72), mas a classe é `@Transactional` (l.15): a transação já foi marcada rollback-only, então o Spring lança `UnexpectedRollbackException` no commit e o "ok" nunca chega. Código que tenta esconder o erro e falha nisso. | Remover o catch cego; deixar o erro subir para o handler global. |
| 6 | **Sem DTO / tudo `Object` / sem validação** | 🟠 Alto | Controller recebe `@RequestBody Object` (l.34,39); entidade com campos `public` (`Imovel.java:19`). Over-posting total e **nenhuma validação**: confirmado que dá pra inserir imóvel com `latitude/longitude = null` (200 ok). | DTOs de request/response + Bean Validation (`@NotNull`, faixas de lat/long). |
| 7 | **`EntityManager` + SQL nativo no lugar do JPA** | 🟠 Alto | Há Hibernate e a entidade `Imovel`, mas o service ignora tudo e escreve SQL na mão retornando `HashMap`. A entidade só gera schema. É a raiz de 1, 5 e 6. | Repository Spring Data + entidade como modelo real. |
| 8 | **`GET /api/imoveis` sem paginação** | 🟠 Alto | `listar` traz todas as linhas (`Service` l.25). Quebra no cenário de grande volume (tarefa 6). | `Pageable`/`Page` na tarefa 6. |
| 9 | **Front sem service + bug de aliasing** | 🟠 Alto | HttpClient e URLs `localhost:8080` hardcoded no componente; `editar(i)` faz `this.form = i` (`imoveis.ts:75`) — mesma referência do objeto da lista, então digitar no form altera a linha da tabela ao vivo, antes de salvar. | Extrair `ImovelService` com estado; copiar o objeto ao editar. |
| 10 | **Credenciais commitadas** | 🟡 Médio | `postgres/postgres` em `application.properties` (l.5-6) versionado. | Externalizar (env var / profile local fora do VCS). |
| 11 | **CORS liberado (`*`)** | 🟡 Médio | `@CrossOrigin(origins = "*")` (`ImovelController.java:16`). | Restringir à origem do front. |
| 12 | **`open-in-view=true`** | 🟡 Médio | Anti-padrão OSIV ligado (properties l.11). | Desligar. |
| 13 | **`System.out.println` de debug** | 🟢 Baixo | `println` de SQL/loop (`Controller` l.24, `Service` l.27,67,97) mesmo com logger configurado. | Usar o logger / remover. |
| 14 | **Padrões Angular antigos** | 🟢 Baixo | `any` em tudo, `detectChanges()` manual, `*ngFor` sem `trackBy` (`imoveis.html:56`), template-driven forms. | Modernizar ao longo da Parte 2. |

## O achado central: por que a tela mente

O enunciado avisa que "o que acontece na tela nem sempre é o que aconteceu no banco". Verificando
ao vivo (backend na 8080, conferindo no `psql`), são vários mecanismos somados:

| Ação testada | HTTP | Corpo | O que aconteceu no banco |
|---|---|---|---|
| `POST` proprietário `O'Brien` (apóstrofo, dado real) | **500** | stack trace vazada (`"trace":"org.springframework..."`) | **nada inserido** (12 → 12) — o front não trata erro e não dá feedback |
| `POST` com `latitude`/`longitude` nulos | **200** | `{"status":"ok"}` | inseriu com coordenadas **NULL** (sem validação) |
| `DELETE` de id inexistente `99999` | **200** | `{"status":"ok"}` | **nada excluído** — front mostra "Imóvel excluído!" (sucesso fantasma) |
| Rodar `data.sql` (= 1 boot com `mode=always`) | — | `INSERT 0 12` | **12 → 24**, "Maria Aparecida Souza" duplicada |

Resumo da causa: o backend ora engole o erro e devolve `200` para operações que não aconteceram
(delete), ora estoura `500` com trace, enquanto o frontend nunca olha a resposta de erro — então
a mensagem de sucesso na tela é independente do que o banco fez. Some a isso o seed que cresce a
cada reinício e o formulário de edição que altera a linha antes de salvar (`this.form = i`).

## Ordem de correção (e o que fica de fora, de propósito)

1. **Reescrever a camada de dados** (Service → Spring Data JPA + Repository + DTO + Bean
   Validation). Um golpe resolve #1 (SQLi), #6 (over-posting/validação) e #7 (arquitetura), e
   remove o `catch` contraditório (#5).
2. **Tratamento de erro coerente** (`@RestControllerAdvice`: 400/404/409, sem trace) + front
   tratando o ramo de erro. Fecha #2 e #4 ponta a ponta.
3. **Seed idempotente** (#3).
4. **Config barata de segurança:** credencial fora do VCS (#10), CORS restrito (#11).

Ficam **de fora nesta etapa, por escolha**: paginação (#8, é a tarefa 6 da Parte 2, feita lá),
OSIV (#12), limpeza de `println` (#13) e a modernização estética do Angular (#14) — baixo risco,
alto ruído; melhor gastar o tempo no que é crítico e no que a Parte 2 exige.

## Resultado do refactor (Parte 1) — antes × depois

A camada de dados foi reescrita: `EntityManager` + SQL concatenado → **Spring Data JPA** com
`ImovelRepository`, entidade encapsulada, **DTOs** (`ImovelRequest`/`ImovelResponse`) com **Bean
Validation**, e um **`@RestControllerAdvice`** para erros enxutos. Verificado no backend rodando:

| Caso | Antes | Depois |
|---|---|---|
| POST nome com apóstrofo `O'Brien` | 500 + stack trace vazada (SQLi) | **201 Created**, gravado corretamente |
| POST latitude/longitude nulos | 200, inseria com NULL | **400** `{campos:{latitude,longitude}}`, não insere |
| DELETE id inexistente `99999` | 200 `{status:ok}` (fantasma) | **404** |
| DELETE id existente | 200 | **204 No Content** |
| Corpo de erro | trazia `"trace"` com a pilha | enxuto, sem trace |
| Reboot do backend | seed reinseria (12 → 24) | **idempotente** (segue 12) |

O que a reescrita eliminou: #1 (SQLi), #3 (seed), #4 (trace vazada), #5 (catch contraditório),
#6 (validação/over-posting), #7 (arquitetura) e a maior parte do #2 (semântica de status correta).
Além disso: credenciais externalizadas via env (#10), CORS restrito à origem do front (#11) e OSIV
desligado (#12).

Fica para a **Parte 2 (tarefa 1)**, de propósito, o tratamento de erro no Angular e o bug de
aliasing (#9) — porque o componente é reestruturado lá, e não faz sentido reescrevê-lo duas vezes.
