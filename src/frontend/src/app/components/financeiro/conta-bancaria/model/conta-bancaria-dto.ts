export class ContaBancariaDTO {
  id?: string;
  nome: string;
  banco?: string;
  agencia?: string;
  numeroConta?: string;
  tipo: string; // CORRENTE, POUPANCA, CAIXA, INVESTIMENTO
  saldoInicial?: number;
  unidadeNegocioId?: string;
  unidadeNegocioNome?: string;
  unidadeNegocioCodigo?: string;
  ativa?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;

  constructor(
    id?: string,
    nome?: string,
    banco?: string,
    agencia?: string,
    numeroConta?: string,
    tipo?: string,
    saldoInicial?: number,
    unidadeNegocioId?: string,
    unidadeNegocioNome?: string,
    unidadeNegocioCodigo?: string,
    ativa?: boolean,
    createdAt?: Date,
    updatedAt?: Date,
    createdBy?: string,
    updatedBy?: string
  ) {
    this.id = id;
    this.nome = nome ?? '';
    this.banco = banco;
    this.agencia = agencia;
    this.numeroConta = numeroConta;
    this.tipo = tipo ?? '';
    this.saldoInicial = saldoInicial;
    this.unidadeNegocioId = unidadeNegocioId;
    this.unidadeNegocioNome = unidadeNegocioNome;
    this.unidadeNegocioCodigo = unidadeNegocioCodigo;
    this.ativa = ativa;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
  }
 
}
