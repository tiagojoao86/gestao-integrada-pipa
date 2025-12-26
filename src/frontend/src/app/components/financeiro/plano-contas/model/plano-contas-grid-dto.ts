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
  deleted?: boolean;

  constructor(
    id: string,
    codigo: string,
    descricao: string,
    tipo: string,
    planoPaiCodigo: string | undefined,
    planoPaiDescricao: string | undefined,
    unidadeNegocioCodigo: string | undefined,
    ativo: boolean,
    analitico: boolean,
    deleted?: boolean
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
    this.deleted = deleted;
  }
}
