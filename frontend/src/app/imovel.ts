export interface Imovel {
  id: number;
  proprietarioId: number;
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

// O que o formulário envia — sem id, sem proprietarioId (envia o nome) e sem auditoria.
export type ImovelInput = Omit<Imovel, 'id' | 'proprietarioId' | 'criadoEm' | 'atualizadoEm'>;
