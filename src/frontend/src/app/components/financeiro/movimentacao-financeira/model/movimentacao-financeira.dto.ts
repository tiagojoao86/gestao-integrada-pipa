export class MovimentacaoTituloDTO {
  id?: string;
  descricao?: string;
  valor?: number;
  tipo?: string;

  constructor(id?: string, descricao?: string, valor?: number, tipo?: string) {
    this.id = id;
    this.descricao = descricao;
    this.valor = valor;
    this.tipo = tipo;
  }
}

export class MovimentacaoFinanceiraDTO {
  id?: string;
  titulos: MovimentacaoTituloDTO[];
  contaBancariaId?: string;
  // legacy/display field kept for UI; backend expects contaBancariaId
  contaBancaria?: string;
  tipo: string;
  formaPagamento: string;
  valor: number;
  data: string;
  unidadeNegocioId: string;
  observacoes?: string;

  constructor(
    id?: string,
    titulos: MovimentacaoTituloDTO[] = [],
    contaBancariaId?: string,
    contaBancaria?: string,
    tipo = '',
    formaPagamento = '',
    valor = 0,
    data = '',
    unidadeNegocioId = '',
    observacoes?: string
  ) {
    this.id = id;
    this.titulos = titulos;
    this.contaBancariaId = contaBancariaId;
    this.contaBancaria = contaBancaria;
    this.tipo = tipo;
    this.formaPagamento = formaPagamento;
    this.valor = valor;
    this.data = data;
    this.unidadeNegocioId = unidadeNegocioId;
    this.observacoes = observacoes;
  }
}
