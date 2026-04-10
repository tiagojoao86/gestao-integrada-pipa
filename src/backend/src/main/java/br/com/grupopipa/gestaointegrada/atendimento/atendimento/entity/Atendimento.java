package br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.atendimento.StatusAtendimento;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.TabelaItem;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "atendimento")
public class Atendimento extends BaseEntity {

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_atendimento_procedimento"))
    private Procedimento procedimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tabela_item_id",
        foreignKey = @ForeignKey(name = "fk_atendimento_tabela_item"))
    private TabelaItem tabelaItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private StatusAtendimento status;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    private Atendimento(ValidatedData data) {
        this.dataHora = data.dataHora;
        this.setor = data.setor;
        this.paciente = data.paciente;
        this.responsavel = data.responsavel;
        this.convenio = data.convenio;
        this.convenioCategoria = data.convenioCategoria;
        this.profissionalAtendimento = data.profissionalAtendimento;
        this.profissionalResponsavel = data.profissionalResponsavel;
        this.procedimento = data.procedimento;
        this.tabelaItem = data.tabelaItem;
        this.status = data.status;
        this.observacoes = data.observacoes;
    }

    protected Atendimento() {
    }

    // =========================================================================
    // Validation
    // =========================================================================

    private static class ValidatedData {
        final LocalDateTime dataHora;
        final Setor setor;
        final Pessoa paciente;
        final Pessoa responsavel;
        final Convenio convenio;
        final ConvenioCategoria convenioCategoria;
        final Profissional profissionalAtendimento;
        final Profissional profissionalResponsavel;
        final Procedimento procedimento;
        final TabelaItem tabelaItem;
        final StatusAtendimento status;
        final String observacoes;

        ValidatedData(
            LocalDateTime dataHora,
            Setor setor,
            Pessoa paciente,
            Pessoa responsavel,
            Convenio convenio,
            ConvenioCategoria convenioCategoria,
            Profissional profissionalAtendimento,
            Profissional profissionalResponsavel,
            Procedimento procedimento,
            TabelaItem tabelaItem,
            StatusAtendimento status,
            String observacoes
        ) {
            this.dataHora = dataHora;
            this.setor = setor;
            this.paciente = paciente;
            this.responsavel = responsavel;
            this.convenio = convenio;
            this.convenioCategoria = convenioCategoria;
            this.profissionalAtendimento = profissionalAtendimento;
            this.profissionalResponsavel = profissionalResponsavel;
            this.procedimento = procedimento;
            this.tabelaItem = tabelaItem;
            this.status = status;
            this.observacoes = observacoes;
        }
    }

    private static ValidatedData validate(
        LocalDateTime dataHora,
        Setor setor,
        Pessoa paciente,
        Pessoa responsavel,
        Convenio convenio,
        ConvenioCategoria convenioCategoria,
        Profissional profissionalAtendimento,
        Profissional profissionalResponsavel,
        Procedimento procedimento,
        TabelaItem tabelaItem,
        StatusAtendimento status,
        String observacoes
    ) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(dataHora, "dataHora", violations).notNull();
        Validator.of(setor, "setor", violations).notNull();
        Validator.of(paciente, "paciente", violations).notNull();
        Validator.of(profissionalAtendimento, "profissionalAtendimento", violations).notNull();
        Validator.of(profissionalResponsavel, "profissionalResponsavel", violations).notNull();
        Validator.of(procedimento, "procedimento", violations).notNull();
        Validator.of(status, "status", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("atendimento", violations);
        }
        return new ValidatedData(
            dataHora, setor, paciente, responsavel,
            convenio, convenioCategoria,
            profissionalAtendimento, profissionalResponsavel,
            procedimento, tabelaItem, status, observacoes
        );
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private LocalDateTime dataHora;
        private Setor setor;
        private Pessoa paciente;
        private Pessoa responsavel;
        private Convenio convenio;
        private ConvenioCategoria convenioCategoria;
        private Profissional profissionalAtendimento;
        private Profissional profissionalResponsavel;
        private Procedimento procedimento;
        private TabelaItem tabelaItem;
        private StatusAtendimento status = StatusAtendimento.AGENDADO;
        private String observacoes;

        public Builder dataHora(LocalDateTime dataHora) {
            this.dataHora = dataHora;
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

        public Builder convenioCategoria(ConvenioCategoria convenioCategoria) {
            this.convenioCategoria = convenioCategoria;
            return this;
        }

        public Builder profissionalAtendimento(Profissional profissionalAtendimento) {
            this.profissionalAtendimento = profissionalAtendimento;
            return this;
        }

        public Builder profissionalResponsavel(Profissional profissionalResponsavel) {
            this.profissionalResponsavel = profissionalResponsavel;
            return this;
        }

        public Builder procedimento(Procedimento procedimento) {
            this.procedimento = procedimento;
            return this;
        }

        public Builder tabelaItem(TabelaItem tabelaItem) {
            this.tabelaItem = tabelaItem;
            return this;
        }

        public Builder status(StatusAtendimento status) {
            this.status = status;
            return this;
        }

        public Builder observacoes(String observacoes) {
            this.observacoes = observacoes;
            return this;
        }

        public Atendimento build() {
            ValidatedData data = validate(
                dataHora, setor, paciente, responsavel,
                convenio, convenioCategoria,
                profissionalAtendimento, profissionalResponsavel,
                procedimento, tabelaItem, status, observacoes
            );
            return new Atendimento(data);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(
        LocalDateTime dataHora,
        Setor setor,
        Pessoa paciente,
        Pessoa responsavel,
        Convenio convenio,
        ConvenioCategoria convenioCategoria,
        Profissional profissionalAtendimento,
        Profissional profissionalResponsavel,
        Procedimento procedimento,
        TabelaItem tabelaItem,
        StatusAtendimento status,
        String observacoes
    ) {
        ValidatedData data = validate(
            dataHora, setor, paciente, responsavel,
            convenio, convenioCategoria,
            profissionalAtendimento, profissionalResponsavel,
            procedimento, tabelaItem, status, observacoes
        );
        this.dataHora = data.dataHora;
        this.setor = data.setor;
        this.paciente = data.paciente;
        this.responsavel = data.responsavel;
        this.convenio = data.convenio;
        this.convenioCategoria = data.convenioCategoria;
        this.profissionalAtendimento = data.profissionalAtendimento;
        this.profissionalResponsavel = data.profissionalResponsavel;
        this.procedimento = data.procedimento;
        this.tabelaItem = data.tabelaItem;
        this.status = data.status;
        this.observacoes = data.observacoes;
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public LocalDateTime getDataHora() {
        return dataHora;
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

    public Procedimento getProcedimento() {
        return procedimento;
    }

    public TabelaItem getTabelaItem() {
        return tabelaItem;
    }

    public StatusAtendimento getStatus() {
        return status;
    }

    public String getObservacoes() {
        return observacoes;
    }
}
