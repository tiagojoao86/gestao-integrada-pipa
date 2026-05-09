package br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.entity;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.Tabela;
import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.TabelaRegraValidator;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tabela_regra")
public class TabelaRegra extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_tabela_regra_convenio"))
    private Convenio convenio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_categoria_id", nullable = true,
        foreignKey = @ForeignKey(name = "fk_tabela_regra_convenio_categoria"))
    private ConvenioCategoria convenioCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tabela_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_tabela_regra_tabela"))
    private Tabela tabela;

    private TabelaRegra(TabelaRegraValidator.ValidatedData data) {
        this.convenio = data.convenio;
        this.convenioCategoria = data.convenioCategoria;
        this.tabela = data.tabela;
    }

    protected TabelaRegra() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private Convenio convenio;
        private ConvenioCategoria convenioCategoria;
        private Tabela tabela;

        public Builder convenio(Convenio v) {
            this.convenio = v;
            return this;
        }

        public Builder convenioCategoria(ConvenioCategoria v) {
            this.convenioCategoria = v;
            return this;
        }

        public Builder tabela(Tabela v) {
            this.tabela = v;
            return this;
        }

        public TabelaRegra build() {
            return new TabelaRegra(TabelaRegraValidator.validate(convenio, convenioCategoria, tabela));
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(Convenio conv, ConvenioCategoria cat, Tabela tab) {
        TabelaRegraValidator.ValidatedData data = TabelaRegraValidator.validate(conv, cat, tab);
        this.convenio = data.convenio;
        this.convenioCategoria = data.convenioCategoria;
        this.tabela = data.tabela;
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public Convenio getConvenio() {
        return convenio;
    }

    public ConvenioCategoria getConvenioCategoria() {
        return convenioCategoria;
    }

    public Tabela getTabela() {
        return tabela;
    }
}
