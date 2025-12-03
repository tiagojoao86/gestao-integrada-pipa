package br.com.grupopipa.gestaointegrada.financeiro.enums;

/**
 * Enum para tipos de plano de contas
 */
public enum TipoPlanoContas {
    RECEITA("Receita"),
    DESPESA("Despesa"),
    ATIVO("Ativo"),
    PASSIVO("Passivo");
    
    private final String descricao;
    
    TipoPlanoContas(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
