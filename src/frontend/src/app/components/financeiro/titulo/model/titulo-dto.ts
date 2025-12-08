export interface TituloDTO {
  id?: string;
  tipo: string; // A_PAGAR, A_RECEBER
  status?: string; // ABERTO, PARCIAL, PAGO, CANCELADO, VENCIDO
  numeroDocumento?: string;
  descricao: string;

  // Relacionamentos
  pessoaId: string;
  pessoaNome?: string;
  planoContasId: string;
  planoContasDescricao?: string;
  unidadeNegocioId?: string;
  unidadeNegocioNome?: string;

  // Valores
  valorOriginal: number;
  valorPago?: number;
  valorDesconto?: number;
  valorJuros?: number;
  valorMulta?: number;
  saldo?: number;

  // Datas
  dataEmissao: Date;
  dataVencimento: Date;
  dataPagamento?: Date;

  observacoes?: string;

  // Parcelamento
  numeroParcela?: number;
  totalParcelas?: number;
  tituloOrigemId?: string;

  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;
}
