package br.com.grupopipa.gestaointegrada.financeiro.entity;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Entidade principal do domínio financeiro - representa títulos a pagar/receber
 * (regime de competência)
 */
@Entity
@Table(name = "titulo", indexes = {
        @Index(name = "idx_titulo_tipo_status", columnList = "tipo, status"),
        @Index(name = "idx_titulo_vencimento", columnList = "data_vencimento"),
        @Index(name = "idx_titulo_pessoa", columnList = "pessoa_id"),
        @Index(name = "idx_titulo_plano_contas", columnList = "plano_contas_id")
})
public class Titulo extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoTitulo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusTitulo status;

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false, foreignKey = @ForeignKey(name = "fk_titulo_pessoa"))
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_contas_id", nullable = false, foreignKey = @ForeignKey(name = "fk_titulo_plano_contas"))
    private PlanoContas planoContas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_negocio_id", nullable = false, foreignKey = @ForeignKey(name = "fk_titulo_unidade_negocio"))
    private UnidadeNegocio unidadeNegocio;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "valor_original", nullable = false, precision = 15, scale = 2))
    private Money valorOriginal;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "valor_pago", nullable = false, precision = 15, scale = 2))
    private Money valorPago;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "valor_desconto", nullable = false, precision = 15, scale = 2))
    private Money valorDesconto;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "valor_juros", nullable = false, precision = 15, scale = 2))
    private Money valorJuros;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "valor_multa", nullable = false, precision = 15, scale = 2))
    private Money valorMulta;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    // Campos para parcelamento
    @Column(name = "numero_parcela")
    private Integer numeroParcela;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titulo_origem_id", foreignKey = @ForeignKey(name = "fk_titulo_origem"))
    private Titulo tituloOrigem;

    @OneToMany(mappedBy = "titulo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimentacaoFinanceira> movimentacoes = new ArrayList<>();

    private Titulo(TipoTitulo tipo, String descricao, String numeroDocumento, Pessoa pessoa,
            PlanoContas planoContas, UnidadeNegocio unidadeNegocio, Money valorOriginal,
            LocalDate dataEmissao, LocalDate dataVencimento) {
        this.tipo = tipo;
        this.descricao = descricao;
        this.numeroDocumento = numeroDocumento;
        this.pessoa = pessoa;
        this.planoContas = planoContas;
        this.unidadeNegocio = unidadeNegocio;
        this.valorOriginal = valorOriginal;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
        this.status = StatusTitulo.ABERTO;
        this.valorPago = Money.zero();
        this.valorDesconto = Money.zero();
        this.valorJuros = Money.zero();
        this.valorMulta = Money.zero();
    }

    protected Titulo() {
    }

    private static class ValidatedData {
        final TipoTitulo tipo;
        final String descricao;
        final String numeroDocumento;
        final Pessoa pessoa;
        final PlanoContas planoContas;
        final UnidadeNegocio unidadeNegocio;
        final Money valorOriginal;
        final LocalDate dataEmissao;
        final LocalDate dataVencimento;

        ValidatedData(TipoTitulo tipo, String descricao, String numeroDocumento, Pessoa pessoa,
                PlanoContas planoContas, UnidadeNegocio unidadeNegocio, Money valorOriginal,
                LocalDate dataEmissao, LocalDate dataVencimento) {
            this.tipo = tipo;
            this.descricao = descricao;
            this.numeroDocumento = numeroDocumento;
            this.pessoa = pessoa;
            this.planoContas = planoContas;
            this.unidadeNegocio = unidadeNegocio;
            this.valorOriginal = valorOriginal;
            this.dataEmissao = dataEmissao;
            this.dataVencimento = dataVencimento;
        }
    }

    private static ValidatedData validate(TipoTitulo tipo, String descricao, String numeroDocumento,
            Pessoa pessoa, PlanoContas planoContas, UnidadeNegocio unidadeNegocio,
            BigDecimal valorOriginal, LocalDate dataEmissao, LocalDate dataVencimento) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (tipo == null) {
            violations.add(new BeanValidationMessage("tipo", "Tipo do título é obrigatório"));
        }
        if (descricao == null || descricao.isBlank()) {
            violations.add(new BeanValidationMessage("descricao", "Descrição é obrigatória"));
        } else if (descricao.length() > 500) {
            violations.add(new BeanValidationMessage("descricao", "Descrição deve ter no máximo 500 caracteres"));
        }
        if (pessoa == null) {
            violations.add(new BeanValidationMessage("pessoa", "Pessoa é obrigatória"));
        }
        if (planoContas == null) {
            violations.add(new BeanValidationMessage("planoContas", "Plano de contas é obrigatório"));
        }
        if (unidadeNegocio == null) {
            violations.add(new BeanValidationMessage("unidadeNegocio", "Unidade de negócio é obrigatória"));
        }
        if (valorOriginal == null || valorOriginal.compareTo(BigDecimal.ZERO) <= 0) {
            violations.add(new BeanValidationMessage("valorOriginal", "Valor original deve ser maior que zero"));
        }
        if (dataEmissao == null) {
            violations.add(new BeanValidationMessage("dataEmissao", "Data de emissão é obrigatória"));
        }
        if (dataVencimento == null) {
            violations.add(new BeanValidationMessage("dataVencimento", "Data de vencimento é obrigatória"));
        } else if (dataEmissao != null && dataVencimento.isBefore(dataEmissao)) {
            violations.add(new BeanValidationMessage("dataVencimento",
                    "Data de vencimento não pode ser anterior à data de emissão"));
        }

        Money money = ValidationUtils.validateAndGet(() -> Money.of(valorOriginal), violations);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        return new ValidatedData(tipo, descricao, numeroDocumento, pessoa, planoContas,
                unidadeNegocio, money, dataEmissao, dataVencimento);
    }

    public void aplicarDesconto(Money desconto) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (desconto == null || desconto.isNegative()) {
            violations.add(new BeanValidationMessage("valorDesconto", "Desconto deve ser positivo"));
        } else if (desconto.isGreaterThan(valorOriginal)) {
            violations.add(
                    new BeanValidationMessage("valorDesconto", "Desconto não pode ser maior que o valor original"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.valorDesconto = desconto;
    }

    public void aplicarJuros(Money juros) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (juros == null || juros.isNegative()) {
            violations.add(new BeanValidationMessage("valorJuros", "Juros deve ser positivo"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.valorJuros = juros;
    }

    public void aplicarMulta(Money multa) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (multa == null || multa.isNegative()) {
            violations.add(new BeanValidationMessage("valorMulta", "Multa deve ser positiva"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.valorMulta = multa;
    }

    public Money calcularSaldo() {
        return valorOriginal
                .add(valorJuros)
                .add(valorMulta)
                .subtract(valorDesconto)
                .subtract(valorPago);
    }

    public boolean isQuitado() {
        return status == StatusTitulo.PAGO;
    }

    public boolean isVencido() {
        return !isQuitado() && dataVencimento.isBefore(LocalDate.now());
    }

    public boolean isParcelado() {
        return numeroParcela != null && totalParcelas != null && totalParcelas > 1;
    }

    public void definirParcelamento(Integer numeroParcela, Integer totalParcelas, Titulo tituloOrigem) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (numeroParcela == null || numeroParcela <= 0) {
            violations.add(new BeanValidationMessage("numeroParcela", "Número da parcela deve ser maior que zero"));
        }
        if (totalParcelas == null || totalParcelas <= 0) {
            violations.add(new BeanValidationMessage("totalParcelas", "Total de parcelas deve ser maior que zero"));
        }
        if (numeroParcela != null && totalParcelas != null && numeroParcela > totalParcelas) {
            violations.add(
                    new BeanValidationMessage("numeroParcela", "Número da parcela não pode ser maior que o total"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.numeroParcela = numeroParcela;
        this.totalParcelas = totalParcelas;
        this.tituloOrigem = tituloOrigem;
    }

    public void atualizar(String descricao, LocalDate dataVencimento) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (status == StatusTitulo.PAGO || status == StatusTitulo.CANCELADO) {
            violations
                    .add(new BeanValidationMessage("status", "Não é possível alterar título " + status.getDescricao()));
        }

        if (descricao != null && !descricao.isBlank() && descricao.length() > 500) {
            violations.add(new BeanValidationMessage("descricao", "Descrição deve ter no máximo 500 caracteres"));
        }

        if (dataVencimento != null && dataVencimento.isBefore(dataEmissao)) {
            violations.add(new BeanValidationMessage("dataVencimento",
                    "Data de vencimento não pode ser anterior à data de emissão"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        if (descricao != null && !descricao.isBlank()) {
            this.descricao = descricao;
        }
        if (dataVencimento != null) {
            this.dataVencimento = dataVencimento;
        }
    }

    public void atualizarUnidadeNegocio(UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (status == StatusTitulo.PAGO || status == StatusTitulo.CANCELADO) {
            violations
                    .add(new BeanValidationMessage("status", "Não é possível alterar título " + status.getDescricao()));
        }

        if (unidadeNegocio == null) {
            violations.add(new BeanValidationMessage("unidadeNegocio", "Unidade de negócio é obrigatória"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.unidadeNegocio = unidadeNegocio;
    }

    public void cancelar() {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (status == StatusTitulo.PAGO) {
            violations.add(new BeanValidationMessage("status", "Não é possível cancelar título já pago"));
        }
        if (!movimentacoes.isEmpty()) {
            violations.add(
                    new BeanValidationMessage("movimentacoes", "Não é possível cancelar título com movimentações"));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.status = StatusTitulo.CANCELADO;
    }

    public void adicionarObservacao(String observacao) {
        if (this.observacoes == null) {
            this.observacoes = observacao;
        } else {
            this.observacoes += "\n" + observacao;
        }
    }

    // Método interno usado por MovimentacaoFinanceira
    void registrarPagamento(Money valor) {
        this.valorPago = this.valorPago.add(valor);
        atualizarStatus();
    }

    private void atualizarStatus() {
        Money saldo = calcularSaldo();

        if (saldo.isZero()) {
            this.status = StatusTitulo.PAGO;
            this.dataPagamento = LocalDate.now();
        } else if (valorPago.isPositive() && saldo.isPositive()) {
            this.status = StatusTitulo.PARCIAL;
        } else if (isVencido()) {
            this.status = StatusTitulo.VENCIDO;
        } else {
            this.status = StatusTitulo.ABERTO;
        }
    }

    // Getters
    public TipoTitulo getTipo() {
        return tipo;
    }

    public StatusTitulo getStatus() {
        return status;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getDescricao() {
        return descricao;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public PlanoContas getPlanoContas() {
        return planoContas;
    }

    public UnidadeNegocio getUnidadeNegocio() {
        return unidadeNegocio;
    }

    public Money getValorOriginal() {
        return valorOriginal;
    }

    public Money getValorPago() {
        return valorPago;
    }

    public Money getValorDesconto() {
        return valorDesconto;
    }

    public Money getValorJuros() {
        return valorJuros;
    }

    public Money getValorMulta() {
        return valorMulta;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public Integer getNumeroParcela() {
        return numeroParcela;
    }

    public Integer getTotalParcelas() {
        return totalParcelas;
    }

    public Titulo getTituloOrigem() {
        return tituloOrigem;
    }

    public List<MovimentacaoFinanceira> getMovimentacoes() {
        return movimentacoes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Titulo))
            return false;
        if (!super.equals(o))
            return false;
        Titulo titulo = (Titulo) o;
        return Objects.equals(numeroDocumento, titulo.numeroDocumento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), numeroDocumento);
    }

    public static class Builder {
        private TipoTitulo tipo;
        private String descricao;
        private String numeroDocumento;
        private Pessoa pessoa;
        private PlanoContas planoContas;
        private UnidadeNegocio unidadeNegocio;
        private Money valorOriginal;
        private LocalDate dataEmissao;
        private LocalDate dataVencimento;

        public Builder tipo(TipoTitulo tipo) {
            this.tipo = tipo;
            return this;
        }

        public Builder descricao(String descricao) {
            this.descricao = descricao;
            return this;
        }

        public Builder numeroDocumento(String numeroDocumento) {
            this.numeroDocumento = numeroDocumento;
            return this;
        }

        public Builder pessoa(Pessoa pessoa) {
            this.pessoa = pessoa;
            return this;
        }

        public Builder planoContas(PlanoContas planoContas) {
            this.planoContas = planoContas;
            return this;
        }

        public Builder unidadeNegocio(UnidadeNegocio unidadeNegocio) {
            this.unidadeNegocio = unidadeNegocio;
            return this;
        }

        public Builder valorOriginal(Money valorOriginal) {
            this.valorOriginal = valorOriginal;
            return this;
        }

        public Builder dataEmissao(LocalDate dataEmissao) {
            this.dataEmissao = dataEmissao;
            return this;
        }

        public Builder dataVencimento(LocalDate dataVencimento) {
            this.dataVencimento = dataVencimento;
            return this;
        }

        public Titulo build() {
            BigDecimal valorOriginalValue = (this.valorOriginal != null) ? this.valorOriginal.getValue() : null;
            ValidatedData data = validate(this.tipo, this.descricao, this.numeroDocumento,
                    this.pessoa, this.planoContas, this.unidadeNegocio,
                    valorOriginalValue, this.dataEmissao, this.dataVencimento);
            return new Titulo(data.tipo, data.descricao, data.numeroDocumento, data.pessoa,
                    data.planoContas, data.unidadeNegocio, data.valorOriginal,
                    data.dataEmissao, data.dataVencimento);
        }
    }
}
