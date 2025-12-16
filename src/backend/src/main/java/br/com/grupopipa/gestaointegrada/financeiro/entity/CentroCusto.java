package br.com.grupopipa.gestaointegrada.financeiro.entity;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.entity.UnidadeNegocioFiltravel;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidade para Centro de Custo
 */
@Entity
@Table(name = "centro_custo", uniqueConstraints = {
        @UniqueConstraint(name = "uk_centro_custo_nome", columnNames = "nome")
})
public class CentroCusto extends BaseEntity implements UnidadeNegocioFiltravel {

    @Embedded
    private Nome nome;

    @Column(name = "centro_resultado")
    private Boolean centroResultado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_negocio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_centro_custo_unidade_negocio"))
    private UnidadeNegocio unidadeNegocio;

    protected CentroCusto() {
    }

    private CentroCusto(Nome nome, Boolean centroResultado, UnidadeNegocio unidadeNegocio) {
        this.nome = nome;
        this.centroResultado = centroResultado;
        this.unidadeNegocio = unidadeNegocio;
    }

    public void atualizar(String nome, Boolean centroResultado, UnidadeNegocio unidadeNegocio) {
        ValidatedData data = validate(nome, centroResultado, unidadeNegocio);
        this.nome = data.nome;
        this.centroResultado = data.centroResultado;
        this.unidadeNegocio = data.unidadeNegocio;
    }

    private static ValidatedData validate(String nome, Boolean centroResultado, UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (nome == null || nome.isBlank()) {
            violations.add(new BeanValidationMessage("nome", "centroCusto.nome.notBlank"));
        } else if (nome.length() > 200) {
            violations.add(new BeanValidationMessage("nome", "centroCusto.nome.maxLength"));
        }

        if (unidadeNegocio == null) {
            violations.add(new BeanValidationMessage("unidadeNegocio", "centroCusto.unidadeNegocio.required"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException(violations);
        }

        return new ValidatedData(Nome.of(nome.trim()), centroResultado, unidadeNegocio);
    }

    private static class ValidatedData {
        final Nome nome;
        final Boolean centroResultado;
        final UnidadeNegocio unidadeNegocio;

        ValidatedData(Nome nome, Boolean centroResultado, UnidadeNegocio unidadeNegocio) {
            this.nome = nome;
            this.centroResultado = centroResultado;
            this.unidadeNegocio = unidadeNegocio;
        }
    }

    // Getters

    public String getNome() {
        return nome != null ? nome.getValue() : null;
    }

    public Boolean getCentroResultado() {
        return centroResultado;
    }

    @Override
    public UnidadeNegocio getUnidadeNegocio() {
        return unidadeNegocio;
    }

    // Builder

    public static class Builder {
        private String nome;
        private Boolean centroResultado;
        private UnidadeNegocio unidadeNegocio;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder centroResultado(Boolean centroResultado) {
            this.centroResultado = centroResultado;
            return this;
        }

        public Builder unidadeNegocio(UnidadeNegocio unidadeNegocio) {
            this.unidadeNegocio = unidadeNegocio;
            return this;
        }

        public CentroCusto build() {
            ValidatedData data = validate(this.nome, this.centroResultado, this.unidadeNegocio);
            return new CentroCusto(data.nome, data.centroResultado, data.unidadeNegocio);
        }
    }
}
