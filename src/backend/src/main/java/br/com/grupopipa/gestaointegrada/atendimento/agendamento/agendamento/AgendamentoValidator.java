package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.entity.Agenda;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;

public class AgendamentoValidator {

    private AgendamentoValidator() {
    }

    public static ValidatedData validate(
            Agenda agenda, Pessoa paciente, Convenio convenio,
            Procedimento procedimento, String observacao) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (agenda == null) {
            violations.add(new BeanValidationMessage("agenda", "Agenda é obrigatória."));
        }
        if (paciente == null) {
            violations.add(new BeanValidationMessage("paciente", "Paciente é obrigatório."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("agendamento", violations);
        }

        return new ValidatedData(agenda, paciente, convenio, procedimento, observacao);
    }

    public static class ValidatedData {
        public final Agenda agenda;
        public final Pessoa paciente;
        public final Convenio convenio;
        public final Procedimento procedimento;
        public final String observacao;

        ValidatedData(Agenda agenda, Pessoa paciente, Convenio convenio,
                Procedimento procedimento, String observacao) {
            this.agenda = agenda;
            this.paciente = paciente;
            this.convenio = convenio;
            this.procedimento = procedimento;
            this.observacao = observacao;
        }
    }
}
