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

// Envelope de paginação retornado pela API (espelha o PageResponse do backend).
export interface Pagina<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
