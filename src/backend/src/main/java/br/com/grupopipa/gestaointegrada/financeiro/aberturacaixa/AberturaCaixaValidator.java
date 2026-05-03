package br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;

public final class AberturaCaixaValidator {

    private AberturaCaixaValidator() {
    }

    public static ValidatedData validate(BigDecimal valorAbertura) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (valorAbertura != null && valorAbertura.compareTo(BigDecimal.ZERO) < 0) {
            violations.add(new BeanValidationMessage(
                    "valorAbertura", "O valor de abertura não pode ser negativo."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("aberturaCaixa", violations);
        }

        return new ValidatedData(valorAbertura != null ? valorAbertura : BigDecimal.ZERO);
    }

    public static void validateFechar(BigDecimal valorConferencia) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (valorConferencia == null) {
            violations.add(new BeanValidationMessage(
                    "valorConferencia", "O valor de conferência é obrigatório."));
        } else if (valorConferencia.compareTo(BigDecimal.ZERO) < 0) {
            violations.add(new BeanValidationMessage(
                    "valorConferencia", "O valor de conferência não pode ser negativo."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("aberturaCaixa", violations);
        }
    }

    public static class ValidatedData {
        public final BigDecimal valorAbertura;

        ValidatedData(BigDecimal valorAbertura) {
            this.valorAbertura = valorAbertura;
        }
    }
}
