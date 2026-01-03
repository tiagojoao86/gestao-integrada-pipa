package br.com.grupopipa.gestaointegrada.financeiro.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Entidade de associação entre Título e Setor com percentual de rateio */
@Entity
@Table(name = "titulo_setor", uniqueConstraints = {
    @UniqueConstraint(name = "uk_titulo_setor", columnNames = { "titulo_id", "setor_id" })
})
public class TituloSetor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titulo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_titulo_setor_titulo"))
    private Titulo titulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_titulo_setor_setor"))
    private Setor setor;

    @Column(name = "percentual_rateio", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualRateio;

    private TituloSetor(Titulo titulo, Setor setor, BigDecimal percentualRateio) {
        this.titulo = titulo;
        this.setor = setor;
        this.percentualRateio = percentualRateio;
    }

    protected TituloSetor() {
    }

    private static class ValidatedData {
        final Titulo titulo;
        final Setor setor;
        final BigDecimal percentualRateio;

        ValidatedData(Titulo titulo, Setor setor, BigDecimal percentualRateio) {
            this.titulo = titulo;
            this.setor = setor;
            this.percentualRateio = percentualRateio;
        }
    }

    private static ValidatedData validate(Titulo titulo, Setor setor, BigDecimal percentualRateio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (titulo == null) {
            violations.add(new BeanValidationMessage("titulo", "Título é obrigatório"));
        }

        if (setor == null) {
            violations.add(new BeanValidationMessage("setor", "Setor é obrigatório"));
        }

        if (percentualRateio == null) {
            violations.add(
                    new BeanValidationMessage("percentualRateio", "Percentual de rateio é obrigatório"));
        } else {
            if (percentualRateio.compareTo(BigDecimal.ZERO) <= 0) {
                violations.add(
                        new BeanValidationMessage("percentualRateio", "Percentual deve ser maior que zero"));
            }
            if (percentualRateio.compareTo(new BigDecimal("100")) > 0) {
                violations.add(
                        new BeanValidationMessage("percentualRateio", "Percentual não pode ser maior que 100"));
            }
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tituloSetor", violations);
        }

        return new ValidatedData(titulo, setor, percentualRateio);
    }

    public void atualizar(BigDecimal percentualRateio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (percentualRateio == null) {
            violations.add(
                    new BeanValidationMessage("percentualRateio", "Percentual de rateio é obrigatório"));
        } else {
            if (percentualRateio.compareTo(BigDecimal.ZERO) <= 0) {
                violations.add(
                        new BeanValidationMessage("percentualRateio", "Percentual deve ser maior que zero"));
            }
            if (percentualRateio.compareTo(new BigDecimal("100")) > 0) {
                violations.add(
                        new BeanValidationMessage("percentualRateio", "Percentual não pode ser maior que 100"));
            }
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tituloSetor", violations);
        }

        this.percentualRateio = percentualRateio;
    }

    // Getters
    public Titulo getTitulo() {
        return titulo;
    }

    public Setor getSetor() {
        return setor;
    }

    public BigDecimal getPercentualRateio() {
        return percentualRateio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TituloSetor)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TituloSetor that = (TituloSetor) o;
        return Objects.equals(titulo, that.titulo) && Objects.equals(setor, that.setor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), titulo, setor);
    }

    @Override
    public String toString() {
        return "TituloSetor{"
                + "setor="
                + (setor != null ? setor.getNome() : null)
                + ", percentual="
                + percentualRateio
                + '}';
    }

    public static class Builder {
        private Titulo titulo;
        private Setor setor;
        private BigDecimal percentualRateio;

        public Builder titulo(Titulo titulo) {
            this.titulo = titulo;
            return this;
        }

        public Builder setor(Setor setor) {
            this.setor = setor;
            return this;
        }

        public Builder percentualRateio(BigDecimal percentualRateio) {
            this.percentualRateio = percentualRateio;
            return this;
        }

        public TituloSetor build() {
            ValidatedData data = validate(this.titulo, this.setor, this.percentualRateio);
            return new TituloSetor(data.titulo, data.setor, data.percentualRateio);
        }
    }
}
