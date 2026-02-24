package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.CNPJ;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;

/**
 * Validador responsável por centralizar as regras de criação de UnidadeNegocio.
 * Chamado exclusivamente pelo {@code UnidadeNegocio.Builder}.
 */
public class UnidadeNegocioValidator {

    private UnidadeNegocioValidator() {
    }

    public static ValidatedData validate(
            String codigo, String nomeStr, String descricao, String cnpjStr) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(codigo, "código", violations).notBlank().maxLength(20);

        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        CNPJ cnpj = null;
        if (cnpjStr != null && !cnpjStr.isBlank()) {
            cnpj = ValidationUtils.validateAndGet(() -> new CNPJ(cnpjStr), violations);
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("unidadeNegocio", violations);
        }

        return new ValidatedData(codigo, nome, descricao, cnpj);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code UnidadeNegocio.Builder}.
     */
    public static class ValidatedData {
        public final String codigo;
        public final Nome nome;
        public final String descricao;
        public final CNPJ cnpj;

        ValidatedData(String codigo, Nome nome, String descricao, CNPJ cnpj) {
            this.codigo = codigo;
            this.nome = nome;
            this.descricao = descricao;
            this.cnpj = cnpj;
        }
    }
}
