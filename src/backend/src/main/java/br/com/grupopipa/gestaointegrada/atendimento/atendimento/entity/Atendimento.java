package br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "atendimento")
public class Atendimento extends BaseEntity {

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_atendimento_setor"))
    private Setor setor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_atendimento_paciente"))
    private Pessoa paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id",
        foreignKey = @ForeignKey(name = "fk_atendimento_responsavel"))
    private Pessoa responsavel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_id",
        foreignKey = @ForeignKey(name = "fk_atendimento_convenio"))
    private Convenio convenio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "convenio_categoria_id",
        foreignKey = @ForeignKey(name = "fk_atendimento_convenio_categoria"))
    private ConvenioCategoria convenioCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_atendimento_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_atendimento_prof_atendimento"))
    private Profissional profissionalAtendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_responsavel_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_atendimento_prof_responsavel"))
    private Profissional profissionalResponsavel;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "atendimento", cascade = CascadeType.ALL, orphanRemoval = true,
        fetch = FetchType.LAZY)
    private List<AtendimentoProcedimento> procedimentos = new ArrayList<>();

    private Atendimento(ValidatedData data) {
        this.dataInicio = data.dataInicio;
        this.dataFim = data.dataFim;
        this.setor = data.setor;
        this.paciente = data.paciente;
        this.responsavel = data.responsavel;
        this.convenio = data.convenio;
        this.convenioCategoria = data.convenioCategoria;
        this.profissionalAtendimento = data.profissionalAtendimento;
        this.profissionalResponsavel = data.profissionalResponsavel;
        this.observacoes = data.observacoes;
    }

    protected Atendimento() {
    }

    // =========================================================================
    // Validation
    // =========================================================================

    private static class ValidatedData {
        final LocalDateTime dataInicio;
        final LocalDateTime dataFim;
        final Setor setor;
        final Pessoa paciente;
        final Pessoa responsavel;
        final Convenio convenio;
        final ConvenioCategoria convenioCategoria;
        final Profissional profissionalAtendimento;
        final Profissional profissionalResponsavel;
        final String observacoes;

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
            String observacoes
        ) {
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

    private static ValidatedData validate(
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Setor setor,
        Pessoa paciente,
        Pessoa responsavel,
        Convenio convenio,
        ConvenioCategoria convenioCategoria,
        Profissional profissionalAtendimento,
        Profissional profissionalResponsavel,
        String observacoes
    ) {
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
            observacoes
        );
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private LocalDateTime dataInicio;
        private LocalDateTime dataFim;
        private Setor setor;
        private Pessoa paciente;
        private Pessoa responsavel;
        private Convenio convenio;
        private ConvenioCategoria convenioCategoria;
        private Profissional profissionalAtendimento;
        private Profissional profissionalResponsavel;
        private String observacoes;

        public Builder dataInicio(LocalDateTime dataInicio) {
            this.dataInicio = dataInicio;
            return this;
        }

        public Builder dataFim(LocalDateTime dataFim) {
            this.dataFim = dataFim;
            return this;
        }

        public Builder setor(Setor setor) {
            this.setor = setor;
            return this;
        }

        public Builder paciente(Pessoa paciente) {
            this.paciente = paciente;
            return this;
        }

        public Builder responsavel(Pessoa responsavel) {
            this.responsavel = responsavel;
            return this;
        }

        public Builder convenio(Convenio convenio) {
            this.convenio = convenio;
            return this;
        }

        public Builder convenioCategoria(ConvenioCategoria cc) {
            this.convenioCategoria = cc;
            return this;
        }

        public Builder profissionalAtendimento(Profissional p) {
            this.profissionalAtendimento = p;
            return this;
        }

        public Builder profissionalResponsavel(Profissional p) {
            this.profissionalResponsavel = p;
            return this;
        }

        public Builder observacoes(String observacoes) {
            this.observacoes = observacoes;
            return this;
        }

        public Atendimento build() {
            ValidatedData data = validate(
                dataInicio, dataFim, setor, paciente, responsavel,
                convenio, convenioCategoria,
                profissionalAtendimento, profissionalResponsavel,
                observacoes
            );
            return new Atendimento(data);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        Setor setor,
        Pessoa paciente,
        Pessoa responsavel,
        Convenio convenio,
        ConvenioCategoria convenioCategoria,
        Profissional profissionalAtendimento,
        Profissional profissionalResponsavel,
        String observacoes
    ) {
        ValidatedData data = validate(
            dataInicio, dataFim, setor, paciente, responsavel,
            convenio, convenioCategoria,
            profissionalAtendimento, profissionalResponsavel,
            observacoes
        );
        this.dataInicio = data.dataInicio;
        this.dataFim = data.dataFim;
        this.setor = data.setor;
        this.paciente = data.paciente;
        this.responsavel = data.responsavel;
        this.convenio = data.convenio;
        this.convenioCategoria = data.convenioCategoria;
        this.profissionalAtendimento = data.profissionalAtendimento;
        this.profissionalResponsavel = data.profissionalResponsavel;
        this.observacoes = data.observacoes;
    }

    public void syncProcedimentos(List<AtendimentoProcedimento> novos) {
        this.procedimentos.clear();
        this.procedimentos.addAll(novos);
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }

    public Setor getSetor() {
        return setor;
    }

    public Pessoa getPaciente() {
        return paciente;
    }

    public Pessoa getResponsavel() {
        return responsavel;
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public ConvenioCategoria getConvenioCategoria() {
        return convenioCategoria;
    }

    public Profissional getProfissionalAtendimento() {
        return profissionalAtendimento;
    }

    public Profissional getProfissionalResponsavel() {
        return profissionalResponsavel;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public List<AtendimentoProcedimento> getProcedimentos() {
        return procedimentos;
    }
}
