import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Proprietario } from './proprietario';
import { Imovel } from './imovel';

@Injectable({ providedIn: 'root' })
export class ProprietarioService {
  private http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/proprietarios';

  readonly proprietarios = signal<Proprietario[]>([]);
  readonly carregando = signal(false);

  carregar(): void {
    this.carregando.set(true);
    this.http.get<Proprietario[]>(this.baseUrl).subscribe({
      next: (lista) => {
        this.proprietarios.set(lista);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  /** Nome já em memória (para o cabeçalho da página de detalhe, sem novo request). */
  nomePorId(id: number): string | undefined {
    return this.proprietarios().find((p) => p.id === id)?.nome;
  }

  imoveisDe(id: number): Observable<Imovel[]> {
    return this.http.get<Imovel[]>(`${this.baseUrl}/${id}/imoveis`);
  }
}
