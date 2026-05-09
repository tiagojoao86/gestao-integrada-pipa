package br.com.grupopipa.gestaointegrada.financeiro.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.entity.UnidadeNegocioFiltravel;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacao.MovimentacaoFinanceiraValidator;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidade para MovimentacaoFinanceira - representa o dinheiro real no
 * caixa/banco (regime de caixa)
 */
@Entity
@Table(name = "movimentacao_financeira", indexes = {
    @Index(name = "idx_movimentacao_data", columnList = "data"),
    @Index(name = "idx_movimentacao_conta", columnList = "conta_bancaria_id")
})
public class MovimentacaoFinanceira extends BaseEntity implements UnidadeNegocioFiltravel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_negocio_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_movimentacao_unidade_negocio"))
    private UnidadeNegocio unidadeNegocio;

    @OneToMany(mappedBy = "movimentacaoFinanceira",
        cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<MovimentacaoFinanceiraTitulo> titulosAssociados = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_bancaria_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movimentacao_conta"))
    private ContaBancaria contaBancaria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoMovimentacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "valor", nullable = false, precision = 15, scale = 2))
    private Money valor;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "movimentacao_caixa_id")
    private UUID movimentacaoCaixaId;

    private MovimentacaoFinanceira(
            Set<Titulo> titulos,
            Map<Titulo, Money> valoresPorTitulo,
            ContaBancaria contaBancaria,
            TipoMovimentacao tipo,
            FormaPagamento formaPagamento,
            Money valor,
            LocalDate data,
            UnidadeNegocio unidadeNegocio,
            UUID movimentacaoCaixaId) {
        this.titulosAssociados = new HashSet<>();
        this.contaBancaria = contaBancaria;
        this.tipo = tipo;
        this.formaPagamento = formaPagamento;
        this.valor = valor;
        this.data = data;
        this.unidadeNegocio = unidadeNegocio;
        this.movimentacaoCaixaId = movimentacaoCaixaId;
        if (titulos != null) {
            titulos.forEach(t -> {
                Money valorAplicado = (valoresPorTitulo != null && valoresPorTitulo.containsKey(t))
                        ? valoresPorTitulo.get(t)
                        : t.calcularSaldo();
                MovimentacaoFinanceiraTitulo mt = MovimentacaoFinanceiraTitulo.create(this, t, valorAplicado);
                this.titulosAssociados.add(mt);
                t.getMovimentacoes().add(mt);
                t.registrarPagamento(valorAplicado);
            });
        }
    }

    protected MovimentacaoFinanceira() {
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private Set<Titulo> titulos = new HashSet<>();
        private Map<Titulo, Money> valoresPorTitulo = new HashMap<>();
        private ContaBancaria contaBancaria;
        private TipoMovimentacao tipo;
        private FormaPagamento formaPagamento;
        private Money valor;
        private LocalDate data;
        private UnidadeNegocio unidadeNegocio;
        private UUID movimentacaoCaixaId;

        public Builder titulos(Set<Titulo> titulos) {
            this.titulos = titulos;
            return this;
        }

        public Builder addTitulo(Titulo titulo) {
            this.titulos.add(titulo);
            return this;
        }

        public Builder addTituloComValor(Titulo titulo, Money valorTitulo) {
            this.titulos.add(titulo);
            this.valoresPorTitulo.put(titulo, valorTitulo);
            return this;
        }

        public Builder contaBancaria(ContaBancaria contaBancaria) {
            this.contaBancaria = contaBancaria;
            return this;
        }

        public Builder tipo(TipoMovimentacao tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder formaPagamento(FormaPagamento formaPagamento) {
            this.formaPagamento = formaPagamento;
            return this;
        }

        public Builder valor(Money valor) {
            this.valor = valor;
            return this;
        }

        public Builder data(LocalDate data) {
            this.data = data;
            return this;
        }

        public Builder unidadeNegocio(UnidadeNegocio unidadeNegocio) {
            this.unidadeNegocio = unidadeNegocio;
            return this;
        }

        public Builder movimentacaoCaixaId(UUID movimentacaoCaixaId) {
            this.movimentacaoCaixaId = movimentacaoCaixaId;
            return this;
        }

        public MovimentacaoFinanceira build() {
            if (this.unidadeNegocio == null && this.contaBancaria != null) {
                this.unidadeNegocio = this.contaBancaria.getUnidadeNegocio();
            }

            BigDecimal valorValue = (this.valor != null) ? this.valor.getValue() : null;
            MovimentacaoFinanceiraValidator.ValidatedData validatedData =
                    MovimentacaoFinanceiraValidator.validate(
                            this.titulos,
                            this.contaBancaria,
                            this.tipo,
                            this.formaPagamento,
                            valorValue,
                            this.data,
                            this.unidadeNegocio);
            return new MovimentacaoFinanceira(
                    validatedData.titulos,
                    this.valoresPorTitulo,
                    validatedData.contaBancaria,
                    validatedData.tipo,
                    validatedData.formaPagamento,
                    validatedData.valor,
                    validatedData.data,
                    validatedData.unidadeNegocio,
                    this.movimentacaoCaixaId);
        }
    }

    // =========================================================================
    // Domain methods
    // =========================================================================

    @Override
    public UnidadeNegocio getUnidadeNegocio() {
        return unidadeNegocio;
    }

    public void setUnidadeNegocio(UnidadeNegocio unidadeNegocio) {
        this.unidadeNegocio = unidadeNegocio;
    }

    public void adicionarObservacao(String observacao) {
        if (this.observacoes == null) {
            this.observacoes = observacao;
        } else {
            this.observacoes += "\n" + observacao;
        }
    }

    public boolean isPagamento() {
        return tipo == TipoMovimentacao.PAGAMENTO;
    }

    public boolean isRecebimento() {
        return tipo == TipoMovimentacao.RECEBIMENTO;
    }

    public boolean isEstorno() {
        return tipo == TipoMovimentacao.ESTORNO;
    }

    // Getters

    /** Retorna os títulos associados com seus valores de pagamento */
    public Set<MovimentacaoFinanceiraTitulo> getTitulosAssociados() {
        return titulosAssociados;
    }

    /** Retorna apenas os títulos (sem o valor por título) */
    public Set<Titulo> getTitulos() {
        if (titulosAssociados == null) {
            return new HashSet<>();
        }
        return titulosAssociados.stream()
                .map(MovimentacaoFinanceiraTitulo::getTitulo)
                .collect(Collectors.toSet());
    }

    public ContaBancaria getContaBancaria() {
        return contaBancaria;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public Money getValor() {
        return valor;
    }

    public LocalDate getData() {
        return data;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public UUID getMovimentacaoCaixaId() {
        return movimentacaoCaixaId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovimentacaoFinanceira)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MovimentacaoFinanceira that = (MovimentacaoFinanceira) o;
        return Objects.equals(data, that.data)
                && Objects.equals(valor, that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), data, valor);
    }
}
