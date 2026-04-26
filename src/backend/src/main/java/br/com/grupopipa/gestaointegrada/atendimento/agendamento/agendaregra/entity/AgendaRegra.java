package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.entity.Agenda;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.AgendaRegraValidator;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "agenda_regra")
public class AgendaRegra extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "agenda_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_agenda_regra_agenda")
    )
    private Agenda agenda;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    @Column(name = "duracao_sessao_minutos", nullable = false)
    private Integer duracaoSessaoMinutos;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "agenda_regra_dia_semana",
        joinColumns = @JoinColumn(
            name = "agenda_regra_id",
            foreignKey = @ForeignKey(name = "fk_agenda_regra_ds_regra")
        )
    )
    @Column(name = "dia_semana")
    @Enumerated(EnumType.STRING)
    private Set<DiaSemana> diasSemana = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "agenda_regra_convenio",
        joinColumns = @JoinColumn(
            name = "agenda_regra_id",
            foreignKey = @ForeignKey(name = "fk_agenda_regra_conv_regra")
        ),
        inverseJoinColumns = @JoinColumn(
            name = "convenio_id",
            foreignKey = @ForeignKey(name = "fk_agenda_regra_conv_convenio")
        )
    )
    private Set<Convenio> convenios = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "agenda_regra_procedimento",
        joinColumns = @JoinColumn(
            name = "agenda_regra_id",
            foreignKey = @ForeignKey(name = "fk_agenda_regra_proc_regra")
        ),
        inverseJoinColumns = @JoinColumn(
            name = "procedimento_id",
            foreignKey = @ForeignKey(name = "fk_agenda_regra_proc_procedimento")
        )
    )
    private Set<Procedimento> procedimentos = new HashSet<>();

    private AgendaRegra(AgendaRegraValidator.ValidatedData data) {
        this.agenda = data.agenda;
        this.dataInicio = data.dataInicio;
        this.dataFim = data.dataFim;
        this.horaInicio = data.horaInicio;
        this.horaFim = data.horaFim;
        this.duracaoSessaoMinutos = data.duracaoSessaoMinutos;
    }

    protected AgendaRegra() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private Agenda agenda;
        private LocalDate dataInicio;
        private LocalDate dataFim;
        private LocalTime horaInicio;
        private LocalTime horaFim;
        private Integer duracaoSessaoMinutos;

        public Builder agenda(Agenda agenda) {
            this.agenda = agenda;
            return this;
        }

        public Builder dataInicio(LocalDate dataInicio) {
            this.dataInicio = dataInicio;
            return this;
        }

        public Builder dataFim(LocalDate dataFim) {
            this.dataFim = dataFim;
            return this;
        }

        public Builder horaInicio(LocalTime horaInicio) {
            this.horaInicio = horaInicio;
            return this;
        }

        public Builder horaFim(LocalTime horaFim) {
            this.horaFim = horaFim;
            return this;
        }

        public Builder duracaoSessaoMinutos(Integer duracaoSessaoMinutos) {
            this.duracaoSessaoMinutos = duracaoSessaoMinutos;
            return this;
        }

        public AgendaRegra build() {
            return new AgendaRegra(AgendaRegraValidator.validate(
                agenda, dataInicio, dataFim, horaInicio, horaFim, duracaoSessaoMinutos));
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(
            Agenda agenda,
            LocalDate dataInicio,
            LocalDate dataFim,
            LocalTime horaInicio,
            LocalTime horaFim,
            Integer duracaoSessaoMinutos) {
        AgendaRegraValidator.ValidatedData data = AgendaRegraValidator.validate(
            agenda, dataInicio, dataFim, horaInicio, horaFim, duracaoSessaoMinutos);
        this.agenda = data.agenda;
        this.dataInicio = data.dataInicio;
        this.dataFim = data.dataFim;
        this.horaInicio = data.horaInicio;
        this.horaFim = data.horaFim;
        this.duracaoSessaoMinutos = data.duracaoSessaoMinutos;
    }

    public void setDiasSemana(Set<DiaSemana> diasSemana) {
        this.diasSemana = diasSemana != null ? diasSemana : new HashSet<>();
    }

    public void setConvenios(Set<Convenio> convenios) {
        this.convenios = convenios != null ? convenios : new HashSet<>();
    }

    public void setProcedimentos(Set<Procedimento> procedimentos) {
        this.procedimentos = procedimentos != null ? procedimentos : new HashSet<>();
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public Agenda getAgenda() {
        return agenda;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public Integer getDuracaoSessaoMinutos() {
        return duracaoSessaoMinutos;
    }

    public Set<DiaSemana> getDiasSemana() {
        return diasSemana;
    }

    public Set<Convenio> getConvenios() {
        return convenios;
    }

    public Set<Procedimento> getProcedimentos() {
        return procedimentos;
    }
}
