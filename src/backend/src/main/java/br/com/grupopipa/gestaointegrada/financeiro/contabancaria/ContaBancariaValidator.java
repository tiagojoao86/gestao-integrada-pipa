package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;

/**
 * Validador responsável por centralizar as regras de criação de ContaBancaria.
 * Chamado exclusivamente pelo {@code ContaBancaria.Builder}.
 */
public class ContaBancariaValidator {

    private ContaBancariaValidator() {
    }

    public static ValidatedData validate(
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

        if (tipo == TipoConta.CORRENTE || tipo == TipoConta.POUPANCA) {
            Validator.of(banco, "banco", violations).notBlank().maxLength(100);
            Validator.of(agencia, "agência", violations).notBlank().maxLength(10);
            Validator.of(numeroConta, "número da conta", violations).notBlank().maxLength(20);
        }

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

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code ContaBancaria.Builder}.
     */
    public static class ValidatedData {
        public final String nome;
        public final TipoConta tipo;
        public final String banco;
        public final String agencia;
        public final String numeroConta;
        public final Money saldoInicial;
        public final UnidadeNegocio unidadeNegocio;

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
}
