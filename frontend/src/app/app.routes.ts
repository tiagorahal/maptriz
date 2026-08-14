import { Routes } from '@angular/router';
import { ImoveisLista } from './imoveis-lista/imoveis-lista';
import { ImovelForm } from './imovel-form/imovel-form';
import { ImovelEditar } from './imovel-editar/imovel-editar';
import { ProprietariosLista } from './proprietarios-lista/proprietarios-lista';
import { ProprietarioDetalhe } from './proprietario-detalhe/proprietario-detalhe';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'imoveis' },
  { path: 'imoveis', component: ImoveisLista },
  { path: 'imoveis/novo', component: ImovelForm },
  { path: 'imoveis/:id/editar', component: ImovelEditar },
  { path: 'proprietarios', component: ProprietariosLista },
  { path: 'proprietarios/:id', component: ProprietarioDetalhe },
  { path: '**', redirectTo: 'imoveis' },
];
