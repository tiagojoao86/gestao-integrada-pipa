package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;

public class ConvenioCategoriaValidator {

    private ConvenioCategoriaValidator() {
    }

    public static ValidatedData validate(
            Convenio convenio, String nomeStr, String codigoAnsPlano, Boolean ativo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(convenio, "convenio", violations).notNull();
        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        Validator.of(codigoAnsPlano, "codigoAnsPlano", violations).maxLength(20);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("convenioCategoria", violations);
        }

        return new ValidatedData(convenio, nome, codigoAnsPlano, ativo);
    }

    public static class ValidatedData {
        public final Convenio convenio;
        public final Nome nome;
        public final String codigoAnsPlano;
        public final Boolean ativo;

        ValidatedData(Convenio convenio, Nome nome, String codigoAnsPlano, Boolean ativo) {
            this.convenio = convenio;
            this.nome = nome;
            this.codigoAnsPlano = codigoAnsPlano;
            this.ativo = ativo;
        }
    }
}
