import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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

  /** Marca se a lista já foi buscada do servidor nesta sessão (diferente de "lista vazia"). */
  private carregado = false;

  /**
   * Carrega a lista uma vez por sessão. Se já foi carregada, reaproveita o que está em memória
   * (base da tarefa 3: voltar sem refetch). Use forcar=true para recarregar de propósito.
   */
  carregar(forcar = false): void {
    if (this.carregado && !forcar) {
      return;
    }
    this.carregando.set(true);
    this.http.get<Imovel[]>(this.baseUrl).subscribe({
      next: (lista) => {
        this.imoveis.set(lista);
        this.carregado = true;
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
