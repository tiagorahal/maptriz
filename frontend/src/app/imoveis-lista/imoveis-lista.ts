import { Component, computed, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ImovelService } from '../imovel.service';
import { Imovel } from '../imovel';

@Component({
  selector: 'app-imoveis-lista',
  imports: [RouterLink],
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

  mensagem = '';

  ngOnInit(): void {
    this.service.carregar();
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
