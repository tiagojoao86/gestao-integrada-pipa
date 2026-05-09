export enum FormaPagamento {
  PIX = 'PIX',
  DINHEIRO = 'DINHEIRO',
  BOLETO = 'BOLETO',
  CARTAO_CREDITO = 'CARTAO_CREDITO',
  CARTAO_DEBITO = 'CARTAO_DEBITO',
  TED = 'TED',
  DOC = 'DOC',
  CHEQUE = 'CHEQUE',
  DEPOSITO = 'DEPOSITO',
}

export function formaPagamentoLabel(f: FormaPagamento | string): string {
  switch (f) {
    case FormaPagamento.PIX:            return 'PIX';
    case FormaPagamento.DINHEIRO:       return 'Dinheiro';
    case FormaPagamento.BOLETO:         return 'Boleto Bancário';
    case FormaPagamento.CARTAO_CREDITO: return 'Cartão de Crédito';
    case FormaPagamento.CARTAO_DEBITO:  return 'Cartão de Débito';
    case FormaPagamento.TED:            return 'TED';
    case FormaPagamento.DOC:            return 'DOC';
    case FormaPagamento.CHEQUE:         return 'Cheque';
    case FormaPagamento.DEPOSITO:       return 'Depósito Bancário';
    default: return f;
  }
}

export const FORMA_PAGAMENTO_OPTIONS = Object.values(FormaPagamento).map((key) => ({
  key,
  label: formaPagamentoLabel(key),
}));
