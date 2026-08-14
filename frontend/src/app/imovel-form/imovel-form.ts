import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ImovelService } from '../imovel.service';
import { ImovelInput } from '../imovel';

@Component({
  selector: 'app-imovel-form',
  imports: [FormsModule, RouterLink],
  templateUrl: './imovel-form.html',
  styleUrl: './imovel-form.scss',
})
export class ImovelForm {
  private service = inject(ImovelService);
  private router = inject(Router);

  salvando = false;
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

  salvar(): void {
    this.salvando = true;
    this.erro = '';
    this.service.criar(this.form).subscribe({
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
