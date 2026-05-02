export enum LancamentoFinanceiroSituacao {
  ABERTO = 'ABERTO',
  FECHADO = 'FECHADO',
  CANCELADO = 'CANCELADO',
}

export function lancamentoFinanceiroSituacaoLabel(situacao: string): string {
  switch (situacao) {
    case LancamentoFinanceiroSituacao.ABERTO:    return $localize`Aberto`;
    case LancamentoFinanceiroSituacao.FECHADO:   return $localize`Fechado`;
    case LancamentoFinanceiroSituacao.CANCELADO: return $localize`Cancelado`;
    default: return situacao;
  }
}
