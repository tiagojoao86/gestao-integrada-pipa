package br.com.grupopipa.gestaointegrada.financeiro.caixa;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;

public class CaixaValidator {

    private static final int NOME_MAX_LENGTH = 150;

    private CaixaValidator() {
    }

    public static ValidatedData validate(
            String nome,
            BigDecimal valorPadraoAbertura,
            BigDecimal percentualPagamentoParcial,
            BigDecimal valorMinimoParcela) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(nome, "nome", violations).notBlank().maxLength(NOME_MAX_LENGTH);
        Validator.of(valorPadraoAbertura, "valor padrão de abertura", violations).notNull();

        if (valorPadraoAbertura != null && valorPadraoAbertura.compareTo(BigDecimal.ZERO) < 0) {
            violations.add(new BeanValidationMessage(
                    "valorPadraoAbertura",
                    "Valor padrão de abertura deve ser maior ou igual a zero."));
        }

        if (percentualPagamentoParcial != null) {
            if (percentualPagamentoParcial.compareTo(BigDecimal.ZERO) < 0
                    || percentualPagamentoParcial.compareTo(BigDecimal.valueOf(100)) > 0) {
                violations.add(new BeanValidationMessage(
                        "percentualPagamentoParcial",
                        "Percentual de pagamento parcial deve estar entre 0 e 100."));
            }
        }

        if (valorMinimoParcela != null && valorMinimoParcela.compareTo(BigDecimal.ZERO) < 0) {
            violations.add(new BeanValidationMessage(
                    "valorMinimoParcela",
                    "Valor mínimo da parcela deve ser maior ou igual a zero."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("caixa", violations);
        }

        return new ValidatedData(nome, valorPadraoAbertura, percentualPagamentoParcial, valorMinimoParcela);
    }

    public static class ValidatedData {
        public final String nome;
        public final BigDecimal valorPadraoAbertura;
        public final BigDecimal percentualPagamentoParcial;
        public final BigDecimal valorMinimoParcela;

        ValidatedData(
                String nome,
                BigDecimal valorPadraoAbertura,
                BigDecimal percentualPagamentoParcial,
                BigDecimal valorMinimoParcela) {
            this.nome = nome;
            this.valorPadraoAbertura = valorPadraoAbertura;
            this.percentualPagamentoParcial = percentualPagamentoParcial;
            this.valorMinimoParcela = valorMinimoParcela;
        }
    }
}
