export enum LancamentoFinanceiroStatus {
  PENDENTE = 'PENDENTE',
  FATURADO = 'FATURADO',
  CANCELADO = 'CANCELADO',
}

export function lancamentoFinanceiroStatusLabel(status: LancamentoFinanceiroStatus | string): string {
  switch (status) {
    case LancamentoFinanceiroStatus.PENDENTE:  return 'Pendente';
    case LancamentoFinanceiroStatus.FATURADO:  return 'Faturado';
    case LancamentoFinanceiroStatus.CANCELADO: return 'Cancelado';
    default: return status;
  }
}
