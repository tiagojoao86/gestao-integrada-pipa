package br.com.grupopipa.gestaointegrada.financeiro.caixa.entity;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.financeiro.caixa.CaixaValidator;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "caixa", uniqueConstraints = {
    @UniqueConstraint(name = "uk_caixa_nome", columnNames = "nome")
})
public class Caixa extends BaseEntity {

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "valor_padrao_abertura", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPadraoAbertura = BigDecimal.ZERO;

    @Column(name = "percentual_pagamento_parcial", precision = 5, scale = 2)
    private BigDecimal percentualPagamentoParcial;

    @Column(name = "valor_minimo_parcela", precision = 15, scale = 2)
    private BigDecimal valorMinimoParcela;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @ElementCollection
    @CollectionTable(name = "usuario_caixa", joinColumns = @JoinColumn(name = "caixa_id"))
    @Column(name = "usuario_id")
    private Set<UUID> usuarioIds = new HashSet<>();

    private Caixa(CaixaValidator.ValidatedData data) {
        this.nome = data.nome;
        this.valorPadraoAbertura = data.valorPadraoAbertura;
        this.percentualPagamentoParcial = data.percentualPagamentoParcial;
        this.valorMinimoParcela = data.valorMinimoParcela;
        this.ativo = true;
    }

    protected Caixa() {
    }

    public void atualizar(
            String nome,
            BigDecimal valorPadraoAbertura,
            BigDecimal percentualPagamentoParcial,
            BigDecimal valorMinimoParcela) {
        CaixaValidator.ValidatedData data = CaixaValidator.validate(
                nome, valorPadraoAbertura, percentualPagamentoParcial, valorMinimoParcela);
        this.nome = data.nome;
        this.valorPadraoAbertura = data.valorPadraoAbertura;
        this.percentualPagamentoParcial = data.percentualPagamentoParcial;
        this.valorMinimoParcela = data.valorMinimoParcela;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void inativar() {
        this.ativo = false;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValorPadraoAbertura() {
        return valorPadraoAbertura;
    }

    public BigDecimal getPercentualPagamentoParcial() {
        return percentualPagamentoParcial;
    }

    public BigDecimal getValorMinimoParcela() {
        return valorMinimoParcela;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public Set<UUID> getUsuarioIds() {
        return usuarioIds;
    }

    public void setUsuarioIds(Set<UUID> usuarioIds) {
        this.usuarioIds = usuarioIds != null ? usuarioIds : new HashSet<>();
    }

    public static class Builder {
        private String nome;
        private BigDecimal valorPadraoAbertura = BigDecimal.ZERO;
        private BigDecimal percentualPagamentoParcial;
        private BigDecimal valorMinimoParcela;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder valorPadraoAbertura(BigDecimal v) {
            this.valorPadraoAbertura = v;
            return this;
        }

        public Builder percentualPagamentoParcial(BigDecimal v) {
            this.percentualPagamentoParcial = v;
            return this;
        }

        public Builder valorMinimoParcela(BigDecimal v) {
            this.valorMinimoParcela = v;
            return this;
        }

        public Caixa build() {
            return new Caixa(CaixaValidator.validate(
                    nome, valorPadraoAbertura, percentualPagamentoParcial, valorMinimoParcela));
        }
    }
}
