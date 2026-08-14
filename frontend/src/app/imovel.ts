export interface Imovel {
  id: number;
  proprietario: string;
  municipio: string;
  uf: string;
  bairro: string;
  rua: string;
  numero: string;
  latitude: number;
  longitude: number;
  areaM2: number;
  ativo: boolean;
  criadoEm?: string;
  atualizadoEm?: string;
}

// O que o formulário envia — sem id nem campos de auditoria (o backend controla isso).
export type ImovelInput = Omit<Imovel, 'id' | 'criadoEm' | 'atualizadoEm'>;
