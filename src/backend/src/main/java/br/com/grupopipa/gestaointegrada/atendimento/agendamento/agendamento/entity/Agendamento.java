package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity;

import java.util.ArrayList;
import java.util.List;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.entity.Agenda;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.AgendamentoValidator;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "agendamento")
public class Agendamento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "agenda_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_agendamento_agenda")
    )
    private Agenda agenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "paciente_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_agendamento_paciente")
    )
    private Pessoa paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "convenio_id",
        foreignKey = @ForeignKey(name = "fk_agendamento_convenio")
    )
    private Convenio convenio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "categoria_id",
        foreignKey = @ForeignKey(name = "fk_agendamento_categoria")
    )
    private ConvenioCategoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "procedimento_id",
        foreignKey = @ForeignKey(name = "fk_agendamento_procedimento")
    )
    private Procedimento procedimento;

    @Column(name = "observacao", length = 1000)
    private String observacao;

    @Column(name = "atendimento_id")
    private UUID atendimentoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AgendamentoStatus status = AgendamentoStatus.AGENDADO;

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.LAZY)
    private List<AgendamentoHorario> horarios = new ArrayList<>();

    private Agendamento(AgendamentoValidator.ValidatedData data) {
        this.agenda = data.agenda;
        this.paciente = data.paciente;
        this.convenio = data.convenio;
        this.categoria = data.categoria;
        this.procedimento = data.procedimento;
        this.observacao = data.observacao;
    }

    protected Agendamento() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private Agenda agenda;
        private Pessoa paciente;
        private Convenio convenio;
        private ConvenioCategoria categoria;
        private Procedimento procedimento;
        private String observacao;

        public Builder agenda(Agenda agenda) {
            this.agenda = agenda;
            return this;
        }

        public Builder paciente(Pessoa paciente) {
            this.paciente = paciente;
            return this;
        }

        public Builder convenio(Convenio convenio) {
            this.convenio = convenio;
            return this;
        }

        public Builder categoria(ConvenioCategoria categoria) {
            this.categoria = categoria;
            return this;
        }

        public Builder procedimento(Procedimento procedimento) {
            this.procedimento = procedimento;
            return this;
        }

        public Builder observacao(String observacao) {
            this.observacao = observacao;
            return this;
        }

        public Agendamento build() {
            return new Agendamento(
                AgendamentoValidator.validate(agenda, paciente, convenio, categoria,
                                             procedimento, observacao));
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(Agenda agenda, Pessoa paciente, Convenio convenio,
            ConvenioCategoria categoria, Procedimento procedimento, String observacao) {
        AgendamentoValidator.ValidatedData data =
            AgendamentoValidator.validate(agenda, paciente, convenio, categoria,
                                         procedimento, observacao);
        this.agenda = data.agenda;
        this.paciente = data.paciente;
        this.convenio = data.convenio;
        this.categoria = data.categoria;
        this.procedimento = data.procedimento;
        this.observacao = data.observacao;
    }

    public void vincularAtendimento(UUID atendimentoId) {
        this.atendimentoId = atendimentoId;
    }

    public void cancelar() {
        this.status = AgendamentoStatus.CANCELADO;
    }

    public void realizar() {
        this.status = AgendamentoStatus.REALIZADO;
    }

    public void addHorario(AgendamentoHorario horario) {
        horarios.add(horario);
    }

    public void limparHorarios() {
        horarios.clear();
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public Agenda getAgenda() {
        return agenda;
    }

    public Pessoa getPaciente() {
        return paciente;
    }

    public Convenio getConvenio() {
        return convenio;
    }

    public ConvenioCategoria getCategoria() {
        return categoria;
    }

    public Procedimento getProcedimento() {
        return procedimento;
    }

    public String getObservacao() {
        return observacao;
    }

    public AgendamentoStatus getStatus() {
        return status;
    }

    public List<AgendamentoHorario> getHorarios() {
        return horarios;
    }

    public UUID getAtendimentoId() {
        return atendimentoId;
    }
}
