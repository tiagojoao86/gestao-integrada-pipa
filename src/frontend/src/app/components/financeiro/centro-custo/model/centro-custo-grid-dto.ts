export class CentroCustoGridDTO {
  id: string;
  nome: string;
  centroResultado?: boolean;
  unidadeNegocioCodigo: string;
  deleted?: boolean;

  constructor(
    id: string,
    nome: string,
    unidadeNegocioCodigo: string,
    centroResultado?: boolean,
    deleted?: boolean
  ) {
    this.id = id;
    this.nome = nome;
    this.centroResultado = centroResultado;
    this.unidadeNegocioCodigo = unidadeNegocioCodigo;
    this.deleted = deleted;
  }
}
