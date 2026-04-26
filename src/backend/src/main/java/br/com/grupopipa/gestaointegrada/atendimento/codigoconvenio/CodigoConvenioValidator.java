package br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;

@SuppressWarnings("checkstyle:MagicNumber")
public class CodigoConvenioValidator {

    private CodigoConvenioValidator() {
    }

    public static ValidatedData validate(Convenio convenio, Procedimento procedimento, String codigo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(convenio, "convenio", violations).notNull();
        Validator.of(procedimento, "procedimento", violations).notNull();
        Validator.of(codigo, "codigo", violations).notNull().maxLength(30);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("codigoConvenio", violations);
        }

        return new ValidatedData(convenio, procedimento, codigo);
    }

    public static class ValidatedData {
        public final Convenio convenio;
        public final Procedimento procedimento;
        public final String codigo;

        ValidatedData(Convenio convenio, Procedimento procedimento, String codigo) {
            this.convenio = convenio;
            this.procedimento = procedimento;
            this.codigo = codigo;
        }
    }
}
