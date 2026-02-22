package br.com.grupopipa.gestaointegrada.financeiro.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.entity.UnidadeNegocioFiltravel;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Entidade para Conta Bancária */
@Entity
@Table(name = "conta_bancaria")
public class ContaBancaria extends BaseEntity implements UnidadeNegocioFiltravel {

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "banco", length = 100)
    private String banco;

    @Column(name = "agencia", length = 10)
    private String agencia;

    @Column(name = "numero_conta", length = 20)
    private String numeroConta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoConta tipo;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "saldo_inicial", precision = 15, scale = 2))
    private Money saldoInicial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_negocio_id", nullable = false)
    private UnidadeNegocio unidadeNegocio;

    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;

    private ContaBancaria(
            String nome,
            TipoConta tipo,
            String banco,
            String agencia,
            String numeroConta,
            Money saldoInicial,
            UnidadeNegocio unidadeNegocio) {
        this.nome = nome;
        this.tipo = tipo;
        this.banco = banco;
        this.agencia = agencia;
        this.numeroConta = numeroConta;
        this.saldoInicial = saldoInicial;
        this.unidadeNegocio = unidadeNegocio;
    }

    protected ContaBancaria() {
    }

    private static class ValidatedData {
        final String nome;
        final TipoConta tipo;
        final String banco;
        final String agencia;
        final String numeroConta;
        final Money saldoInicial;
        final UnidadeNegocio unidadeNegocio;

        ValidatedData(
                String nome,
                TipoConta tipo,
                String banco,
                String agencia,
                String numeroConta,
                Money saldoInicial,
                UnidadeNegocio unidadeNegocio) {
            this.nome = nome;
            this.tipo = tipo;
            this.banco = banco;
            this.agencia = agencia;
            this.numeroConta = numeroConta;
            this.saldoInicial = saldoInicial;
            this.unidadeNegocio = unidadeNegocio;
        }
    }

    private static ValidatedData validate(
            String nome,
            TipoConta tipo,
            String banco,
            String agencia,
            String numeroConta,
            BigDecimal saldoInicial,
            UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(nome, "nome da conta", violations).notBlank().maxLength(100);
        Validator.of(tipo, "tipo da conta", violations).notNull();
        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();

        // Validar e criar Money
        Money money = Money.zero();
        if (saldoInicial != null) {
            try {
                money = Money.of(saldoInicial);
            } catch (Exception e) {
                violations.add(new BeanValidationMessage("saldoInicial", "Saldo inicial inválido"));
            }
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("contaBancaria", violations);
        }

        return new ValidatedData(nome, tipo, banco, agencia, numeroConta, money, unidadeNegocio);
    }

    public void atualizar(String nome, String banco, String agencia, String numeroConta) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(nome, "nome da conta", violations).notBlank().maxLength(100);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("contaBancaria", violations);
        }

        this.nome = nome;
        this.banco = banco;
        this.agencia = agencia;
        this.numeroConta = numeroConta;
    }

    public void atualizarUnidadeNegocio(UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();
        if (!violations.isEmpty()) {
            throw new BeanValidationException("contaBancaria", violations);
        }
        this.unidadeNegocio = unidadeNegocio;
    }

    public void ativar() {
        this.ativa = true;
    }

    public void inativar() {
        this.ativa = false;
    }

    public boolean isCaixa() {
        return tipo == TipoConta.CAIXA;
    }

    public boolean isBancaria() {
        return tipo == TipoConta.CORRENTE || tipo == TipoConta.POUPANCA;
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public String getBanco() {
        return banco;
    }

    public String getAgencia() {
        return agencia;
    }

    public String getNumeroConta() {
        return numeroConta;
    }

    public TipoConta getTipo() {
        return tipo;
    }

    public Money getSaldoInicial() {
        return saldoInicial;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public boolean isAtiva() {
        return ativa != null && ativa;
    }

    public UnidadeNegocio getUnidadeNegocio() {
        return unidadeNegocio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContaBancaria)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ContaBancaria that = (ContaBancaria) o;
        return Objects.equals(nome, that.nome) && Objects.equals(numeroConta, that.numeroConta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nome, numeroConta);
    }

    @Override
    public String toString() {
        if (banco != null && numeroConta != null) {
            return nome + " - " + banco + " Ag: " + agencia + " Conta: " + numeroConta;
        }
        return nome;
    }

    public static class Builder {
        private String nome;
        private TipoConta tipo;
        private String banco;
        private String agencia;
        private String numeroConta;
        private Money saldoInicial;
        private UnidadeNegocio unidadeNegocio;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder tipo(TipoConta tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder banco(String banco) {
            this.banco = banco;
            return this;
        }

        public Builder agencia(String agencia) {
            this.agencia = agencia;
            return this;
        }

        public Builder numeroConta(String numeroConta) {
            this.numeroConta = numeroConta;
            return this;
        }

        public Builder saldoInicial(Money saldoInicial) {
            this.saldoInicial = saldoInicial;
            return this;
        }

        public Builder unidadeNegocio(UnidadeNegocio unidadeNegocio) {
            this.unidadeNegocio = unidadeNegocio;
            return this;
        }

        public ContaBancaria build() {
            BigDecimal saldoInicialValue = (this.saldoInicial != null) ? this.saldoInicial.getValue() : null;
            ValidatedData data = validate(
                    this.nome,
                    this.tipo,
                    this.banco,
                    this.agencia,
                    this.numeroConta,
                    saldoInicialValue,
                    this.unidadeNegocio);
            return new ContaBancaria(
                    data.nome,
                    data.tipo,
                    data.banco,
                    data.agencia,
                    data.numeroConta,
                    data.saldoInicial,
                    data.unidadeNegocio);
        }
    }
}
