package br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
@Table(name = "lancamento_financeiro_procedimento")
public class LancamentoFinanceiroProcedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lancamento_financeiro_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_lanc_proc_lancamento"))
    private LancamentoFinanceiro lancamento;

    @Column(name = "procedimento_id")
    private UUID procedimentoId;

    @Column(name = "procedimento_codigo")
    private String procedimentoCodigo;

    @Column(name = "procedimento_descricao")
    private String procedimentoDescricao;

    @Column(name = "convenio_id")
    private UUID convenioId;

    @Column(name = "convenio_nome")
    private String convenioNome;

    @Column(name = "tabela_item_id")
    private UUID tabelaItemId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "valor", precision = 15, scale = 2))
    private Money valor;

    protected LancamentoFinanceiroProcedimento() {
    }

    public LancamentoFinanceiroProcedimento(
            LancamentoFinanceiro lancamento,
            UUID procedimentoId,
            String procedimentoCodigo,
            String procedimentoDescricao,
            UUID convenioId,
            String convenioNome,
            UUID tabelaItemId,
            BigDecimal valor) {
        this.lancamento = lancamento;
        this.procedimentoId = procedimentoId;
        this.procedimentoCodigo = procedimentoCodigo;
        this.procedimentoDescricao = procedimentoDescricao;
        this.convenioId = convenioId;
        this.convenioNome = convenioNome;
        this.tabelaItemId = tabelaItemId;
        this.valor = valor != null ? Money.positiveOrZero(valor) : Money.zero();
    }

    public void atualizarValor(BigDecimal novoValor) {
        this.valor = novoValor != null ? Money.positiveOrZero(novoValor) : Money.zero();
    }

    public UUID getId() {
        return id;
    }

    public LancamentoFinanceiro getLancamento() {
        return lancamento;
    }

    public UUID getProcedimentoId() {
        return procedimentoId;
    }

    public String getProcedimentoCodigo() {
        return procedimentoCodigo;
    }

    public String getProcedimentoDescricao() {
        return procedimentoDescricao;
    }

    public UUID getConvenioId() {
        return convenioId;
    }

    public String getConvenioNome() {
        return convenioNome;
    }

    public UUID getTabelaItemId() {
        return tabelaItemId;
    }

    public BigDecimal getValor() {
        return valor != null ? valor.getValue() : null;
    }
}
