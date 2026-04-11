package br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.TabelaItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "atendimento_procedimento")
public class AtendimentoProcedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_atend_proc_atendimento"))
    private Atendimento atendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimento_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_atend_proc_procedimento"))
    private Procedimento procedimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tabela_item_id",
        foreignKey = @ForeignKey(name = "fk_atend_proc_tabela_item"))
    private TabelaItem tabelaItem;

    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    protected AtendimentoProcedimento() {
    }

    public AtendimentoProcedimento(
            Atendimento atendimento,
            Procedimento procedimento,
            TabelaItem tabelaItem,
            LocalDateTime dataInicio,
            LocalDateTime dataFim) {
        this.atendimento = atendimento;
        this.procedimento = procedimento;
        this.tabelaItem = tabelaItem;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public UUID getId() {
        return id;
    }

    public Atendimento getAtendimento() {
        return atendimento;
    }

    public Procedimento getProcedimento() {
        return procedimento;
    }

    public TabelaItem getTabelaItem() {
        return tabelaItem;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public LocalDateTime getDataFim() {
        return dataFim;
    }
}
