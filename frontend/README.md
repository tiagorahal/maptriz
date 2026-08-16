# Frontend — WebGIS

Interface do cadastro de imóveis. **Angular 22** (componentes standalone, **signals**, aplicação
**zoneless**), TypeScript, **Leaflet** + OpenStreetMap para o mapa.

## Como rodar

Pré-requisito: **Node.js 20+**. O backend precisa estar no ar em `http://localhost:8080`.

```bash
npm install
npm start        # http://localhost:4200
```

## Arquitetura

Telas standalone com roteamento, e o estado vivendo em serviços (`signal`s) como fonte de verdade:

| Rota | Componente | Papel |
|---|---|---|
| `/imoveis` | `ImoveisLista` | listagem paginada, filtros, excluir |
| `/imoveis/novo` | `ImovelForm` | cadastro |
| `/imoveis/:id/editar` | `ImovelEditar` | edição dedicada |
| `/proprietarios` | `ProprietariosLista` | proprietários com contagem de imóveis |
| `/proprietarios/:id` | `ProprietarioDetalhe` | imóveis do proprietário + renomear |
| `/mapa` | `Mapa` | imóveis como pontos no mapa |

- `ImovelService` — acesso à API de imóveis + estado (página atual, filtros, paginação) em signals.
- `ProprietarioService` — proprietários + renomear (propaga o novo nome para o cache de imóveis).

## Decisões

- **Extração do `ImovelService` com estado.** Tirar o `HttpClient` e as URLs de dentro do
  componente foi a base para tudo que veio depois (cache, filtros, paginação). O estado é um
  `signal`, fonte de verdade única.
- **Filtros server-side com debounce.** A busca só dispara 300ms após a última tecla (e só se
  algo mudou), casando com a paginação do backend — pensando no cenário de grande volume.
- **Edição sem novo request ao voltar (tarefa 3).** A lista carrega uma vez por sessão
  (`carregarSeNecessario`); a edição lê o imóvel da memória e, ao salvar, atualiza o item no
  próprio estado. Voltar para a listagem **não refaz o GET** — só reaproveita o que já está lá.
  O estado dos filtros também vive no service, então a navegação preserva tudo.
- **Estado de UI como `signal` (app zoneless).** Sem Zone.js, um campo comum alterado num callback
  assíncrono não dispara change detection e a tela "trava" (ex.: um `Carregando...` que nunca some).
  Por isso todo estado reativo lido no template (`carregando`, `salvando`, `erro`, `mensagem`,
  `nome`…) é `signal`. Esse foi um bug real, encontrado testando a página de proprietários.
- **Paginação clássica.** Controles Anterior/Próxima sobre o `PageResponse` do backend — só a
  página atual é renderizada (20 linhas em vez de milhares). Filtrar volta para a primeira página.
- **Renomear propaga para o cache.** Ao renomear um proprietário, o novo nome é aplicado tanto na
  lista de proprietários quanto nos imóveis já carregados em memória (o backend já propagou via FK).
- **Mapa com `circleMarker`.** Os imóveis são plotados como círculos, o que evita o problema
  clássico dos ícones padrão do Leaflet quebrarem com bundler; `fitBounds` enquadra os pontos e
  cada um tem popup com proprietário e município.
