package br.com.grupopipa.gestaointegrada.atendimento.tabela;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;

public class TabelaValidator {

    private TabelaValidator() {
    }

    public static ValidatedData validate(String nomeStr, TipoTabela tipo, Boolean ativo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        Validator.of(tipo, "tipo", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tabela", violations);
        }

        return new ValidatedData(nome, tipo, ativo);
    }

    public static class ValidatedData {
        public final Nome nome;
        public final TipoTabela tipo;
        public final Boolean ativo;

        ValidatedData(Nome nome, TipoTabela tipo, Boolean ativo) {
            this.nome = nome;
            this.tipo = tipo;
            this.ativo = ativo;
        }
    }
}
