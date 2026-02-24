package br.com.grupopipa.gestaointegrada.cadastro.setor.entity;

import java.util.Objects;

import br.com.grupopipa.gestaointegrada.cadastro.setor.SetorValidator;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CentroCusto;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Entidade para Setor da Empresa */
@Entity
@Table(name = "setor")
public class Setor extends BaseEntity {

    @Embedded
    private Nome nome;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_custo_id", nullable = false)
    private CentroCusto centroCusto;

    private Setor(Nome nome, String descricao, CentroCusto centroCusto) {
        this.nome = nome;
        this.descricao = descricao;
        this.centroCusto = centroCusto;
    }

    protected Setor() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private String nome;
        private String descricao;
        private CentroCusto centroCusto;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder centroCusto(CentroCusto centroCusto) {
            this.centroCusto = centroCusto;
            return this;
        }

        public Setor build() {
            SetorValidator.ValidatedData data = SetorValidator.validate(
                    this.nome, this.descricao, this.centroCusto);
            return new Setor(data.nome, data.descricao, data.centroCusto);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(String nomeStr, String descricao, CentroCusto centroCusto) {
        SetorValidator.ValidatedData data = SetorValidator.validate(nomeStr, descricao, centroCusto);
        this.nome = data.nome;
        this.descricao = data.descricao;
        this.centroCusto = data.centroCusto;
    }

    // Getters retornam String (não expõe VOs)
    public String getNome() {
        return nome != null ? nome.getValue() : null;
    }

    public String getDescricao() {
        return descricao;
    }

    public CentroCusto getCentroCusto() {
        return centroCusto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Setor)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Setor setor = (Setor) o;
        return Objects.equals(nome, setor.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nome);
    }

    @Override
    public String toString() {
        return getNome();
    }
}
