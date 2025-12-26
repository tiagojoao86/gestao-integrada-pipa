export class ContaBancariaGridDTO {
  id: string;
  nome: string;
  banco?: string;
  tipo: string;
  saldoInicial?: number;
  unidadeNegocioCodigo?: string;
  ativa: boolean;
  deleted?: boolean;

  constructor(
    id: string,
    nome: string,
    banco: string | undefined,
    tipo: string,
    saldoInicial: number | undefined,
    unidadeNegocioCodigo: string | undefined,
    ativa: boolean,
    deleted?: boolean
  ) {
    this.id = id;
    this.nome = nome;
    this.banco = banco;
    this.tipo = tipo;
    this.saldoInicial = saldoInicial;
    this.unidadeNegocioCodigo = unidadeNegocioCodigo;
    this.ativa = ativa;
    this.deleted = deleted;
  }
}
