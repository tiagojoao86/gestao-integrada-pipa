package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;

/**
 * Validador responsável por centralizar as regras de criação de PlanoContas.
 * Chamado exclusivamente pelo {@code PlanoContas.Builder}.
 */
public class PlanoContasValidator {

    private PlanoContasValidator() {
    }

    public static ValidatedData validate(
            String codigo,
            String descricao,
            TipoPlanoContas tipo,
            PlanoContas planoPai,
            UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(codigo, "código", violations).notBlank().maxLength(20);
        Validator.of(descricao, "descrição", violations).notBlank().maxLength(200);
        Validator.of(tipo, "tipo", violations).notNull();
        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();

        // Validar plano pai
        if (planoPai != null && tipo != null && !planoPai.getTipo().equals(tipo)) {
            violations.add(new BeanValidationMessage(
                    "validation.planoContas.tipoPaiDiferente",
                    "Plano pai deve ser do mesmo tipo: " + tipo));
        }
        // Nota: Validação de auto-referência não é possível aqui pois o objeto ainda
        // não foi criado
        // Esta validação deve ser feita em @PrePersist/@PreUpdate se necessário

        if (!violations.isEmpty()) {
            throw new BeanValidationException("planoContas", violations);
        }

        return new ValidatedData(codigo, descricao, tipo, planoPai, unidadeNegocio);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code PlanoContas.Builder}.
     */
    public static class ValidatedData {
        public final String codigo;
        public final String descricao;
        public final TipoPlanoContas tipo;
        public final PlanoContas planoPai;
        public final UnidadeNegocio unidadeNegocio;

        ValidatedData(
                String codigo,
                String descricao,
                TipoPlanoContas tipo,
                PlanoContas planoPai,
                UnidadeNegocio unidadeNegocio) {
            this.codigo = codigo;
            this.descricao = descricao;
            this.tipo = tipo;
            this.planoPai = planoPai;
            this.unidadeNegocio = unidadeNegocio;
        }
    }
}
