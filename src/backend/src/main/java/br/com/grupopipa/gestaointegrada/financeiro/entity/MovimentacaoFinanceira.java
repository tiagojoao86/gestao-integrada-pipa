package br.com.grupopipa.gestaointegrada.financeiro.entity;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade para MovimentacaoFinanceira - representa o dinheiro real no
 * caixa/banco (regime de caixa)
 */
@Entity
@Table(name = "movimentacao_financeira", indexes = {
        @Index(name = "idx_movimentacao_data", columnList = "data"),
        @Index(name = "idx_movimentacao_titulo", columnList = "titulo_id"),
        @Index(name = "idx_movimentacao_conta", columnList = "conta_bancaria_id")
})
public class MovimentacaoFinanceira extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titulo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movimentacao_titulo"))
    private Titulo titulo;

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

    private MovimentacaoFinanceira(Titulo titulo, ContaBancaria contaBancaria, TipoMovimentacao tipo,
            FormaPagamento formaPagamento, Money valor, LocalDate data) {
        this.titulo = titulo;
        this.contaBancaria = contaBancaria;
        this.tipo = tipo;
        this.formaPagamento = formaPagamento;
        this.valor = valor;
        this.data = data;

        // Registra o pagamento no título
        titulo.registrarPagamento(valor);
    }

    protected MovimentacaoFinanceira() {
    }

    private static class ValidatedData {
        final Titulo titulo;
        final ContaBancaria contaBancaria;
        final TipoMovimentacao tipo;
        final FormaPagamento formaPagamento;
        final Money valor;
        final LocalDate data;

        ValidatedData(Titulo titulo, ContaBancaria contaBancaria, TipoMovimentacao tipo,
                FormaPagamento formaPagamento, Money valor, LocalDate data) {
            this.titulo = titulo;
            this.contaBancaria = contaBancaria;
            this.tipo = tipo;
            this.formaPagamento = formaPagamento;
            this.valor = valor;
            this.data = data;
        }
    }

    private static ValidatedData validate(Titulo titulo, ContaBancaria contaBancaria, TipoMovimentacao tipo,
            FormaPagamento formaPagamento, BigDecimal valor, LocalDate data) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (titulo == null) {
            violations.add(new BeanValidationMessage("titulo", "Título é obrigatório"));
        }
        if (contaBancaria == null) {
            violations.add(new BeanValidationMessage("contaBancaria", "Conta bancária é obrigatória"));
        }
        if (tipo == null) {
            violations.add(new BeanValidationMessage("tipo", "Tipo de movimentação é obrigatório"));
        }
        if (formaPagamento == null) {
            violations.add(new BeanValidationMessage("formaPagamento", "Forma de pagamento é obrigatória"));
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            violations.add(new BeanValidationMessage("valor", "Valor deve ser maior que zero"));
        }
        if (data == null) {
            violations.add(new BeanValidationMessage("data", "Data é obrigatória"));
        }

        Money money = ValidationUtils.validateAndGet(() -> Money.of(valor), violations);

        // Validar regras de negócio
        if (titulo != null && !titulo.getStatus().permiteMovimentacao()) {
            violations.add(new BeanValidationMessage("titulo.status",
                    "Não é possível criar movimentação para título " + titulo.getStatus().getDescricao()));
        }

        if (titulo != null && money != null) {
            Money saldoTitulo = titulo.calcularSaldo();
            if (money.isGreaterThan(saldoTitulo)) {
                violations.add(new BeanValidationMessage("valor",
                        "Valor da movimentação (" + money + ") excede o saldo do título (" + saldoTitulo + ")"));
            }
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("movimentacaoFinanceira", violations);
        }

        return new ValidatedData(titulo, contaBancaria, tipo, formaPagamento, money, data);
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
    public Titulo getTitulo() {
        return titulo;
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
        if (this == o)
            return true;
        if (!(o instanceof MovimentacaoFinanceira))
            return false;
        if (!super.equals(o))
            return false;
        MovimentacaoFinanceira that = (MovimentacaoFinanceira) o;
        return Objects.equals(titulo, that.titulo) &&
                Objects.equals(data, that.data) &&
                Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), titulo, data, valor);
    }

    public static class Builder {
        private Titulo titulo;
        private ContaBancaria contaBancaria;
        private TipoMovimentacao tipo;
        private FormaPagamento formaPagamento;
        private Money valor;
        private LocalDate data;

        public Builder titulo(Titulo titulo) {
            this.titulo = titulo;
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

        public MovimentacaoFinanceira build() {
            BigDecimal valorValue = (this.valor != null) ? this.valor.getValue() : null;
            ValidatedData data = validate(this.titulo, this.contaBancaria, this.tipo,
                    this.formaPagamento, valorValue, this.data);
            return new MovimentacaoFinanceira(data.titulo, data.contaBancaria, data.tipo,
                    data.formaPagamento, data.valor, data.data);
        }
    }
}
