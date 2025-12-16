export class PlanoContasGridDTO {
  id: string;
  codigo: string;
  descricao: string;
  tipo: string;
  planoPaiCodigo?: string;
  planoPaiDescricao?: string;
  unidadeNegocioCodigo?: string;
  ativo: boolean;
  analitico: boolean;

  constructor(
    id: string,
    codigo: string,
    descricao: string,
    tipo: string,
    planoPaiCodigo: string | undefined,
    planoPaiDescricao: string | undefined,
    unidadeNegocioCodigo: string | undefined,
    ativo: boolean,
    analitico: boolean
  ) {
    this.id = id;
    this.codigo = codigo;
    this.descricao = descricao;
    this.tipo = tipo;
    this.planoPaiCodigo = planoPaiCodigo;
    this.planoPaiDescricao = planoPaiDescricao;
    this.unidadeNegocioCodigo = unidadeNegocioCodigo;
    this.ativo = ativo;
    this.analitico = analitico;
  }
}
