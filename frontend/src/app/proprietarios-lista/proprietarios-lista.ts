import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProprietarioService } from '../proprietario.service';

@Component({
  selector: 'app-proprietarios-lista',
  imports: [RouterLink],
  templateUrl: './proprietarios-lista.html',
  styleUrl: './proprietarios-lista.scss',
})
export class ProprietariosLista implements OnInit {
  private service = inject(ProprietarioService);

  readonly proprietarios = this.service.proprietarios;
  readonly carregando = this.service.carregando;

  ngOnInit(): void {
    this.service.carregar();
  }
}
