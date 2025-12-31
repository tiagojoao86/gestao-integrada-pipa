package br.com.grupopipa.gestaointegrada.financeiro.enums;

/** Enum para status do título financeiro */
public enum StatusTitulo {
  ABERTO("Aberto"),
  PARCIAL("Parcialmente Pago"),
  PAGO("Pago"),
  CANCELADO("Cancelado"),
  VENCIDO("Vencido");

  private final String descricao;

  StatusTitulo(String descricao) {
    this.descricao = descricao;
  }

  public String getDescricao() {
    return descricao;
  }

  public boolean permiteMovimentacao() {
    return this == ABERTO || this == PARCIAL || this == VENCIDO;
  }
}
