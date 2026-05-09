export enum LancamentoFinanceiroStatusFinanceiro {
  PENDENTE = 'PENDENTE',
  PAGO = 'PAGO',
  PAGO_PARCIAL = 'PAGO_PARCIAL',
  FATURADO = 'FATURADO',
}

export function lancamentoFinanceiroStatusFinanceiroLabel(status: string): string {
  switch (status) {
    case LancamentoFinanceiroStatusFinanceiro.PENDENTE:     return $localize`Pendente`;
    case LancamentoFinanceiroStatusFinanceiro.PAGO:         return $localize`Pago`;
    case LancamentoFinanceiroStatusFinanceiro.PAGO_PARCIAL: return $localize`Pago Parcial`;
    case LancamentoFinanceiroStatusFinanceiro.FATURADO:     return $localize`Faturado`;
    default: return status;
  }
}
