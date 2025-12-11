export interface MovimentacaoTituloDTO {
  id?: string;
  descricao?: string;
}

export interface MovimentacaoFinanceiraDTO {
  id?: string;
  titulos: MovimentacaoTituloDTO[];
  contaBancariaId?: string;
  // legacy/display field kept for UI; backend expects contaBancariaId
  contaBancaria?: string;
  tipo: string;
  formaPagamento: string;
  valor: number;
  data: string;
  unidadeNegocio: string;
  observacoes?: string;
}
