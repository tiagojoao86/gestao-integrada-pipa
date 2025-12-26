export class SetorGridDTO {
  id: string;
  nome: string;
  descricao?: string;
  centroCustoNome: string;
  deleted?: boolean;

  constructor(
    id: string,
    nome: string,
    centroCustoNome: string,
    descricao?: string,
    deleted?: boolean
  ) {
    this.id = id;
    this.nome = nome;
    this.descricao = descricao;
    this.centroCustoNome = centroCustoNome;
    this.deleted = deleted;
  }
}
