package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.entity.Agenda;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;

public class AgendaRegraValidator {

    private AgendaRegraValidator() {
    }

    public static ValidatedData validate(
            Agenda agenda,
            LocalDate dataInicio,
            LocalDate dataFim,
            LocalTime horaInicio,
            LocalTime horaFim,
            Integer duracaoSessaoMinutos) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(agenda, "agenda", violations).notNull();
        Validator.of(dataInicio, "dataInicio", violations).notNull();
        Validator.of(horaInicio, "horaInicio", violations).notNull();
        Validator.of(horaFim, "horaFim", violations).notNull();

        if (horaInicio != null && horaFim != null && !horaFim.isAfter(horaInicio)) {
            violations.add(new BeanValidationMessage(
                "horaFim", "Hora de fim deve ser posterior à hora de início."));
        }
        if (dataFim != null && dataInicio != null && dataFim.isBefore(dataInicio)) {
            violations.add(new BeanValidationMessage(
                "dataFim", "Data de fim deve ser posterior à data de início."));
        }
        if (duracaoSessaoMinutos == null || duracaoSessaoMinutos <= 0) {
            violations.add(new BeanValidationMessage(
                "duracaoSessaoMinutos", "Duração da sessão deve ser maior que zero."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("agendaRegra", violations);
        }

        return new ValidatedData(agenda, dataInicio, dataFim, horaInicio, horaFim, duracaoSessaoMinutos);
    }

    public static class ValidatedData {
        public final Agenda agenda;
        public final LocalDate dataInicio;
        public final LocalDate dataFim;
        public final LocalTime horaInicio;
        public final LocalTime horaFim;
        public final Integer duracaoSessaoMinutos;

        ValidatedData(
                Agenda agenda,
                LocalDate dataInicio,
                LocalDate dataFim,
                LocalTime horaInicio,
                LocalTime horaFim,
                Integer duracaoSessaoMinutos) {
            this.agenda = agenda;
            this.dataInicio = dataInicio;
            this.dataFim = dataFim;
            this.horaInicio = horaInicio;
            this.horaFim = horaFim;
            this.duracaoSessaoMinutos = duracaoSessaoMinutos;
        }
    }
}
