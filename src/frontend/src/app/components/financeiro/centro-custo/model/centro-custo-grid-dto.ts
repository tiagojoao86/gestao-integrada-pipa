export class CentroCustoGridDTO {
  id: string;
  nome: string;
  centroResultado?: boolean;
  unidadeNegocioCodigo: string;

  constructor(
    id: string,
    nome: string,
    unidadeNegocioCodigo: string,
    centroResultado?: boolean
  ) {
    this.id = id;
    this.nome = nome;
    this.centroResultado = centroResultado;
    this.unidadeNegocioCodigo = unidadeNegocioCodigo;
  }
}
