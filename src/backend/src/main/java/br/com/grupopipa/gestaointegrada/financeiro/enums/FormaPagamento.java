package br.com.grupopipa.gestaointegrada.financeiro.enums;

/** Enum para formas de pagamento */
public enum FormaPagamento {
  PIX("PIX"),
  DINHEIRO("Dinheiro"),
  BOLETO("Boleto Bancário"),
  CARTAO_CREDITO("Cartão de Crédito"),
  CARTAO_DEBITO("Cartão de Débito"),
  TED("TED"),
  DOC("DOC"),
  CHEQUE("Cheque"),
  DEPOSITO("Depósito Bancário");

  private final String descricao;

  FormaPagamento(String descricao) {
    this.descricao = descricao;
  }

  public String getDescricao() {
    return descricao;
  }
}
