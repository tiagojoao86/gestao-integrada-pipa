export class UnidadeNegocioDTO {
  id: string;
  codigo: string;
  nome: string;
  descricao?: string;
  cnpj?: string;
  ativa: boolean;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;

  constructor(
    id: string,
    codigo: string,
    nome: string,
    descricao: string | undefined,
    cnpj: string | undefined,
    ativa: boolean,
    createdAt?: Date,
    updatedAt?: Date,
    createdBy?: string,
    updatedBy?: string
  ) {
    this.id = id;
    this.codigo = codigo;
    this.nome = nome;
    this.descricao = descricao;
    this.cnpj = cnpj;
    this.ativa = ativa;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
  }
}
