package br.com.grupopipa.gestaointegrada.atendimento.tabela.entity;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.tabela.TipoTabela;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
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

    private Tabela(ValidatedData data) {
        this.nome = data.nome;
        this.tipo = data.tipo;
        this.ativo = data.ativo != null ? data.ativo : true;
    }

    protected Tabela() {
    }

    // =========================================================================
    // Validation
    // =========================================================================

    private static class ValidatedData {
        final Nome nome;
        final TipoTabela tipo;
        final Boolean ativo;

        ValidatedData(Nome nome, TipoTabela tipo, Boolean ativo) {
            this.nome = nome;
            this.tipo = tipo;
            this.ativo = ativo;
        }
    }

    private static ValidatedData validate(String nomeStr, TipoTabela tipo, Boolean ativo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        Validator.of(tipo, "tipo", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tabela", violations);
        }
        return new ValidatedData(nome, tipo, ativo);
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
            ValidatedData data = validate(nome, tipo, ativo);
            return new Tabela(data);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(String nomeStr, TipoTabela tipo, Boolean ativo) {
        ValidatedData data = validate(nomeStr, tipo, ativo);
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
