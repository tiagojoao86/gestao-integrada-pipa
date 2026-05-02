package br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.ConvenioTipoCobrancaEnum;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.LancamentoFinanceiroValidator;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "lancamento_financeiro")
public class LancamentoFinanceiro extends BaseEntity {

    @Column(name = "atendimento_id", nullable = false)
    private UUID atendimentoId;

    @Column(name = "atendimento_numero")
    private Long atendimentoNumero;

    @Column(name = "data_atendimento")
    private LocalDate dataAtendimento;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "paciente_nome")
    private String pacienteNome;

    @Column(name = "convenio_id")
    private UUID convenioId;

    @Column(name = "convenio_nome")
    private String convenioNome;

    @Enumerated(EnumType.STRING)
    @Column(name = "convenio_tipo_cobranca")
    private ConvenioTipoCobrancaEnum convenioTipoCobranca;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "valor_total", precision = 15, scale = 2))
    private Money valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false)
    private LancamentoFinanceiroSituacaoEnum situacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_financeiro", nullable = false)
    private LancamentoFinanceiroStatusFinanceiroEnum statusFinanceiro;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "setor_id")
    private UUID setorId;

    @Column(name = "setor_nome", length = 150)
    private String setorNome;

    @Column(name = "unidade_negocio_id")
    private UUID unidadeNegocioId;

    @Column(name = "unidade_negocio_nome", length = 150)
    private String unidadeNegocioNome;

    @Column(name = "titulo_id")
    private UUID tituloId;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "lancamento", cascade = CascadeType.ALL, orphanRemoval = true,
        fetch = FetchType.LAZY)
    private List<LancamentoFinanceiroProcedimento> procedimentos = new ArrayList<>();

    private LancamentoFinanceiro(LancamentoFinanceiroValidator.ValidatedData data) {
        this.atendimentoId = data.atendimentoId;
        this.atendimentoNumero = data.atendimentoNumero;
        this.dataAtendimento = data.dataAtendimento;
        this.pacienteId = data.pacienteId;
        this.pacienteNome = data.pacienteNome;
        this.convenioId = data.convenioId;
        this.convenioNome = data.convenioNome;
        this.convenioTipoCobranca = data.convenioTipoCobranca;
        this.valorTotal = data.valorTotal;
        this.situacao = data.situacao;
        this.statusFinanceiro = data.statusFinanceiro;
        this.observacoes = data.observacoes;
        this.setorId = data.setorId;
        this.setorNome = data.setorNome;
        this.unidadeNegocioId = data.unidadeNegocioId;
        this.unidadeNegocioNome = data.unidadeNegocioNome;
        this.tituloId = data.tituloId;
    }

    protected LancamentoFinanceiro() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private UUID atendimentoId;
        private Long atendimentoNumero;
        private LocalDate dataAtendimento;
        private UUID pacienteId;
        private String pacienteNome;
        private UUID convenioId;
        private String convenioNome;
        private ConvenioTipoCobrancaEnum convenioTipoCobranca;
        private BigDecimal valorTotal;
        private LancamentoFinanceiroSituacaoEnum situacao = LancamentoFinanceiroSituacaoEnum.ABERTO;
        private LancamentoFinanceiroStatusFinanceiroEnum statusFinanceiro =
            LancamentoFinanceiroStatusFinanceiroEnum.PENDENTE;
        private String observacoes;
        private UUID setorId;
        private String setorNome;
        private UUID unidadeNegocioId;
        private String unidadeNegocioNome;
        private UUID tituloId;

        public Builder atendimentoId(UUID atendimentoId) {
            this.atendimentoId = atendimentoId;
            return this;
        }

        public Builder atendimentoNumero(Long n) {
            this.atendimentoNumero = n;
            return this;
        }

        public Builder dataAtendimento(LocalDate d) {
            this.dataAtendimento = d;
            return this;
        }

        public Builder pacienteId(UUID pacienteId) {
            this.pacienteId = pacienteId;
            return this;
        }

        public Builder pacienteNome(String n) {
            this.pacienteNome = n;
            return this;
        }

        public Builder convenioId(UUID convenioId) {
            this.convenioId = convenioId;
            return this;
        }

        public Builder convenioNome(String n) {
            this.convenioNome = n;
            return this;
        }

        public Builder convenioTipoCobranca(ConvenioTipoCobrancaEnum tipo) {
            this.convenioTipoCobranca = tipo;
            return this;
        }

        public Builder valorTotal(BigDecimal v) {
            this.valorTotal = v;
            return this;
        }

        public Builder situacao(LancamentoFinanceiroSituacaoEnum s) {
            this.situacao = s;
            return this;
        }

        public Builder statusFinanceiro(LancamentoFinanceiroStatusFinanceiroEnum s) {
            this.statusFinanceiro = s;
            return this;
        }

        public Builder observacoes(String o) {
            this.observacoes = o;
            return this;
        }

        public Builder setorId(UUID setorId) {
            this.setorId = setorId;
            return this;
        }

        public Builder setorNome(String setorNome) {
            this.setorNome = setorNome;
            return this;
        }

        public Builder unidadeNegocioId(UUID unidadeNegocioId) {
            this.unidadeNegocioId = unidadeNegocioId;
            return this;
        }

        public Builder unidadeNegocioNome(String unidadeNegocioNome) {
            this.unidadeNegocioNome = unidadeNegocioNome;
            return this;
        }

        public Builder tituloId(UUID tituloId) {
            this.tituloId = tituloId;
            return this;
        }

        public LancamentoFinanceiro build() {
            return new LancamentoFinanceiro(LancamentoFinanceiroValidator.validate(
                atendimentoId, atendimentoNumero, dataAtendimento,
                pacienteId, pacienteNome, convenioId, convenioNome, convenioTipoCobranca,
                valorTotal, situacao, statusFinanceiro, observacoes,
                setorId, setorNome, unidadeNegocioId, unidadeNegocioNome, tituloId));
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    public void atualizar(String observacoes) {
        if (this.situacao != LancamentoFinanceiroSituacaoEnum.ABERTO) {
            Set<BeanValidationMessage> violations = new HashSet<>();
            violations.add(new BeanValidationMessage(
                "situacao", "Apenas lançamentos em situação ABERTO podem ser editados."));
            throw new BeanValidationException("lancamentoFinanceiro", violations);
        }
        this.observacoes = observacoes;
    }

    public void syncProcedimentos(List<LancamentoFinanceiroProcedimento> novos) {
        this.procedimentos.clear();
        this.procedimentos.addAll(novos);
        recalcularValorTotal();
    }

    public void recalcularValorTotal() {
        this.valorTotal = procedimentos.stream()
            .map(LancamentoFinanceiroProcedimento::getValor)
            .filter(Objects::nonNull)
            .map(Money::of)
            .reduce(Money.zero(), Money::add);
    }

    public void fechar() {
        if (this.situacao != LancamentoFinanceiroSituacaoEnum.ABERTO) {
            Set<BeanValidationMessage> violations = new HashSet<>();
            violations.add(new BeanValidationMessage(
                "situacao", "Apenas lançamentos em situação ABERTO podem ser fechados."));
            throw new BeanValidationException("lancamentoFinanceiro", violations);
        }
        this.situacao = LancamentoFinanceiroSituacaoEnum.FECHADO;
        this.statusFinanceiro = LancamentoFinanceiroStatusFinanceiroEnum.FATURADO;
    }

    public void marcarComoPago() {
        this.statusFinanceiro = LancamentoFinanceiroStatusFinanceiroEnum.PAGO;
    }

    public void vincularTitulo(UUID id) {
        this.tituloId = id;
    }

    public void atualizarSetorSnapshot(
            UUID novoSetorId, String novoSetorNome,
            UUID novaUnidadeNegocioId, String novaUnidadeNegocioNome) {
        if (this.situacao != LancamentoFinanceiroSituacaoEnum.ABERTO) {
            return;
        }
        this.setorId = novoSetorId;
        this.setorNome = novoSetorNome;
        this.unidadeNegocioId = novaUnidadeNegocioId;
        this.unidadeNegocioNome = novaUnidadeNegocioNome;
    }

    public void cancelar() {
        if (this.situacao == LancamentoFinanceiroSituacaoEnum.FECHADO
                && this.statusFinanceiro == LancamentoFinanceiroStatusFinanceiroEnum.FATURADO) {
            Set<BeanValidationMessage> violations = new HashSet<>();
            violations.add(new BeanValidationMessage(
                "situacao", "Lançamentos já faturados ao convênio não podem ser cancelados."));
            throw new BeanValidationException("lancamentoFinanceiro", violations);
        }
        this.situacao = LancamentoFinanceiroSituacaoEnum.CANCELADO;
    }

    // =========================================================================
    // Getters
    // =========================================================================

    public UUID getAtendimentoId() {
        return atendimentoId;
    }

    public Long getAtendimentoNumero() {
        return atendimentoNumero;
    }

    public LocalDate getDataAtendimento() {
        return dataAtendimento;
    }

    public UUID getPacienteId() {
        return pacienteId;
    }

    public String getPacienteNome() {
        return pacienteNome;
    }

    public UUID getConvenioId() {
        return convenioId;
    }

    public String getConvenioNome() {
        return convenioNome;
    }

    public ConvenioTipoCobrancaEnum getConvenioTipoCobranca() {
        return convenioTipoCobranca;
    }

    public BigDecimal getValorTotal() {
        return valorTotal != null ? valorTotal.getValue() : null;
    }

    public LancamentoFinanceiroSituacaoEnum getSituacao() {
        return situacao;
    }

    public LancamentoFinanceiroStatusFinanceiroEnum getStatusFinanceiro() {
        return statusFinanceiro;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public List<LancamentoFinanceiroProcedimento> getProcedimentos() {
        return procedimentos;
    }

    public UUID getSetorId() {
        return setorId;
    }

    public String getSetorNome() {
        return setorNome;
    }

    public UUID getUnidadeNegocioId() {
        return unidadeNegocioId;
    }

    public String getUnidadeNegocioNome() {
        return unidadeNegocioNome;
    }

    public UUID getTituloId() {
        return tituloId;
    }
}
