package br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.entity.AberturaCaixa;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.MovimentacaoCaixaValidator;
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
@Table(name = "movimentacao_caixa")
public class MovimentacaoCaixa extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abertura_caixa_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_movimentacao_caixa_abertura"))
    private AberturaCaixa aberturaCaixa;

    @Column(name = "lancamento_id", nullable = false)
    private UUID lancamentoId;

    @Column(name = "titulo_id")
    private UUID tituloId;

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    private MovimentacaoCaixa(MovimentacaoCaixaValidator.ValidatedData data) {
        this.aberturaCaixa = data.aberturaCaixa;
        this.lancamentoId = data.lancamentoId;
        this.tituloId = data.tituloId;
        this.valor = data.valor;
        this.formaPagamento = data.formaPagamento;
        this.observacoes = data.observacoes;
        this.dataHora = LocalDateTime.now();
    }

    protected MovimentacaoCaixa() {}

    public AberturaCaixa getAberturaCaixa() {
        return aberturaCaixa;
    }

    public UUID getLancamentoId() {
        return lancamentoId;
    }

    public UUID getTituloId() {
        return tituloId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public static class Builder {
        private AberturaCaixa aberturaCaixa;
        private UUID lancamentoId;
        private UUID tituloId;
        private BigDecimal valor;
        private FormaPagamento formaPagamento;
        private String observacoes;

        public Builder aberturaCaixa(AberturaCaixa a) {
            this.aberturaCaixa = a;
            return this;
        }

        public Builder lancamentoId(UUID id) {
            this.lancamentoId = id;
            return this;
        }

        public Builder tituloId(UUID id) {
            this.tituloId = id;
            return this;
        }

        public Builder valor(BigDecimal v) {
            this.valor = v;
            return this;
        }

        public Builder formaPagamento(FormaPagamento f) {
            this.formaPagamento = f;
            return this;
        }

        public Builder observacoes(String o) {
            this.observacoes = o;
            return this;
        }

        public MovimentacaoCaixa build() {
            return new MovimentacaoCaixa(MovimentacaoCaixaValidator.validate(
                aberturaCaixa, lancamentoId, tituloId, valor, formaPagamento, observacoes));
        }
    }
}
