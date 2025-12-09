export interface TituloGridDTO {
  id: string;
  tipo: string;
  status: string;
  numeroDocumento?: string;
  descricao: string;
  pessoaNome: string;
  unidadeNegocioCodigo?: string;
  valorOriginal: number;
  saldo: number;
  dataVencimento: Date;
  parcelamento?: string; // "3/12" se for parcelado
}
