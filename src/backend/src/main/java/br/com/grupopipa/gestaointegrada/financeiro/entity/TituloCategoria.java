package br.com.grupopipa.gestaointegrada.financeiro.entity;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "titulo_categoria")
public class TituloCategoria extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 100)
    private Nome nome;

    @Column(name = "descricao", length = 400)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TituloCategoriaTipoEnum tipo;

    private TituloCategoria(Nome nome, String descricao, TituloCategoriaTipoEnum tipo) {
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
    }

    protected TituloCategoria() {
    }

    private static class ValidatedData {
        final Nome nome;
        final String descricao;
        final TituloCategoriaTipoEnum tipo;

        ValidatedData(Nome nome, String descricao, TituloCategoriaTipoEnum tipo) {
            this.nome = nome;
            this.descricao = descricao;
            this.tipo = tipo;
        }
    }

    private static ValidatedData validate(String nomeStr, String descricaoStr, TituloCategoriaTipoEnum tipo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);

        if (descricaoStr != null && descricaoStr.length() > 400) {
            violations.add(new BeanValidationMessage("descricao", "Descrição deve ter no máximo 400 caracteres"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tituloCategoria", violations);
        }

        return new ValidatedData(nome, descricaoStr, tipo);
    }

    public void atualizar(String nome, String descricao, TituloCategoriaTipoEnum tipo) {
        ValidatedData data = validate(nome, descricao, tipo);
        this.nome = data.nome;
        this.descricao = data.descricao;
        this.tipo = data.tipo;
    }

    // Getters
    public Nome getNome() {
        if (Objects.isNull(this.nome)) {
            nome = Nome.of("");
        }

        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public TituloCategoriaTipoEnum getTipo() {
        return tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TituloCategoria))
            return false;
        if (!super.equals(o))
            return false;
        TituloCategoria that = (TituloCategoria) o;
        return Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nome);
    }

    public static class Builder {
        private String nome;
        private String descricao;
        private TituloCategoriaTipoEnum tipo;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder tipo(TituloCategoriaTipoEnum tipo) {
            this.tipo = tipo;
            return this;
        }

        public TituloCategoria build() {
            ValidatedData data = validate(this.nome, this.descricao, this.tipo);
            return new TituloCategoria(data.nome, data.descricao, this.tipo);
        }
    }
}
