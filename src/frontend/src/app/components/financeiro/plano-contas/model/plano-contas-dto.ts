export interface PlanoContasDTO {
  id?: string;
  codigo: string;
  descricao: string;
  tipo: string; // RECEITA, DESPESA, ATIVO, PASSIVO
  planoPaiId?: string;
  planoPaiDescricao?: string;
  ativo?: boolean;
  analitico?: boolean;
  nivel?: number;
  createdAt?: Date;
  updatedAt?: Date;
  createdBy?: string;
  updatedBy?: string;
}
