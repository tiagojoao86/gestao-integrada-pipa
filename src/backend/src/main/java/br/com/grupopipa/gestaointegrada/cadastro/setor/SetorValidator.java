package br.com.grupopipa.gestaointegrada.cadastro.setor;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CentroCusto;

/**
 * Validador responsável por centralizar as regras de criação de Setor.
 * Chamado exclusivamente pelo {@code Setor.Builder}.
 */
public class SetorValidator {

    private SetorValidator() {
    }

    public static ValidatedData validate(String nomeStr, String descricao, CentroCusto centroCusto) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        // IMPORTANTE: Use ValidationUtils.validateAndGet para Value Objects
        // Isso captura BeanValidationExceptions e adiciona ao set de violations
        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);

        Validator.of(descricao, "descrição", violations).maxLength(500);
        Validator.of(centroCusto, "centro de custo", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("setor", violations);
        }

        return new ValidatedData(nome, descricao, centroCusto);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code Setor.Builder}.
     */
    public static class ValidatedData {
        public final Nome nome;
        public final String descricao;
        public final CentroCusto centroCusto;

        ValidatedData(Nome nome, String descricao, CentroCusto centroCusto) {
            this.nome = nome;
            this.descricao = descricao;
            this.centroCusto = centroCusto;
        }
    }
}
