export interface MovimentacaoCaixaDTO {
  id: string;
  valor: number;
  formaPagamento: string;
  formaPagamentoDescricao: string;
  dataHora: string;
  observacoes?: string;
  lancamentoId: string;
}
