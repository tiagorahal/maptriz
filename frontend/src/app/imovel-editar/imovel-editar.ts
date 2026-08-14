import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ImovelService } from '../imovel.service';
import { Imovel, ImovelInput } from '../imovel';

@Component({
  selector: 'app-imovel-editar',
  imports: [FormsModule, RouterLink],
  templateUrl: './imovel-editar.html',
  styleUrl: './imovel-editar.scss',
})
export class ImovelEditar implements OnInit {
  private service = inject(ImovelService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  id = 0;
  carregando = false;
  salvando = false;
  naoEncontrado = false;
  erro = '';

  form: ImovelInput = {
    proprietario: '',
    municipio: '',
    uf: '',
    bairro: '',
    rua: '',
    numero: '',
    latitude: null as unknown as number,
    longitude: null as unknown as number,
    areaM2: null as unknown as number,
    ativo: true,
  };

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));

    const emMemoria = this.service.daMemoria(this.id);
    if (emMemoria) {
      // Reaproveita o que já veio na listagem — sem novo GET.
      this.preencher(emMemoria);
    } else {
      // Acesso direto/refresh: a lista não está em memória, então busca só este imóvel.
      this.carregando = true;
      this.service.buscarUm(this.id).subscribe({
        next: (i) => {
          this.preencher(i);
          this.carregando = false;
        },
        error: () => {
          this.naoEncontrado = true;
          this.carregando = false;
        },
      });
    }
  }

  private preencher(i: Imovel): void {
    // Cópia campo a campo — nunca a mesma referência do objeto da lista (evita o bug de aliasing
    // do código original, em que editar o formulário alterava a linha da tabela ao vivo).
    this.form = {
      proprietario: i.proprietario,
      municipio: i.municipio,
      uf: i.uf,
      bairro: i.bairro,
      rua: i.rua,
      numero: i.numero,
      latitude: i.latitude,
      longitude: i.longitude,
      areaM2: i.areaM2,
      ativo: i.ativo,
    };
  }

  salvar(): void {
    this.salvando = true;
    this.erro = '';
    this.service.atualizar(this.id, this.form).subscribe({
      next: () => this.router.navigate(['/imoveis']),
      error: (e) => {
        this.salvando = false;
        this.erro = this.mensagemErro(e);
      },
    });
  }

  private mensagemErro(e: any): string {
    if (e?.error?.campos) {
      return 'Corrija os campos: ' + Object.keys(e.error.campos).join(', ');
    }
    return e?.error?.mensagem || 'Erro ao salvar. Tente novamente.';
  }
}
