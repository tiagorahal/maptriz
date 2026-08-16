import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProprietarioService } from '../proprietario.service';
import { Imovel } from '../imovel';

@Component({
  selector: 'app-proprietario-detalhe',
  imports: [RouterLink, FormsModule],
  templateUrl: './proprietario-detalhe.html',
  styleUrl: './proprietario-detalhe.scss',
})
export class ProprietarioDetalhe implements OnInit {
  private service = inject(ProprietarioService);
  private route = inject(ActivatedRoute);

  id = 0;
  readonly nome = signal('');
  readonly carregando = signal(false);
  readonly imoveis = signal<Imovel[]>([]);

  // Estado do "renomear".
  novoNome = '';
  readonly editando = signal(false);
  readonly salvandoNome = signal(false);
  readonly erroNome = signal('');

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    // Se veio da listagem, o nome já está em memória (sem request extra).
    this.nome.set(this.service.nomePorId(this.id) ?? '');

    this.carregando.set(true);
    this.service.imoveisDe(this.id).subscribe({
      next: (lista) => {
        this.imoveis.set(lista);
        if (!this.nome() && lista.length) {
          this.nome.set(lista[0].proprietario);
        }
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  abrirRenomear(): void {
    this.novoNome = this.nome();
    this.erroNome.set('');
    this.editando.set(true);
  }

  cancelarRenomear(): void {
    this.editando.set(false);
  }

  renomear(): void {
    this.salvandoNome.set(true);
    this.erroNome.set('');
    this.service.renomear(this.id, this.novoNome).subscribe({
      next: (p) => {
        this.nome.set(p.nome);
        this.salvandoNome.set(false);
        this.editando.set(false);
      },
      error: (e) => {
        this.salvandoNome.set(false);
        this.erroNome.set(e?.error?.mensagem || 'Erro ao renomear. Tente novamente.');
      },
    });
  }

  endereco(i: Imovel): string {
    return `${i.rua}, ${i.numero} - ${i.bairro}`;
  }
}
