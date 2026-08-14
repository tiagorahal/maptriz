import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Imovel, ImovelInput } from './imovel';

/**
 * Centraliza o acesso à API e mantém em memória, como fonte de verdade, tanto a lista de imóveis
 * quanto o estado dos filtros. Assim a navegação (ex.: voltar da edição para a listagem)
 * reaproveita o que já está em memória, sem uma nova requisição.
 */
@Injectable({ providedIn: 'root' })
export class ImovelService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/imoveis';

  readonly imoveis = signal<Imovel[]>([]);
  readonly carregando = signal(false);
  readonly filtroProprietario = signal('');
  readonly filtroMunicipio = signal('');

  /** Se a lista já foi buscada nesta sessão (diferente de "lista vazia"). */
  private carregado = false;

  /**
   * Carrega a lista só se ainda não tiver sido carregada nesta sessão. É o que garante o
   * requisito da tarefa 3: ao voltar da edição, a listagem reaproveita a memória, sem novo GET.
   */
  carregarSeNecessario(): void {
    if (!this.carregado) {
      this.buscar();
    }
  }

  /** Busca no servidor usando os filtros atuais. Sempre refaz a requisição (usado ao filtrar). */
  buscar(): void {
    this.carregando.set(true);
    let params = new HttpParams();
    const proprietario = this.filtroProprietario().trim();
    const municipio = this.filtroMunicipio().trim();
    if (proprietario) {
      params = params.set('proprietario', proprietario);
    }
    if (municipio) {
      params = params.set('municipio', municipio);
    }
    this.http.get<Imovel[]>(this.baseUrl, { params }).subscribe({
      next: (lista) => {
        this.imoveis.set(lista);
        this.carregado = true;
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  /** Item já em memória (sem ir ao servidor) — a edição usa isto para não refazer o GET. */
  daMemoria(id: number): Imovel | undefined {
    return this.imoveis().find((i) => i.id === id);
  }

  /** Fallback: busca um imóvel direto no servidor (acesso direto/refresh na página de edição). */
  buscarUm(id: number): Observable<Imovel> {
    return this.http.get<Imovel>(`${this.baseUrl}/${id}`);
  }

  criar(input: ImovelInput): Observable<Imovel> {
    return this.http.post<Imovel>(this.baseUrl, input).pipe(
      tap((novo) => this.imoveis.update((lista) => [...lista, novo]))
    );
  }

  atualizar(id: number, input: ImovelInput): Observable<Imovel> {
    return this.http.put<Imovel>(`${this.baseUrl}/${id}`, input).pipe(
      tap((atualizado) =>
        this.imoveis.update((lista) => lista.map((i) => (i.id === id ? atualizado : i)))
      )
    );
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      tap(() => this.imoveis.update((lista) => lista.filter((i) => i.id !== id)))
    );
  }
}
