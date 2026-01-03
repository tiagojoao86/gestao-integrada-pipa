package br.com.grupopipa.gestaointegrada.financeiro.enums;

/** Enum para tipos de conta bancária */
public enum TipoConta {
    CORRENTE("Conta Corrente"),
    POUPANCA("Poupança"),
    CAIXA("Caixa"),
    INVESTIMENTO("Investimento");

    private final String descricao;

    TipoConta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
