package br.com.grupopipa.gestaointegrada.financeiro.condicaopagamento;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;

/**
 * Validador responsável por centralizar as regras de criação de CondicaoPagamento.
 * Chamado exclusivamente pelo {@code CondicaoPagamento.Builder}.
 */
public class CondicaoPagamentoValidator {

    private static final Pattern PATTERN_NX = Pattern.compile("^(\\d+)x$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_DIAS = Pattern.compile("^\\d+(/\\d+)*$");

    private CondicaoPagamentoValidator() {
    }

    public static ValidatedData validate(
            String condicao,
            String descricaoStr,
            Boolean ativo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(descricaoStr, "descrição", violations).maxLength(400);

        if (condicao == null || condicao.isBlank()) {
            Validator.of(condicao, "condição de pagamento", violations).notBlank();
        } else {
            validateCondicaoFormat(condicao.trim(), violations);
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("condicaoPagamento", violations);
        }

        return new ValidatedData(
                condicao != null ? condicao.trim() : null,
                descricaoStr,
                ativo != null ? ativo : Boolean.TRUE);
    }

    private static void validateCondicaoFormat(String condicao, Set<BeanValidationMessage> violations) {
        var matcherNx = PATTERN_NX.matcher(condicao);
        if (matcherNx.matches()) {
            int n = Integer.parseInt(matcherNx.group(1));
            if (n <= 0) {
                violations.add(new BeanValidationMessage(
                        "validation.condicaoPagamento.parcelasZero",
                        "Número de parcelas deve ser maior que zero."));
            }
            return;
        }

        var matcherDias = PATTERN_DIAS.matcher(condicao);
        if (matcherDias.matches()) {
            String[] parts = condicao.split("/");
            int anterior = 0;
            for (String part : parts) {
                int dias = Integer.parseInt(part);
                if (dias <= 0) {
                    violations.add(new BeanValidationMessage(
                            "validation.condicaoPagamento.diasZero",
                            "Dias de vencimento devem ser maiores que zero."));
                    return;
                }
                if (dias <= anterior) {
                    violations.add(new BeanValidationMessage(
                            "validation.condicaoPagamento.diasOrdem",
                            "Dias de vencimento devem estar em ordem crescente."));
                    return;
                }
                anterior = dias;
            }
            return;
        }

        violations.add(new BeanValidationMessage(
                "validation.condicaoPagamento.formatoInvalido",
                "Formato de condição inválido. Use 'Nx' (ex: 3x) ou 'dias/dias/dias' (ex: 10/20/40)."));
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code CondicaoPagamento.Builder}.
     */
    public static class ValidatedData {
        public final String condicao;
        public final String descricao;
        public final Boolean ativo;

        ValidatedData(
                String condicao,
                String descricao,
                Boolean ativo) {
            this.condicao = condicao;
            this.descricao = descricao;
            this.ativo = ativo;
        }
    }
}
