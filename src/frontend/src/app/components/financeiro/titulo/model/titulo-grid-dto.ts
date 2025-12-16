export class TituloGridDTO {
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

  constructor(
    id: string,
    tipo: string,
    status: string,
    numeroDocumento: string | undefined,
    descricao: string,
    pessoaNome: string,
    unidadeNegocioCodigo: string | undefined,
    valorOriginal: number,
    saldo: number,
    dataVencimento: Date,
    parcelamento?: string
  ) {
    this.id = id;
    this.tipo = tipo;
    this.status = status;
    this.numeroDocumento = numeroDocumento;
    this.descricao = descricao;
    this.pessoaNome = pessoaNome;
    this.unidadeNegocioCodigo = unidadeNegocioCodigo;
    this.valorOriginal = valorOriginal;
    this.saldo = saldo;
    this.dataVencimento = dataVencimento;
    this.parcelamento = parcelamento;
  }
}
