package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda;

import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;

public class AgendaValidator {

    private AgendaValidator() {
    }

    public static ValidatedData validate(
            String nomeStr, Profissional profissional, Setor setor, Boolean ativo) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
        Validator.of(profissional, "profissional", violations).notNull();
        Validator.of(setor, "setor", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("agenda", violations);
        }

        return new ValidatedData(nome, profissional, setor, ativo);
    }

    public static class ValidatedData {
        public final Nome nome;
        public final Profissional profissional;
        public final Setor setor;
        public final Boolean ativo;

        ValidatedData(Nome nome, Profissional profissional, Setor setor, Boolean ativo) {
            this.nome = nome;
            this.profissional = profissional;
            this.setor = setor;
            this.ativo = ativo;
        }
    }
}
