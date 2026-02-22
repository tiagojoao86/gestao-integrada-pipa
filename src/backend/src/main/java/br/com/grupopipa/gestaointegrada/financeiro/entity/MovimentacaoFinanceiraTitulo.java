package br.com.grupopipa.gestaointegrada.financeiro.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Entidade de associação entre MovimentacaoFinanceira e Titulo com valor aplicado por título. */
@Entity
@Table(name = "movimentacao_financeira_titulo", uniqueConstraints = {
    @UniqueConstraint(name = "uk_movimentacao_financeira_titulo",
        columnNames = {"movimentacao_financeira_id", "titulo_id"})
})
public class MovimentacaoFinanceiraTitulo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimentacao_financeira_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_movimentacao_titulo_mov"))
    private MovimentacaoFinanceira movimentacaoFinanceira;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titulo_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_movimentacao_titulo_tit"))
    private Titulo titulo;

    @Embedded
    @AttributeOverride(name = "value",
        column = @Column(name = "valor", nullable = false, precision = 15, scale = 2))
    private Money valor;

    private MovimentacaoFinanceiraTitulo(
            MovimentacaoFinanceira movimentacaoFinanceira,
            Titulo titulo,
            Money valor) {
        this.movimentacaoFinanceira = movimentacaoFinanceira;
        this.titulo = titulo;
        this.valor = valor;
    }

    protected MovimentacaoFinanceiraTitulo() {
    }

    public MovimentacaoFinanceira getMovimentacaoFinanceira() {
        return movimentacaoFinanceira;
    }

    public Titulo getTitulo() {
        return titulo;
    }

    public Money getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovimentacaoFinanceiraTitulo)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MovimentacaoFinanceiraTitulo that = (MovimentacaoFinanceiraTitulo) o;
        return Objects.equals(movimentacaoFinanceira, that.movimentacaoFinanceira)
                && Objects.equals(titulo, that.titulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), movimentacaoFinanceira, titulo);
    }

    public static MovimentacaoFinanceiraTitulo create(
            MovimentacaoFinanceira movimentacaoFinanceira,
            Titulo titulo,
            Money valor) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        Validator.of(movimentacaoFinanceira, "movimentação financeira", violations).notNull();
        Validator.of(titulo, "título", violations).notNull();
        Validator.of(valor, "valor", violations).notNull();
        if (valor != null && !valor.isPositive()) {
            violations.add(new BeanValidationMessage(
                "validation.movimentacaoTitulo.valorPositivo",
                "Valor da movimentação por título deve ser positivo."));
        }
        if (!violations.isEmpty()) {
            throw new BeanValidationException("movimentacaoFinanceiraTitulo", violations);
        }
        return new MovimentacaoFinanceiraTitulo(movimentacaoFinanceira, titulo, valor);
    }
}
