export class TituloDTO {
  id?: string;
  tipo: string; // A_PAGAR, A_RECEBER
  status?: string; // ABERTO, PARCIAL, PAGO, CANCELADO, VENCIDO
  numeroDocumento?: string;
  descricao: string;

  // Relacionamentos
  pessoaId: string;
  pessoaNome?: string;
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

  constructor(
    id?: string,
    tipo?: string,
    status?: string,
    numeroDocumento?: string,
    descricao?: string,
    pessoaId?: string,
    pessoaNome?: string,
    unidadeNegocioId?: string,
    unidadeNegocioNome?: string,
    valorOriginal?: number,
    valorPago?: number,
    valorDesconto?: number,
    valorJuros?: number,
    valorMulta?: number,
    saldo?: number,
    dataEmissao?: Date,
    dataVencimento?: Date,
    dataPagamento?: Date,
    observacoes?: string,
    numeroParcela?: number,
    totalParcelas?: number,
    tituloOrigemId?: string,
    createdAt?: Date,
    updatedAt?: Date,
    createdBy?: string,
    updatedBy?: string
  ) {
    this.id = id;
    this.tipo = tipo!;
    this.status = status;
    this.numeroDocumento = numeroDocumento;
    this.descricao = descricao!;
    this.pessoaId = pessoaId!;
    this.pessoaNome = pessoaNome;
    this.unidadeNegocioId = unidadeNegocioId;
    this.unidadeNegocioNome = unidadeNegocioNome;
    this.valorOriginal = valorOriginal!;
    this.valorPago = valorPago;
    this.valorDesconto = valorDesconto;
    this.valorJuros = valorJuros;
    this.valorMulta = valorMulta;
    this.saldo = saldo;
    this.dataEmissao = dataEmissao!;
    this.dataVencimento = dataVencimento!;
    this.dataPagamento = dataPagamento;
    this.observacoes = observacoes;
    this.numeroParcela = numeroParcela;
    this.totalParcelas = totalParcelas;
    this.tituloOrigemId = tituloOrigemId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
  }


}
