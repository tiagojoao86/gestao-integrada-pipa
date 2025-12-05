export interface UnidadeNegocioDTO {
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
}
