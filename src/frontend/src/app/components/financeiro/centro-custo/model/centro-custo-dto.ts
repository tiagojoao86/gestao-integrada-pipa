export class CentroCustoDTO {
  id?: string;
  nome: string;
  centroResultado?: boolean;
  unidadeNegocioId: string;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;

  constructor(
    id: string | undefined,
    nome: string,
    centroResultado: boolean | undefined,
    unidadeNegocioId: string,
    createdAt: string | undefined,
    updatedAt: string | undefined,
    createdBy: string | undefined,
    updatedBy: string | undefined
  ) {
    this.id = id;
    this.nome = nome;
    this.centroResultado = centroResultado;
    this.unidadeNegocioId = unidadeNegocioId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.createdBy = createdBy;
    this.updatedBy = updatedBy;
  }
}
