package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.entity;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.AgendaValidator;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "agenda")
public class Agenda extends BaseEntity {

    @Embedded
    private Nome nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "profissional_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_agenda_profissional")
    )
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "setor_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_agenda_setor")
    )
    private Setor setor;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    private Agenda(AgendaValidator.ValidatedData data) {
        this.nome = data.nome;
        this.profissional = data.profissional;
        this.setor = data.setor;
        this.ativo = data.ativo != null ? data.ativo : true;
    }

    protected Agenda() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private String nome;
        private Profissional profissional;
        private Setor setor;
        private Boolean ativo = true;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder profissional(Profissional profissional) {
            this.profissional = profissional;
            return this;
        }

        public Builder setor(Setor setor) {
            this.setor = setor;
            return this;
        }

        public Builder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public Agenda build() {
            return new Agenda(AgendaValidator.validate(nome, profissional, setor, ativo));
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(String nomeStr, Profissional profissional, Setor setor, Boolean ativo) {
        AgendaValidator.ValidatedData data = AgendaValidator.validate(nomeStr, profissional, setor, ativo);
        this.nome = data.nome;
        this.profissional = data.profissional;
        this.setor = data.setor;
        if (data.ativo != null) {
            this.ativo = data.ativo;
        }
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public String getNome() {
        return nome != null ? nome.getValue() : null;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public Setor getSetor() {
        return setor;
    }

    public Boolean getAtivo() {
        return ativo;
    }
}
