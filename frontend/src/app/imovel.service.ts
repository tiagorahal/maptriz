import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Imovel, ImovelInput, Pagina, PontoImovel } from './imovel';

/**
 * Acesso à API de imóveis com paginação no servidor. Mantém em memória, como fonte de verdade,
 * a página atual, os filtros e os metadados de paginação — o que sustenta a listagem em grande
 * volume (só a página pedida trafega) e o reaproveitamento ao voltar da edição (tarefa 3).
 */
@Injectable({ providedIn: 'root' })
export class ImovelService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/imoveis';

  readonly imoveis = signal<Imovel[]>([]); // conteúdo da página atual
  readonly carregando = signal(false);
  readonly filtroProprietario = signal('');
  readonly filtroMunicipio = signal('');
  readonly pagina = signal(0); // 0-based
  readonly tamanhoPagina = signal(20);
  readonly totalElementos = signal(0);
  readonly totalPaginas = signal(0);

  private carregado = false;

  /** Carrega só se ainda não carregou nesta sessão — reaproveita a memória ao voltar da edição. */
  carregarSeNecessario(): void {
    if (!this.carregado) {
      this.buscar();
    }
  }

  /** Busca a página atual no servidor, com os filtros atuais. */
  buscar(): void {
    this.carregando.set(true);
    let params = new HttpParams()
      .set('page', this.pagina())
      .set('size', this.tamanhoPagina());
    const proprietario = this.filtroProprietario().trim();
    const municipio = this.filtroMunicipio().trim();
    if (proprietario) {
      params = params.set('proprietario', proprietario);
    }
    if (municipio) {
      params = params.set('municipio', municipio);
    }

    this.http.get<Pagina<Imovel>>(this.baseUrl, { params }).subscribe({
      next: (pg) => {
        this.imoveis.set(pg.content);
        this.totalElementos.set(pg.totalElements);
        this.totalPaginas.set(pg.totalPages);
        this.pagina.set(pg.page);
        this.carregado = true;
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  /** Reaplica os filtros voltando à primeira página. */
  filtrar(): void {
    this.pagina.set(0);
    this.buscar();
  }

  irParaPagina(p: number): void {
    if (p < 0 || (this.totalPaginas() > 0 && p >= this.totalPaginas())) {
      return;
    }
    this.pagina.set(p);
    this.buscar();
  }

  /** Item da página atual em memória (a edição usa isto para não refazer o GET). */
  daMemoria(id: number): Imovel | undefined {
    return this.imoveis().find((i) => i.id === id);
  }

  /** Fallback: busca um imóvel direto no servidor (acesso direto/refresh na edição). */
  buscarUm(id: number): Observable<Imovel> {
    return this.http.get<Imovel>(`${this.baseUrl}/${id}`);
  }

  /** Pontos (coordenadas) de todos os imóveis, para o mapa. */
  pontos(): Observable<PontoImovel[]> {
    return this.http.get<PontoImovel[]>(`${this.baseUrl}/mapa`);
  }

  criar(input: ImovelInput): Observable<Imovel> {
    // O novo item pode cair em outra página; invalida o cache para recarregar ao voltar à lista.
    return this.http.post<Imovel>(this.baseUrl, input).pipe(tap(() => (this.carregado = false)));
  }

  atualizar(id: number, input: ImovelInput): Observable<Imovel> {
    // O item editado está na página atual — atualiza em memória (tarefa 3: voltar sem refetch).
    return this.http.put<Imovel>(`${this.baseUrl}/${id}`, input).pipe(
      tap((atualizado) =>
        this.imoveis.update((lista) => lista.map((i) => (i.id === id ? atualizado : i)))
      )
    );
  }

  excluir(id: number): Observable<void> {
    // Recarrega a página atual para manter a contagem total e o preenchimento corretos.
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(tap(() => this.buscar()));
  }

  /** Propaga um novo nome de proprietário para os imóveis já em memória (tarefa 5). */
  atualizarNomeProprietario(proprietarioId: number, novoNome: string): void {
    this.imoveis.update((lista) =>
      lista.map((i) => (i.proprietarioId === proprietarioId ? { ...i, proprietario: novoNome } : i))
    );
  }
}
