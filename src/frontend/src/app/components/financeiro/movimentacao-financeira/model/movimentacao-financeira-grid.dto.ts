export interface MovimentacaoFinanceiraGridDTO {
  id: string;
  data: string;
  valor: number;
  tipo: string;
  unidadeNegocioId: string;
  unidadeNegocioNome: string;
  contaBancaria?: string;
  contaBancariaNome?: string;
}
