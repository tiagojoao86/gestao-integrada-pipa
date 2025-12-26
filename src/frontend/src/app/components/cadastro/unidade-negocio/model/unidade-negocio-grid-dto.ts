export class UnidadeNegocioGridDTO {
  id: string;
  codigo: string;
  nome: string;
  cnpj?: string;
  ativa: boolean;
  deleted?: boolean;

  constructor(
    id: string,
    codigo: string,
    nome: string,
    cnpj: string | undefined,
    ativa: boolean,
    deleted?: boolean
  ) {
    this.id = id;
    this.codigo = codigo;
    this.nome = nome;
    this.cnpj = cnpj;
    this.ativa = ativa;
    this.deleted = deleted;
  }
}
