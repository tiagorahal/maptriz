import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProprietarioService } from '../proprietario.service';
import { Imovel } from '../imovel';

@Component({
  selector: 'app-proprietario-detalhe',
  imports: [RouterLink],
  templateUrl: './proprietario-detalhe.html',
  styleUrl: './proprietario-detalhe.scss',
})
export class ProprietarioDetalhe implements OnInit {
  private service = inject(ProprietarioService);
  private route = inject(ActivatedRoute);

  id = 0;
  // App é zoneless: estado lido no template precisa ser signal para re-renderizar.
  readonly nome = signal('');
  readonly carregando = signal(false);
  readonly imoveis = signal<Imovel[]>([]);

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    // Se veio da listagem, o nome já está em memória (sem request extra).
    this.nome.set(this.service.nomePorId(this.id) ?? '');

    this.carregando.set(true);
    this.service.imoveisDe(this.id).subscribe({
      next: (lista) => {
        this.imoveis.set(lista);
        // Acesso direto/refresh: sem o nome em memória, usa o do próprio imóvel.
        if (!this.nome() && lista.length) {
          this.nome.set(lista[0].proprietario);
        }
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  endereco(i: Imovel): string {
    return `${i.rua}, ${i.numero} - ${i.bairro}`;
  }
}
