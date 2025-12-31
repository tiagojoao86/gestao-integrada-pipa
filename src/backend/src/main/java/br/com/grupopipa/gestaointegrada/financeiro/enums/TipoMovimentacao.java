package br.com.grupopipa.gestaointegrada.financeiro.enums;

/** Enum para tipos de movimentação financeira */
public enum TipoMovimentacao {
  PAGAMENTO("Pagamento"),
  RECEBIMENTO("Recebimento"),
  ESTORNO("Estorno"),
  TRANSFERENCIA("Transferência");

  private final String descricao;

  TipoMovimentacao(String descricao) {
    this.descricao = descricao;
  }

  public String getDescricao() {
    return descricao;
  }
}
