package br.com.grupopipa.gestaointegrada.atendimento.tabela;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.Tabela;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;

public class TabelaItemValidator {

    private TabelaItemValidator() {
    }

    public static ValidatedData validate(
            Tabela tabela,
            Procedimento procedimento,
            BigDecimal valorDecimal,
            LocalDate vigenciaInicio,
            LocalDate vigenciaFim) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(tabela, "tabela", violations).notNull();
        Validator.of(procedimento, "procedimento", violations).notNull();
        Validator.of(vigenciaInicio, "vigenciaInicio", violations).notNull();

        Money valor = null;
        if (valorDecimal == null) {
            violations.add(new BeanValidationMessage("valor", "O valor é obrigatório."));
        } else if (valorDecimal.compareTo(BigDecimal.ZERO) < 0) {
            violations.add(new BeanValidationMessage("valor", "O valor não pode ser negativo."));
        } else {
            valor = Money.of(valorDecimal);
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("tabelaItem", violations);
        }

        return new ValidatedData(tabela, procedimento, valor, vigenciaInicio, vigenciaFim);
    }

    public static class ValidatedData {
        public final Tabela tabela;
        public final Procedimento procedimento;
        public final Money valor;
        public final LocalDate vigenciaInicio;
        public final LocalDate vigenciaFim;

        ValidatedData(
                Tabela tabela,
                Procedimento procedimento,
                Money valor,
                LocalDate vigenciaInicio,
                LocalDate vigenciaFim) {
            this.tabela = tabela;
            this.procedimento = procedimento;
            this.valor = valor;
            this.vigenciaInicio = vigenciaInicio;
            this.vigenciaFim = vigenciaFim;
        }
    }
}
