import { Component, computed, inject, OnInit } from '@angular/core';
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
  readonly areaTotal = computed(() =>
    this.imoveis().reduce((soma, i) => soma + (Number(i.areaM2) || 0), 0)
  );

  filtroProprietario = '';
  filtroMunicipio = '';
  mensagem = '';

  // Debounce: só dispara a busca 300ms depois da última tecla, e só se algo mudou.
  private filtros$ = new Subject<string>();

  constructor() {
    this.filtros$
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(() => this.service.buscar(this.filtroProprietario, this.filtroMunicipio));
  }

  ngOnInit(): void {
    this.service.buscar();
  }

  aoFiltrar(): void {
    this.filtros$.next(`${this.filtroProprietario}|${this.filtroMunicipio}`);
  }

  limparFiltros(): void {
    this.filtroProprietario = '';
    this.filtroMunicipio = '';
    this.service.buscar();
  }

  endereco(i: Imovel): string {
    return `${i.rua}, ${i.numero} - ${i.bairro}`;
  }

  excluir(i: Imovel): void {
    if (!confirm(`Excluir o imóvel de ${i.proprietario}?`)) {
      return;
    }
    this.service.excluir(i.id).subscribe({
      next: () => (this.mensagem = 'Imóvel excluído!'),
      error: () => (this.mensagem = 'Erro ao excluir. Tente novamente.'),
    });
  }
}
