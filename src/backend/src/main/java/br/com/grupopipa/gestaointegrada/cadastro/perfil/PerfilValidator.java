package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;

/**
 * Validador responsável por centralizar as regras de criação de PerfilEntity.
 * Chamado exclusivamente pelo {@code PerfilEntity.Builder}.
 */
public class PerfilValidator {

    private PerfilValidator() {
    }

    public static ValidatedData validate(String nomeStr) {
        Set<BeanValidationMessage> violations = new HashSet<>();
        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("perfil", violations);
        }
        return new ValidatedData(nome);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code PerfilEntity.Builder}.
     */
    public static class ValidatedData {
        public final Nome nome;

        ValidatedData(Nome nome) {
            this.nome = nome;
        }
    }
}
