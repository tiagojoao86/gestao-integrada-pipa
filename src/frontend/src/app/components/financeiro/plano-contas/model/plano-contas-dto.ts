export class PlanoContasDTO {
  id?: string;
  codigo: string;
  descricao: string;
  tipo: string; // RECEITA, DESPESA, ATIVO, PASSIVO
  planoPaiId?: string;
  planoPaiDescricao?: string;
  unidadeNegocioId?: string;
  unidadeNegocioNome?: string;
  ativo?: boolean;
  analitico?: boolean;
  nivel?: number;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;

  constructor(
    id?: string,
    codigo = '',
    descricao = '',
    tipo = '',
    planoPaiId?: string,
    planoPaiDescricao?: string,
    unidadeNegocioId?: string,
    unidadeNegocioNome?: string,
    ativo?: boolean,
    analitico?: boolean,
    nivel?: number,
    createdAt?: Date,
    updatedAt?: Date,
    createdBy?: string,
    updatedBy?: string
  ) {
    this.id = id;
    this.codigo = codigo;
    this.descricao = descricao;
    this.tipo = tipo;
    this.planoPaiId = planoPaiId;
    this.planoPaiDescricao = planoPaiDescricao;
    this.unidadeNegocioId = unidadeNegocioId;
    this.unidadeNegocioNome = unidadeNegocioNome;
    this.ativo = ativo;
    this.analitico = analitico;
    this.nivel = nivel;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
  }
}
