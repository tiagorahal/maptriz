# Maptriz — Cadastro de imóveis georreferenciados

Solução do teste técnico da Maptriz. O enunciado original está preservado em
[DESAFIO.md](DESAFIO.md).

Cadastro de imóveis georreferenciados: listar, cadastrar, editar, excluir, filtrar, agrupar por
proprietário e visualizar num mapa. Backend em Java/Spring, frontend em Angular, banco PostgreSQL
com migrações versionadas.

## Stack

| Camada | Tecnologias |
|---|---|
| Backend | Java 21, Spring Boot 3.5.16, Maven, Spring Data JPA / Hibernate, Bean Validation, **Flyway**, Lombok |
| Banco | PostgreSQL 16 |
| Frontend | Angular 22 (standalone, **signals**, zoneless), TypeScript, **Leaflet** + OpenStreetMap |

Detalhes de cada lado (arquitetura, como rodar, decisões) estão em
[backend/README.md](backend/README.md) e [frontend/README.md](frontend/README.md).

## Como rodar (resumo)

Pré-requisitos: **JDK 21**, **PostgreSQL** em `localhost:5432`, **Node.js 20+**.

```bash
# 1. Banco — cria a base 'webgis' (o schema e o seed vêm das migrações Flyway na 1ª subida)
psql -U postgres -f scripts/setup-db.sql

# 2. Backend — http://localhost:8080
cd backend && ./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run

# 3. Frontend — http://localhost:4200
cd frontend && npm install && npm start
```

A senha do Postgres é externalizável por variável de ambiente (`DB_PASSWORD`); o default local é
`postgres`. Veja [backend/README.md](backend/README.md#configuração).

## Documentação

- **[REVIEW.md](REVIEW.md)** — Parte 1: revisão de código com os problemas encontrados (por
  gravidade, com evidências rodadas no sistema) e o antes×depois do refactor.
- **[backend/README.md](backend/README.md)** — arquitetura, migrações, endpoints e decisões do backend.
- **[frontend/README.md](frontend/README.md)** — arquitetura, telas e decisões do frontend.

## O que foi feito

**Parte 1 — revisão + refactor** ([REVIEW.md](REVIEW.md)): 14 problemas classificados por
gravidade. Refatorados os críticos — **SQL Injection** (SQL nativo concatenado → Spring Data JPA),
o **"sucesso fantasma"** da tela (erro correto no back + tratamento no front), **stack trace
vazada**, **validação/over-posting** (DTOs + Bean Validation), **seed duplicando a cada boot**,
credenciais e CORS.

**Parte 2 — tarefas:**

| # | Tarefa | Destaque |
|---|--------|----------|
| 1 | Duas páginas (lista / cadastro) | extração do `ImovelService` com estado (signals) |
| 2 | Filtros por proprietário e município | server-side + debounce |
| 3 | Página de edição | **voltar para a lista sem novo request** (reaproveita a memória) |
| 4 | Proprietário como entidade + relacionamento | **migração Flyway sem perder os 12 imóveis** |
| 5 | Renomear proprietário | um `UPDATE` propaga para todos os imóveis dele (via FK) |
| 6 | Listagem para grande volume | paginação no servidor e na UI — medido com 10 mil registros |
| 7 | Mapa | Leaflet + OpenStreetMap, imóveis como pontos |
| 8 | PostGIS (opcional sênior) | não implementada |

## Fluxo de trabalho (git)

Uma branch por unidade de trabalho, cada uma com vários commits e mergeada na `development` via
Pull Request; a `main` guarda a base e recebe a `development` ao final.

```
main ──● (base) ─────────────────────────────────────────────► entrega
        \
development ──●──●──●──●──●──●──●──●  (integração)
               \   \   \
       refactor/parte-1  feat/duas-paginas  feat/filtros  feat/edicao
       feat/proprietario  feat/renomear-proprietario  feat/paginacao  feat/mapa
```
