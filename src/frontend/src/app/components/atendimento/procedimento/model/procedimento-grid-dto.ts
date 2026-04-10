export class ProcedimentoGridDTO {
  id: string;
  codigo: string;
  codigoTiss?: string;
  codigoTuss?: string;
  descricao: string;
  ativo?: boolean;
  deleted?: boolean;

  constructor(
    id: string,
    codigo: string,
    descricao: string,
    codigoTiss?: string,
    codigoTuss?: string,
    ativo?: boolean,
    deleted?: boolean
  ) {
    this.id = id;
    this.codigo = codigo;
    this.descricao = descricao;
    this.codigoTiss = codigoTiss;
    this.codigoTuss = codigoTuss;
    this.ativo = ativo;
    this.deleted = deleted;
  }
}
