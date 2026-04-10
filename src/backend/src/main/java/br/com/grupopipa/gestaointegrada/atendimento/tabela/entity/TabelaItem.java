package br.com.grupopipa.gestaointegrada.atendimento.tabela.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
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

@Entity
@Table(name = "tabela_item")
public class TabelaItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "tabela_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_tabela_item_tabela")
    )
    private Tabela tabela;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "procedimento_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_tabela_item_procedimento")
    )
    private Procedimento procedimento;

    @Embedded
    @AttributeOverride(name = "value",
        column = @Column(name = "valor", nullable = false, precision = 12, scale = 2))
    private Money valor;

    @Column(name = "vigencia_inicio", nullable = false)
    private LocalDate vigenciaInicio;

    @Column(name = "vigencia_fim")
    private LocalDate vigenciaFim;

    private TabelaItem(ValidatedData data) {
        this.tabela = data.tabela;
        this.procedimento = data.procedimento;
        this.valor = data.valor;
        this.vigenciaInicio = data.vigenciaInicio;
        this.vigenciaFim = data.vigenciaFim;
    }

    protected TabelaItem() {
    }

    // =========================================================================
    // Validation
    // =========================================================================

    private static class ValidatedData {
        final Tabela tabela;
        final Procedimento procedimento;
        final Money valor;
        final LocalDate vigenciaInicio;
        final LocalDate vigenciaFim;

        ValidatedData(
            Tabela tabela,
            Procedimento procedimento,
            Money valor,
            LocalDate vigenciaInicio,
            LocalDate vigenciaFim
        ) {
            this.tabela = tabela;
            this.procedimento = procedimento;
            this.valor = valor;
            this.vigenciaInicio = vigenciaInicio;
            this.vigenciaFim = vigenciaFim;
        }
    }

    private static ValidatedData validate(
        Tabela tabela,
        Procedimento procedimento,
        BigDecimal valorDecimal,
        LocalDate vigenciaInicio,
        LocalDate vigenciaFim
    ) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(tabela, "tabela", violations).notNull();
        Validator.of(procedimento, "procedimento", violations).notNull();
        Validator.of(vigenciaInicio, "vigenciaInicio", violations).notNull();

        Money valor = null;
        if (valorDecimal == null) {
            violations.add(new BeanValidationMessage("valor", "O valor é obrigatório."));
        } else if (valorDecimal.compareTo(BigDecimal.ZERO) < 0) {
            violations.add(new BeanValidationMessage("valor", "O valor não pode ser negativo."));
        } else {
            valor = Money.of(valorDecimal);
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tabelaItem", violations);
        }
        return new ValidatedData(tabela, procedimento, valor, vigenciaInicio, vigenciaFim);
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private Tabela tabela;
        private Procedimento procedimento;
        private BigDecimal valor;
        private LocalDate vigenciaInicio;
        private LocalDate vigenciaFim;

        public Builder tabela(Tabela tabela) {
            this.tabela = tabela;
            return this;
        }

        public Builder procedimento(Procedimento procedimento) {
            this.procedimento = procedimento;
            return this;
        }

        public Builder valor(BigDecimal valor) {
            this.valor = valor;
            return this;
        }

        public Builder vigenciaInicio(LocalDate vigenciaInicio) {
            this.vigenciaInicio = vigenciaInicio;
            return this;
        }

        public Builder vigenciaFim(LocalDate vigenciaFim) {
            this.vigenciaFim = vigenciaFim;
            return this;
        }

        public TabelaItem build() {
            ValidatedData data = validate(tabela, procedimento, valor, vigenciaInicio, vigenciaFim);
            return new TabelaItem(data);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(BigDecimal valorDecimal, LocalDate vigenciaInicio, LocalDate vigenciaFim) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        Money novoValor = null;
        if (valorDecimal == null) {
            violations.add(new BeanValidationMessage("valor", "O valor é obrigatório."));
        } else if (valorDecimal.compareTo(BigDecimal.ZERO) < 0) {
            violations.add(new BeanValidationMessage("valor", "O valor não pode ser negativo."));
        } else {
            novoValor = Money.of(valorDecimal);
        }
        Validator.of(vigenciaInicio, "vigenciaInicio", violations).notNull();
        if (!violations.isEmpty()) {
            throw new BeanValidationException("tabelaItem", violations);
        }
        this.valor = novoValor;
        this.vigenciaInicio = vigenciaInicio;
        this.vigenciaFim = vigenciaFim;
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public Tabela getTabela() {
        return tabela;
    }

    public Procedimento getProcedimento() {
        return procedimento;
    }

    public BigDecimal getValor() {
        return valor != null ? valor.getValue() : null;
    }

    public LocalDate getVigenciaInicio() {
        return vigenciaInicio;
    }

    public LocalDate getVigenciaFim() {
        return vigenciaFim;
    }
}
