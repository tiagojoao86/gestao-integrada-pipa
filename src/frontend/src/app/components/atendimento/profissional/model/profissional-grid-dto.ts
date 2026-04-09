export class ProfissionalGridDTO {
  id: string;
  pessoaNome: string;
  conselho: string;
  codigoConselho: string;
  tipoRemuneracao?: string;
  ativo?: boolean;
  deleted?: boolean;

  constructor(
    id: string,
    pessoaNome: string,
    conselho: string,
    codigoConselho: string,
    tipoRemuneracao?: string,
    ativo?: boolean,
    deleted?: boolean
  ) {
    this.id = id;
    this.pessoaNome = pessoaNome;
    this.conselho = conselho;
    this.codigoConselho = codigoConselho;
    this.tipoRemuneracao = tipoRemuneracao;
    this.ativo = ativo;
    this.deleted = deleted;
  }
}
