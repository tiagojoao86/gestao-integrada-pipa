package br.com.grupopipa.gestaointegrada.financeiro.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.entity.UnidadeNegocioFiltravel;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidade para MovimentacaoFinanceira - representa o dinheiro real no
 * caixa/banco (regime de caixa)
 */
@Entity
@Table(name = "movimentacao_financeira", indexes = {
    @Index(name = "idx_movimentacao_data", columnList = "data"),
    @Index(name = "idx_movimentacao_conta", columnList = "conta_bancaria_id")
})
public class MovimentacaoFinanceira extends BaseEntity implements UnidadeNegocioFiltravel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_negocio_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_movimentacao_unidade_negocio"))
    private UnidadeNegocio unidadeNegocio;

    @OneToMany(mappedBy = "movimentacaoFinanceira",
        cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<MovimentacaoFinanceiraTitulo> titulosAssociados = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movimentacao_conta"))
    private ContaBancaria contaBancaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoMovimentacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "valor", nullable = false, precision = 15, scale = 2))
    private Money valor;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    private MovimentacaoFinanceira(
            Set<Titulo> titulos,
            ContaBancaria contaBancaria,
            TipoMovimentacao tipo,
            FormaPagamento formaPagamento,
            Money valor,
            LocalDate data,
            UnidadeNegocio unidadeNegocio) {
        this.titulosAssociados = new HashSet<>();
        this.contaBancaria = contaBancaria;
        this.tipo = tipo;
        this.formaPagamento = formaPagamento;
        this.valor = valor;
        this.data = data;
        this.unidadeNegocio = unidadeNegocio;
        // Para cada título, registrar pagamento com o saldo exato do título
        if (titulos != null) {
            titulos.forEach(t -> {
                Money saldo = t.calcularSaldo();
                MovimentacaoFinanceiraTitulo mt = MovimentacaoFinanceiraTitulo.create(this, t, saldo);
                this.titulosAssociados.add(mt);
                t.getMovimentacoes().add(mt);
                t.registrarPagamento(saldo);
            });
        }
    }

    protected MovimentacaoFinanceira() {
    }

    private static class ValidatedData {
        final Set<Titulo> titulos;
        final ContaBancaria contaBancaria;
        final TipoMovimentacao tipo;
        final FormaPagamento formaPagamento;
        final Money valor;
        final LocalDate data;
        final UnidadeNegocio unidadeNegocio;

        ValidatedData(
                Set<Titulo> titulos,
                ContaBancaria contaBancaria,
                TipoMovimentacao tipo,
                FormaPagamento formaPagamento,
                Money valor,
                LocalDate data,
                UnidadeNegocio unidadeNegocio) {
            this.titulos = titulos;
            this.contaBancaria = contaBancaria;
            this.tipo = tipo;
            this.formaPagamento = formaPagamento;
            this.valor = valor;
            this.data = data;
            this.unidadeNegocio = unidadeNegocio;
        }
    }

    private static ValidatedData validate(
            Set<Titulo> titulos,
            ContaBancaria contaBancaria,
            TipoMovimentacao tipo,
            FormaPagamento formaPagamento,
            BigDecimal valor,
            LocalDate data,
            UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (titulos == null || titulos.isEmpty()) {
            violations.add(new BeanValidationMessage(
                    "validation.movimentacao.titulosObrigatorio",
                    "Pelo menos um título é obrigatório."));
        }
        Validator.of(contaBancaria, "conta bancária", violations).notNull();
        Validator.of(tipo, "tipo de movimentação", violations).notNull();
        Validator.of(formaPagamento, "forma de pagamento", violations).notNull();
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            violations.add(new BeanValidationMessage(
                    "validation.movimentacao.valorPositivo",
                    "Valor deve ser maior que zero."));
        }
        Validator.of(data, "data", violations).notNull();
        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();

        Money money = ValidationUtils.validateAndGet(() -> Money.of(valor), violations);

        // Validar regras de negócio para cada título
        if (titulos != null && !titulos.isEmpty() && money != null) {
            // Validar que o valor total da movimentação é igual à soma dos saldos dos títulos
            Money somaSaldos = titulos.stream()
                    .map(Titulo::calcularSaldo)
                    .reduce(Money.zero(), Money::add);

            if (!money.equals(somaSaldos)) {
                violations.add(new BeanValidationMessage(
                        "validation.movimentacao.valorDivergente",
                        "Valor da movimentação (" + money + ") deve ser igual à soma dos saldos "
                                + "dos títulos selecionados (" + somaSaldos + ")."));
            }

            for (Titulo titulo : titulos) {
                if (!titulo.getStatus().permiteMovimentacao()) {
                    violations.add(new BeanValidationMessage(
                            "validation.movimentacao.statusNaoPermite",
                            "Não é possível criar movimentação para título "
                                    + titulo.getStatus().getDescricao() + "."));
                }

                if (titulo.isOrigemParcelamento()) {
                    violations.add(new BeanValidationMessage(
                            "validation.movimentacao.origemParcelamento",
                            "Não é possível criar movimentação para título origem de parcelamento. "
                                    + "Utilize as parcelas."));
                }

                if (!titulo.calcularSaldo().isPositive()) {
                    violations.add(new BeanValidationMessage(
                            "validation.movimentacao.tituloSemSaldo",
                            "Título '" + titulo.getDescricao() + "' não possui saldo para pagamento."));
                }
            }
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("movimentacaoFinanceira", violations);
        }

        return new ValidatedData(
                titulos, contaBancaria, tipo, formaPagamento, money, data, unidadeNegocio);
    }

    @Override
    public UnidadeNegocio getUnidadeNegocio() {
        return unidadeNegocio;
    }

    public void setUnidadeNegocio(UnidadeNegocio unidadeNegocio) {
        this.unidadeNegocio = unidadeNegocio;
    }

    public void adicionarObservacao(String observacao) {
        if (this.observacoes == null) {
            this.observacoes = observacao;
        } else {
            this.observacoes += "\n" + observacao;
        }
    }

    public boolean isPagamento() {
        return tipo == TipoMovimentacao.PAGAMENTO;
    }

    public boolean isRecebimento() {
        return tipo == TipoMovimentacao.RECEBIMENTO;
    }

    public boolean isEstorno() {
        return tipo == TipoMovimentacao.ESTORNO;
    }

    // Getters

    /** Retorna os títulos associados com seus valores de pagamento */
    public Set<MovimentacaoFinanceiraTitulo> getTitulosAssociados() {
        return titulosAssociados;
    }

    /** Retorna apenas os títulos (sem o valor por título) */
    public Set<Titulo> getTitulos() {
        if (titulosAssociados == null) {
            return new HashSet<>();
        }
        return titulosAssociados.stream()
                .map(MovimentacaoFinanceiraTitulo::getTitulo)
                .collect(Collectors.toSet());
    }

    public ContaBancaria getContaBancaria() {
        return contaBancaria;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public Money getValor() {
        return valor;
    }

    public LocalDate getData() {
        return data;
    }

    public String getObservacoes() {
        return observacoes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovimentacaoFinanceira)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MovimentacaoFinanceira that = (MovimentacaoFinanceira) o;
        return Objects.equals(data, that.data)
                && Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), data, valor);
    }

    public static class Builder {
        private Set<Titulo> titulos = new HashSet<>();
        private ContaBancaria contaBancaria;
        private TipoMovimentacao tipo;
        private FormaPagamento formaPagamento;
        private Money valor;
        private LocalDate data;
        private UnidadeNegocio unidadeNegocio;

        public Builder titulos(Set<Titulo> titulos) {
            this.titulos = titulos;
            return this;
        }

        public Builder addTitulo(Titulo titulo) {
            this.titulos.add(titulo);
            return this;
        }

        public Builder contaBancaria(ContaBancaria contaBancaria) {
            this.contaBancaria = contaBancaria;
            return this;
        }

        public Builder tipo(TipoMovimentacao tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder formaPagamento(FormaPagamento formaPagamento) {
            this.formaPagamento = formaPagamento;
            return this;
        }

        public Builder valor(Money valor) {
            this.valor = valor;
            return this;
        }

        public Builder data(LocalDate data) {
            this.data = data;
            return this;
        }

        public Builder unidadeNegocio(UnidadeNegocio unidadeNegocio) {
            this.unidadeNegocio = unidadeNegocio;
            return this;
        }

        public MovimentacaoFinanceira build() {
            // Inferir unidadeNegocio a partir da contaBancaria se não fornecida
            // explicitamente
            if (this.unidadeNegocio == null && this.contaBancaria != null) {
                this.unidadeNegocio = this.contaBancaria.getUnidadeNegocio();
            }

            BigDecimal valorValue = (this.valor != null) ? this.valor.getValue() : null;
            ValidatedData data = validate(
                    this.titulos,
                    this.contaBancaria,
                    this.tipo,
                    this.formaPagamento,
                    valorValue,
                    this.data,
                    this.unidadeNegocio);
            return new MovimentacaoFinanceira(
                    data.titulos,
                    data.contaBancaria,
                    data.tipo,
                    data.formaPagamento,
                    data.valor,
                    data.data,
                    data.unidadeNegocio);
        }
    }
}
