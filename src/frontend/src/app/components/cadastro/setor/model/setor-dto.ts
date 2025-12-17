export class SetorDTO {
  id?: string;
  nome: string;
  descricao?: string;
  centroCustoId: string;
  centroCustoNome?: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;

  constructor(
    id: string | undefined,
    nome: string,
    centroCustoId: string,
    descricao?: string,
    centroCustoNome?: string,
    createdAt?: string,
    updatedAt?: string,
    createdBy?: string,
    updatedBy?: string
  ) {
    this.id = id;
    this.nome = nome;
    this.descricao = descricao;
    this.centroCustoId = centroCustoId;
    this.centroCustoNome = centroCustoNome;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
  }
}
