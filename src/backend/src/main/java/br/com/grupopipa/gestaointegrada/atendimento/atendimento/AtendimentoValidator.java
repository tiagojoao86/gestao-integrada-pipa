package br.com.grupopipa.gestaointegrada.atendimento.atendimento;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;

public class AtendimentoValidator {

    private AtendimentoValidator() {
    }

    public static ValidatedData validate(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Setor setor,
            Pessoa paciente,
            Pessoa responsavel,
            Convenio convenio,
            ConvenioCategoria convenioCategoria,
            Profissional profissionalAtendimento,
            Profissional profissionalResponsavel,
            String observacoes) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(dataInicio, "dataInicio", violations).notNull();
        Validator.of(dataFim, "dataFim", violations).notNull();
        Validator.of(setor, "setor", violations).notNull();
        Validator.of(paciente, "paciente", violations).notNull();
        Validator.of(profissionalAtendimento, "profissionalAtendimento", violations).notNull();
        Validator.of(profissionalResponsavel, "profissionalResponsavel", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("atendimento", violations);
        }

        return new ValidatedData(
            dataInicio, dataFim, setor, paciente, responsavel,
            convenio, convenioCategoria,
            profissionalAtendimento, profissionalResponsavel,
            observacoes);
    }

    public static class ValidatedData {
        public final LocalDateTime dataInicio;
        public final LocalDateTime dataFim;
        public final Setor setor;
        public final Pessoa paciente;
        public final Pessoa responsavel;
        public final Convenio convenio;
        public final ConvenioCategoria convenioCategoria;
        public final Profissional profissionalAtendimento;
        public final Profissional profissionalResponsavel;
        public final String observacoes;

        ValidatedData(
                LocalDateTime dataInicio,
                LocalDateTime dataFim,
                Setor setor,
                Pessoa paciente,
                Pessoa responsavel,
                Convenio convenio,
                ConvenioCategoria convenioCategoria,
                Profissional profissionalAtendimento,
                Profissional profissionalResponsavel,
                String observacoes) {
            this.dataInicio = dataInicio;
            this.dataFim = dataFim;
            this.setor = setor;
            this.paciente = paciente;
            this.responsavel = responsavel;
            this.convenio = convenio;
            this.convenioCategoria = convenioCategoria;
            this.profissionalAtendimento = profissionalAtendimento;
            this.profissionalResponsavel = profissionalResponsavel;
            this.observacoes = observacoes;
        }
    }
}
