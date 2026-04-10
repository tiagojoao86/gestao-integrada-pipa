package br.com.grupopipa.gestaointegrada.atendimento.convenio;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;

public class ConvenioValidator {

    private ConvenioValidator() {
    }

    public static ValidatedData validate(String nomeStr, Pessoa pessoa, String registroAns, Boolean ativo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        Validator.of(pessoa, "pessoa", violations).notNull();
        Validator.of(registroAns, "registroAns", violations).maxLength(20);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("convenio", violations);
        }

        return new ValidatedData(nome, pessoa, registroAns, ativo);
    }

    public static class ValidatedData {
        public final Nome nome;
        public final Pessoa pessoa;
        public final String registroAns;
        public final Boolean ativo;

        ValidatedData(Nome nome, Pessoa pessoa, String registroAns, Boolean ativo) {
            this.nome = nome;
            this.pessoa = pessoa;
            this.registroAns = registroAns;
            this.ativo = ativo;
        }
    }
}
