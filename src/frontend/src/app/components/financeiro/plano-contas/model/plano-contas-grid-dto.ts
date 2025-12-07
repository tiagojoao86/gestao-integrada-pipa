export interface PlanoContasGridDTO {
  id: string;
  codigo: string;
  descricao: string;
  tipo: string;
  planoPaiCodigo?: string;
  planoPaiDescricao?: string;
  ativo: boolean;
  analitico: boolean;
}
