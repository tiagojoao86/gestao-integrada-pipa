package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;

/**
 * Validador responsável por centralizar as regras de criação de TituloCategoria.
 * Chamado exclusivamente pelo {@code TituloCategoria.Builder}.
 */
public class TituloCategoriaValidator {

    private TituloCategoriaValidator() {
    }

    public static ValidatedData validate(
            String codigo,
            String nomeStr,
            String descricaoStr,
            TituloCategoriaTipoEnum tipo,
            TituloCategoria agrupador) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(codigo, "código", violations).notBlank();

        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);

        Validator.of(descricaoStr, "descrição", violations).maxLength(400);
        Validator.of(tipo, "tipo", violations).notNull();

        // Validação: se tem agrupador, o tipo deve ser o mesmo
        if (agrupador != null && tipo != null && agrupador.getTipo() != tipo) {
            violations.add(new BeanValidationMessage(
                    "validation.tituloCategoria.tipoAgrupadorDiferente",
                    "O tipo da categoria deve ser o mesmo do agrupador."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tituloCategoria", violations);
        }

        return new ValidatedData(
                codigo != null ? codigo.trim() : null, nome, descricaoStr, tipo, agrupador);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code TituloCategoria.Builder}.
     */
    public static class ValidatedData {
        public final String codigo;
        public final Nome nome;
        public final String descricao;
        public final TituloCategoriaTipoEnum tipo;
        public final TituloCategoria agrupador;

        ValidatedData(
                String codigo,
                Nome nome,
                String descricao,
                TituloCategoriaTipoEnum tipo,
                TituloCategoria agrupador) {
            this.codigo = codigo;
            this.nome = nome;
            this.descricao = descricao;
            this.tipo = tipo;
            this.agrupador = agrupador;
        }
    }
}
