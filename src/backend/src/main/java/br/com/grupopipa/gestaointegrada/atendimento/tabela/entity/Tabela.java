package br.com.grupopipa.gestaointegrada.atendimento.tabela.entity;

import br.com.grupopipa.gestaointegrada.atendimento.tabela.TabelaValidator;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.TipoTabela;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "tabela")
public class Tabela extends BaseEntity {

    @Embedded
    private Nome nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoTabela tipo;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    private Tabela(TabelaValidator.ValidatedData data) {
        this.nome = data.nome;
        this.tipo = data.tipo;
        this.ativo = data.ativo != null ? data.ativo : true;
    }

    protected Tabela() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private String nome;
        private TipoTabela tipo;
        private Boolean ativo = true;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder tipo(TipoTabela tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public Tabela build() {
            return new Tabela(TabelaValidator.validate(nome, tipo, ativo));
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(String nomeStr, TipoTabela tipo, Boolean ativo) {
        TabelaValidator.ValidatedData data = TabelaValidator.validate(nomeStr, tipo, ativo);
        this.nome = data.nome;
        this.tipo = data.tipo;
        if (data.ativo != null) {
            this.ativo = data.ativo;
        }
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public String getNome() {
        return nome != null ? nome.getValue() : null;
    }

    public TipoTabela getTipo() {
        return tipo;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}
