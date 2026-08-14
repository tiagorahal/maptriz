import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Imovel, ImovelInput } from './imovel';

/**
 * Centraliza o acesso à API e mantém a lista de imóveis em memória como fonte de verdade.
 * As mutações atualizam esse estado a partir da resposta do servidor, sem um novo GET —
 * o que já prepara o terreno para a tarefa 3 (voltar da edição sem refetch).
 */
@Injectable({ providedIn: 'root' })
export class ImovelService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/imoveis';

  readonly imoveis = signal<Imovel[]>([]);
  readonly carregando = signal(false);

  /**
   * Busca a lista no servidor, opcionalmente filtrada por proprietário e/ou município.
   * O filtro é server-side (query params) — pensando na tarefa 6, onde a base é grande demais
   * para filtrar no cliente.
   */
  buscar(proprietario = '', municipio = ''): void {
    this.carregando.set(true);
    let params = new HttpParams();
    if (proprietario.trim()) {
      params = params.set('proprietario', proprietario.trim());
    }
    if (municipio.trim()) {
      params = params.set('municipio', municipio.trim());
    }
    this.http.get<Imovel[]>(this.baseUrl, { params }).subscribe({
      next: (lista) => {
        this.imoveis.set(lista);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  buscarPorId(id: number): Imovel | undefined {
    return this.imoveis().find((i) => i.id === id);
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
