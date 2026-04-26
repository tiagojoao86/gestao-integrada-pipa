package br.com.grupopipa.gestaointegrada.atendimento.procedimento;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;

@SuppressWarnings("checkstyle:MagicNumber")
public class ProcedimentoValidator {

    private ProcedimentoValidator() {
    }

    public static ValidatedData validate(
            String codigo, String codigoTiss, String codigoTuss, String descricao, Boolean ativo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(codigo, "codigo", violations).notNull().maxLength(30);
        Validator.of(descricao, "descricao", violations).notNull().maxLength(200);
        Validator.of(codigoTiss, "codigoTiss", violations).maxLength(20);
        Validator.of(codigoTuss, "codigoTuss", violations).maxLength(20);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("procedimento", violations);
        }

        return new ValidatedData(codigo, codigoTiss, codigoTuss, descricao, ativo);
    }

    public static class ValidatedData {
        public final String codigo;
        public final String codigoTiss;
        public final String codigoTuss;
        public final String descricao;
        public final Boolean ativo;

        ValidatedData(String codigo, String codigoTiss, String codigoTuss, String descricao, Boolean ativo) {
            this.codigo = codigo;
            this.codigoTiss = codigoTiss;
            this.codigoTuss = codigoTuss;
            this.descricao = descricao;
            this.ativo = ativo;
        }
    }
}
