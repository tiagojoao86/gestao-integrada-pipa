package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

/**
 * Enum para tipo de pessoa (Física ou Jurídica)
 */
public enum TipoPessoa {
    FISICA("Pessoa Física"),
    JURIDICA("Pessoa Jurídica");

    private final String descricao;

    TipoPessoa(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
