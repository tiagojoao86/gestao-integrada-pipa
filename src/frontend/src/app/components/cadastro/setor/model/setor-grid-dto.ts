export class SetorGridDTO {
  id: string;
  nome: string;
  descricao?: string;
  centroCustoNome: string;

  constructor(
    id: string,
    nome: string,
    centroCustoNome: string,
    descricao?: string
  ) {
    this.id = id;
    this.nome = nome;
    this.descricao = descricao;
    this.centroCustoNome = centroCustoNome;
  }
}
