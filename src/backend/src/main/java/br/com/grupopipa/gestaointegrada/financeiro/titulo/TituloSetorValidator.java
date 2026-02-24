package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;

/**
 * Validador responsável por centralizar as regras de criação de TituloSetor.
 * Chamado exclusivamente pelo {@code TituloSetor.Builder}.
 */
public class TituloSetorValidator {

    private TituloSetorValidator() {
    }

    public static ValidatedData validate(Titulo titulo, Setor setor, BigDecimal percentualRateio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(titulo, "título", violations).notNull();
        Validator.of(setor, "setor", violations).notNull();
        Validator.of(percentualRateio, "percentual de rateio", violations).notNull();

        if (percentualRateio != null) {
            if (percentualRateio.compareTo(BigDecimal.ZERO) <= 0) {
                violations.add(new BeanValidationMessage(
                        "validation.tituloSetor.percentualZero",
                        "Percentual de rateio deve ser maior que zero."));
            }
            if (percentualRateio.compareTo(new BigDecimal("100")) > 0) {
                violations.add(new BeanValidationMessage(
                        "validation.tituloSetor.percentualMaximo",
                        "Percentual de rateio não pode ser maior que 100."));
            }
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tituloSetor", violations);
        }

        return new ValidatedData(titulo, setor, percentualRateio);
    }

    /**
     * Dados validados retornados por {@code validate()}.
     * Campos públicos para acesso direto pelo {@code TituloSetor.Builder}.
     */
    public static class ValidatedData {
        public final Titulo titulo;
        public final Setor setor;
        public final BigDecimal percentualRateio;

        ValidatedData(Titulo titulo, Setor setor, BigDecimal percentualRateio) {
            this.titulo = titulo;
            this.setor = setor;
            this.percentualRateio = percentualRateio;
        }
    }
}
