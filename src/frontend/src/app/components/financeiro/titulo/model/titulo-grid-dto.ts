export class TituloGridDTO {
  id: string;
  tipo: string;
  status: string;
  numeroDocumento?: string;
  descricao: string;
  pessoaNome: string;
  unidadeNegocioCodigo?: string;
  valorOriginal: number;
  valorPago: number;
  saldo: number;
  dataVencimento: Date;
  parcelamento?: string; // "3/12" se for parcelado
  deleted?: boolean;

  constructor(
    id: string,
    tipo: string,
    status: string,
    numeroDocumento: string | undefined,
    descricao: string,
    pessoaNome: string,
    unidadeNegocioCodigo: string | undefined,
    valorOriginal: number,
    valorPago: number,
    saldo: number,
    dataVencimento: Date,
    parcelamento?: string,
    deleted?: boolean
  ) {
    this.id = id;
    this.tipo = tipo;
    this.status = status;
    this.numeroDocumento = numeroDocumento;
    this.descricao = descricao;
    this.pessoaNome = pessoaNome;
    this.unidadeNegocioCodigo = unidadeNegocioCodigo;
    this.valorOriginal = valorOriginal;
    this.valorPago = valorPago;
    this.saldo = saldo;
    this.dataVencimento = dataVencimento;
    this.parcelamento = parcelamento;
    this.deleted = deleted;
  }
}
