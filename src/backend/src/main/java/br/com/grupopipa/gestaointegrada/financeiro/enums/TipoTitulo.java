package br.com.grupopipa.gestaointegrada.financeiro.enums;

/**
 * Enum para tipos de título financeiro
 */
public enum TipoTitulo {
    A_PAGAR("A Pagar"),
    A_RECEBER("A Receber");
    
    private final String descricao;
    
    TipoTitulo(String descricao) {
        this.descricao = descricao;
    }
    
    public String getDescricao() {
        return descricao;
    }
}
