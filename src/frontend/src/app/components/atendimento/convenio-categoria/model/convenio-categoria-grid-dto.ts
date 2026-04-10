export class ConvenioCategoriaGridDTO {
  id: string;
  convenioNome?: string;
  nome: string;
  codigoAnsPlano?: string;
  ativo?: boolean;
  deleted?: boolean;

  constructor(
    id: string,
    nome: string,
    convenioNome?: string,
    codigoAnsPlano?: string,
    ativo?: boolean,
    deleted?: boolean
  ) {
    this.id = id;
    this.nome = nome;
    this.convenioNome = convenioNome;
    this.codigoAnsPlano = codigoAnsPlano;
    this.ativo = ativo;
    this.deleted = deleted;
  }
}
