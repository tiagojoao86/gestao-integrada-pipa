package br.com.grupopipa.gestaointegrada.core.valueobject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value Object para representar valores monetários Garante precisão de 2 casas
 * decimais e
 * validações
 */
@Embeddable
public class Money implements Serializable {

    @Column(name = "valor", precision = 15, scale = 2)
    private BigDecimal value;

    protected Money() {
        this.value = BigDecimal.ZERO;
    }

    private Money(BigDecimal value) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (value == null) {
            violations.add(new BeanValidationMessage("valor", "Valor monetário não pode ser nulo"));
            throw new BeanValidationException(violations);
        }

        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }

    private Money(String value) {
        this(new BigDecimal(value));
    }

    private Money(double value) {
        this(BigDecimal.valueOf(value));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    public static Money of(String value) {
        return new Money(value);
    }

    public static Money of(double value) {
        return new Money(value);
    }

    /**
     * Cria um Money garantindo que o valor seja >= 0 Usado para valores que não
     * podem ser negativos:
     * desconto, juros, multa, valores originais
     */
    public static Money positiveOrZero(BigDecimal value) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (value == null) {
            violations.add(new BeanValidationMessage("valor", "Valor monetário não pode ser nulo"));
            throw new BeanValidationException(violations);
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            violations.add(new BeanValidationMessage("valor", "Valor não pode ser negativo"));
            throw new BeanValidationException(violations);
        }

        return new Money(value);
    }

    /**
     * Cria um Money garantindo que o valor seja > 0 Usado para valores que devem
     * ser estritamente
     * positivos
     */
    public static Money positive(BigDecimal value) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (value == null) {
            violations.add(new BeanValidationMessage("valor", "Valor monetário não pode ser nulo"));
            throw new BeanValidationException(violations);
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            violations.add(new BeanValidationMessage("valor", "Valor deve ser maior que zero"));
            throw new BeanValidationException(violations);
        }

        return new Money(value);
    }

    public Money add(Money other) {
        if (other == null) {
            return this;
        }
        return new Money(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        if (other == null) {
            return this;
        }
        return new Money(this.value.subtract(other.value));
    }

    public Money multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            return this;
        }
        return new Money(this.value.multiply(multiplier));
    }

    public Money divide(BigDecimal divisor) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            violations.add(new BeanValidationMessage("divisor", "Divisor não pode ser nulo ou zero"));
            throw new BeanValidationException(violations);
        }

        return new Money(this.value.divide(divisor, 2, RoundingMode.HALF_UP));
    }

    public boolean isNegative() {
        return value.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isGreaterThan(Money other) {
        if (other == null) {
            return true;
        }
        return value.compareTo(other.value) > 0;
    }

    public boolean isLessThan(Money other) {
        if (other == null) {
            return false;
        }
        return value.compareTo(other.value) < 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        if (other == null) {
            return true;
        }
        return value.compareTo(other.value) >= 0;
    }

    public boolean isLessThanOrEqual(Money other) {
        if (other == null) {
            return false;
        }
        return value.compareTo(other.value) <= 0;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money)) {
            return false;
        }
        Money money = (Money) o;
        return value.compareTo(money.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.format("R$ %,.2f", value);
    }
}
