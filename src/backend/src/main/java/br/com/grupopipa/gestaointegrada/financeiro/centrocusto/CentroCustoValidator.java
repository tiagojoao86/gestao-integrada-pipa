package br.com.grupopipa.gestaointegrada.financeiro.centrocusto;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;

/**
 * Validador responsável por centralizar as regras de criação de CentroCusto.
 * Chamado exclusivamente pelo {@code CentroCusto.Builder}.
 */
public class CentroCustoValidator {

    private CentroCustoValidator() {
    }

    public static ValidatedData validate(
            String nome,
            Boolean centroResultado,
            UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Nome nomeVO = ValidationUtils.validateAndGet(() -> Nome.of(nome), violations);
        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException(violations);
        }

        return new ValidatedData(nomeVO, centroResultado, unidadeNegocio);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code CentroCusto.Builder}.
     */
    public static class ValidatedData {
        public final Nome nome;
        public final Boolean centroResultado;
        public final UnidadeNegocio unidadeNegocio;

        ValidatedData(Nome nome, Boolean centroResultado, UnidadeNegocio unidadeNegocio) {
            this.nome = nome;
            this.centroResultado = centroResultado;
            this.unidadeNegocio = unidadeNegocio;
        }
    }
}
