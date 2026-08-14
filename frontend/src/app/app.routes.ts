import { Routes } from '@angular/router';
import { ImoveisLista } from './imoveis-lista/imoveis-lista';
import { ImovelForm } from './imovel-form/imovel-form';
import { ImovelEditar } from './imovel-editar/imovel-editar';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'imoveis' },
  { path: 'imoveis', component: ImoveisLista },
  { path: 'imoveis/novo', component: ImovelForm },
  { path: 'imoveis/:id/editar', component: ImovelEditar },
  { path: '**', redirectTo: 'imoveis' },
];
