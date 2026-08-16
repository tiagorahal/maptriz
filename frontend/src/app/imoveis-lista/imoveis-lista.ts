import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { ImovelService } from '../imovel.service';
import { Imovel } from '../imovel';

@Component({
  selector: 'app-imoveis-lista',
  imports: [RouterLink, FormsModule],
  templateUrl: './imoveis-lista.html',
  styleUrl: './imoveis-lista.scss',
})
export class ImoveisLista implements OnInit {
  private service = inject(ImovelService);

  readonly imoveis = this.service.imoveis;
  readonly carregando = this.service.carregando;
  readonly pagina = this.service.pagina;
  readonly totalPaginas = this.service.totalPaginas;
  readonly totalElementos = this.service.totalElementos;

  readonly mensagem = signal('');

  // Os filtros vivem no service para sobreviverem à navegação (voltar da edição preserva o estado).
  get filtroProprietario(): string {
    return this.service.filtroProprietario();
  }
  set filtroProprietario(v: string) {
    this.service.filtroProprietario.set(v);
  }
  get filtroMunicipio(): string {
    return this.service.filtroMunicipio();
  }
  set filtroMunicipio(v: string) {
    this.service.filtroMunicipio.set(v);
  }

  // Debounce: só dispara a busca 300ms depois da última tecla, e só se algo mudou.
  private filtros$ = new Subject<string>();

  constructor() {
    this.filtros$
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(() => this.service.filtrar());
  }

  ngOnInit(): void {
    this.service.carregarSeNecessario();
  }

  aoFiltrar(): void {
    this.filtros$.next(`${this.filtroProprietario}|${this.filtroMunicipio}`);
  }

  limparFiltros(): void {
    this.filtroProprietario = '';
    this.filtroMunicipio = '';
    this.service.filtrar();
  }

  paginaAnterior(): void {
    this.service.irParaPagina(this.pagina() - 1);
  }

  proximaPagina(): void {
    this.service.irParaPagina(this.pagina() + 1);
  }

  endereco(i: Imovel): string {
    return `${i.rua}, ${i.numero} - ${i.bairro}`;
  }

  excluir(i: Imovel): void {
    if (!confirm(`Excluir o imóvel de ${i.proprietario}?`)) {
      return;
    }
    this.service.excluir(i.id).subscribe({
      next: () => this.mensagem.set('Imóvel excluído!'),
      error: () => this.mensagem.set('Erro ao excluir. Tente novamente.'),
    });
  }
}
