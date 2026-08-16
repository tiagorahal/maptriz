# Backend — WebGIS

API REST do cadastro de imóveis. Java 21, Spring Boot 3.5.16, Spring Data JPA, PostgreSQL com
migrações **Flyway**.

## Como rodar

Pré-requisitos: **JDK 21** e **PostgreSQL** em `localhost:5432`.

```bash
# cria a base 'webgis' (schema e seed são aplicados pelo Flyway na 1ª subida)
psql -U postgres -f ../scripts/setup-db.sql

./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Sobe em `http://localhost:8080`.

### Configuração

Credenciais e URL do banco saem por variável de ambiente (com defaults locais), em
`src/main/resources/application.properties`:

| Variável | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/webgis` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `app.cors.origin` | `http://localhost:4200` |

O schema é gerenciado pelo Flyway; o Hibernate roda em `ddl-auto=validate` (só confere que as
entidades batem com o schema, não altera nada).

## Arquitetura

`Controller` (HTTP/status) → `Service` (`@Transactional`, regra) → `Repository` (Spring Data).
Entrada e saída via **DTOs** (`record`), nunca a entidade crua:

- `ImovelRequest` — o que o cliente envia (com Bean Validation); `id` e auditoria nunca vêm do corpo.
- `ImovelResponse` / `ProprietarioResponse` / `PontoImovelResponse` / `PageResponse<T>` — respostas.
- `ApiExceptionHandler` (`@RestControllerAdvice`) — erros enxutos e com status correto (400/404/409),
  **sem vazar stack trace**.

## Migrações (Flyway)

`src/main/resources/db/migration/`:

| Versão | O que faz |
|---|---|
| `V1__schema_inicial` | cria a tabela `imovel` (proprietário ainda como texto) |
| `V2__seed_imoveis` | popula os 12 imóveis de exemplo (idempotente) |
| `V3__proprietario_como_entidade` | cria `proprietario`, faz o backfill dos nomes distintos, adiciona a FK e remove a coluna texto |

`baseline-on-migrate=true` faz a solução funcionar tanto num **banco novo** (roda V1→V3) quanto num
**banco que já existia** com dados (baseline + aplica só o que falta). Testado recriando a base do
zero: os 12 imóveis são preservados, viram 12 proprietários, nenhum imóvel fica órfão.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/imoveis` | Lista **paginada**. Params: `page`, `size`, `proprietario`, `municipio` |
| GET | `/api/imoveis/mapa` | Pontos (projeção leve) de todos os imóveis, para o mapa |
| GET | `/api/imoveis/{id}` | Busca por id |
| POST | `/api/imoveis` | Cadastra (valida; associa/cria o proprietário pelo nome) |
| PUT | `/api/imoveis/{id}` | Atualiza |
| DELETE | `/api/imoveis/{id}` | Exclui (204; 404 se não existe) |
| GET | `/api/proprietarios` | Lista proprietários com a contagem de imóveis |
| GET | `/api/proprietarios/{id}/imoveis` | Imóveis de um proprietário |
| PUT | `/api/proprietarios/{id}` | Renomeia (409 se o nome já existe) |

## Decisões

- **Spring Data JPA no lugar de SQL nativo concatenado.** Elimina o SQL Injection que existia em
  toda escrita/leitura, e devolve o controle de transação ao framework. Foi a correção mais
  importante da Parte 1 (detalhes em [../REVIEW.md](../REVIEW.md)).
- **DTOs + Bean Validation.** Fecham o over-posting (`@RequestBody Object`) e barram dados
  inválidos (lat/long fora de faixa, campos vazios) antes do banco.
- **Flyway + `ddl-auto=validate`.** Migração versionada, testável e reversível — necessária para
  transformar o proprietário em entidade sem perder os dados existentes, o que o `ddl-auto=update`
  não faz com segurança.
- **Proprietário como entidade + _find-or-create_ por nome.** O contrato da API manteve
  `proprietario` como nome (string) no request/response — o front das tarefas 1–3 não quebrou —,
  mas internamente o nome é normalizado numa entidade. Isso habilita a tarefa 5: **renomear é um
  `UPDATE` numa linha** e todos os imóveis do dono refletem via FK (contra N updates no modelo
  antigo).
- **Paginação no servidor (`Pageable`) + `PageResponse`.** Só a página pedida trafega. Medição com
  **10.012 imóveis / 1.010 municípios**: página de 20 = ~6 KB; carregar tudo = ~621 KB (~100×). O
  ganho maior está no front (renderizar 20 linhas vs. 10 mil). `PageResponse` dá um contrato de
  paginação estável em vez de expor a serialização interna de `Page`. **Assumido**: paginação
  clássica; para o próximo passo de escala, o filtro por bounding box no mapa e índices já criados
  em `municipio` e `proprietario_id`.
- **Endpoint de mapa separado e leve.** `/api/imoveis/mapa` devolve só id/proprietário/município/
  coordenadas, sem trafegar o imóvel inteiro.
- **Segurança e higiene:** credenciais fora do código, CORS restrito à origem do front,
  `open-in-view=false`, e `server.error.include-stacktrace=never` (o devtools ligava o vazamento
  em dev).
