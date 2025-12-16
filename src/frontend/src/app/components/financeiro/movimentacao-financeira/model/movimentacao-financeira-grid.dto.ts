export class MovimentacaoFinanceiraGridDTO {
  id: string;
  data: string;
  valor: number;
  tipo: string;
  unidadeNegocioId: string;
  unidadeNegocioNome: string;
  contaBancaria?: string;
  contaBancariaNome?: string;

  constructor(
    id: string,
    data: string,
    valor: number,
    tipo: string,
    unidadeNegocioId: string,
    unidadeNegocioNome: string,
    contaBancaria?: string,
    contaBancariaNome?: string
  ) {
    this.id = id;
    this.data = data;
    this.valor = valor;
    this.tipo = tipo;
    this.unidadeNegocioId = unidadeNegocioId;
    this.unidadeNegocioNome = unidadeNegocioNome;
    this.contaBancaria = contaBancaria;
    this.contaBancariaNome = contaBancariaNome;
  }
}
