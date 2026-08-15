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
// Largura/comprimento (metros) são opcionais e geram o polígono georreferenciado (tarefa 8).
export type ImovelInput = Omit<Imovel, 'id' | 'proprietarioId' | 'criadoEm' | 'atualizadoEm'> & {
  largura?: number;
  comprimento?: number;
};

// Envelope de paginação retornado pela API (espelha o PageResponse do backend).
export interface Pagina<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// Projeção leve para o mapa (espelha o PontoImovelResponse do backend).
export interface PontoImovel {
  id: number;
  proprietario: string;
  municipio: string;
  latitude: number;
  longitude: number;
  poligono?: number[][]; // vértices [lat, lng]
}
