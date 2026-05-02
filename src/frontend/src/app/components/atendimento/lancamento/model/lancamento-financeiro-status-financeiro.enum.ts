export enum LancamentoFinanceiroStatusFinanceiro {
  PENDENTE = 'PENDENTE',
  PAGO = 'PAGO',
  FATURADO = 'FATURADO',
}

export function lancamentoFinanceiroStatusFinanceiroLabel(status: string): string {
  switch (status) {
    case LancamentoFinanceiroStatusFinanceiro.PENDENTE:  return $localize`Pendente`;
    case LancamentoFinanceiroStatusFinanceiro.PAGO:      return $localize`Pago`;
    case LancamentoFinanceiroStatusFinanceiro.FATURADO:  return $localize`Faturado`;
    default: return status;
  }
}
