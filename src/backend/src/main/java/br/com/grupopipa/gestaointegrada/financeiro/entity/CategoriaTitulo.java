package br.com.grupopipa.gestaointegrada.financeiro.entity;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "categoria_titulo")
public class CategoriaTitulo extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", length = 400)
    private String descricao;

    private CategoriaTitulo(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    protected CategoriaTitulo() {
    }

    private static class ValidatedData {
        final String nome;
        final String descricao;

        ValidatedData(String nome, String descricao) {
            this.nome = nome;
            this.descricao = descricao;
        }
    }

    private static ValidatedData validate(String nome, String descricao) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (nome == null || nome.isBlank()) {
            violations.add(new BeanValidationMessage("nome", "Nome da categoria é obrigatório"));
        } else if (nome.length() > 100) {
            violations.add(new BeanValidationMessage("nome", "Nome deve ter no máximo 100 caracteres"));
        }

        if (descricao != null && descricao.length() > 400) {
            violations.add(new BeanValidationMessage("descricao", "Descrição deve ter no máximo 400 caracteres"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("categoriaTitulo", violations);
        }

        return new ValidatedData(nome, descricao);
    }

    public void atualizar(String nome, String descricao) {
        ValidatedData data = validate(nome, descricao);
        this.nome = data.nome;
        this.descricao = data.descricao;
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CategoriaTitulo))
            return false;
        if (!super.equals(o))
            return false;
        CategoriaTitulo that = (CategoriaTitulo) o;
        return Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nome);
    }

    public static class Builder {
        private String nome;
        private String descricao;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public CategoriaTitulo build() {
            ValidatedData data = validate(this.nome, this.descricao);
            return new CategoriaTitulo(data.nome, data.descricao);
        }
    }
}
