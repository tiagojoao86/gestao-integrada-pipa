package br.com.grupopipa.gestaointegrada.financeiro.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.entity.UnidadeNegocioFiltravel;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
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
import jakarta.persistence.Transient;

/**
 * Entidade principal do domínio financeiro - representa títulos a pagar/receber
 * (regime de
 * competência)
 */
@Entity
@Table(name = "titulo", indexes = {
    @Index(name = "idx_titulo_tipo_status", columnList = "tipo, status"),
    @Index(name = "idx_titulo_vencimento", columnList = "data_vencimento"),
    @Index(name = "idx_titulo_pessoa", columnList = "pessoa_id"),
// plano_contas removed - index removed
})
public class Titulo extends BaseEntity implements UnidadeNegocioFiltravel {

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
    @JoinColumn(name = "titulo_categoria_id", nullable = false, foreignKey = @ForeignKey(name = "fk_titulo_categoria"))
    private TituloCategoria tituloCategoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_negocio_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_titulo_unidade_negocio"))
    private UnidadeNegocio unidadeNegocio;

    @Embedded
    @AttributeOverride(name = "value",
        column = @Column(name = "valor_original", nullable = false, precision = 15, scale = 2))
    private Money valorOriginal;

    @Embedded
    @AttributeOverride(name = "value",
        column = @Column(name = "valor_desconto", nullable = false, precision = 15, scale = 2))
    private Money valorDesconto;

    @Embedded
    @AttributeOverride(name = "value",
        column = @Column(name = "valor_juros", nullable = false, precision = 15, scale = 2))
    private Money valorJuros;

    @Embedded
    @AttributeOverride(name = "value", column =
        @Column(name = "valor_multa", nullable = false, precision = 15, scale = 2))
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condicao_pagamento_id", foreignKey = @ForeignKey(name = "fk_titulo_condicao_pagamento"))
    private CondicaoPagamento condicaoPagamento;

    @OneToMany(mappedBy = "titulo")
    private Set<MovimentacaoFinanceiraTitulo> movimentacoes = new HashSet<>();

    @OneToMany(mappedBy = "titulo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TituloSetor> setores = new HashSet<>();

    @Column(name = "rateio_automatico", nullable = false)
    private Boolean rateioAutomatico = false;

    private Titulo(
            TipoTitulo tipo,
            String descricao,
            String numeroDocumento,
            Pessoa pessoa,
            TituloCategoria tituloCategoria,
            UnidadeNegocio unidadeNegocio,
            Money valorOriginal,
            LocalDate dataEmissao,
            LocalDate dataVencimento) {
        this.tipo = tipo;
        this.descricao = descricao;
        this.numeroDocumento = numeroDocumento;
        this.pessoa = pessoa;
        this.tituloCategoria = tituloCategoria;
        this.unidadeNegocio = unidadeNegocio;
        this.valorOriginal = valorOriginal;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
        this.status = StatusTitulo.ABERTO;
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
        final TituloCategoria tituloCategoria;
        final UnidadeNegocio unidadeNegocio;
        final Money valorOriginal;
        final LocalDate dataEmissao;
        final LocalDate dataVencimento;

        ValidatedData(
                TipoTitulo tipo,
                String descricao,
                String numeroDocumento,
                Pessoa pessoa,
                TituloCategoria tituloCategoria,
                UnidadeNegocio unidadeNegocio,
                Money valorOriginal,
                LocalDate dataEmissao,
                LocalDate dataVencimento) {
            this.tipo = tipo;
            this.descricao = descricao;
            this.numeroDocumento = numeroDocumento;
            this.pessoa = pessoa;
            this.tituloCategoria = tituloCategoria;
            this.unidadeNegocio = unidadeNegocio;
            this.valorOriginal = valorOriginal;
            this.dataEmissao = dataEmissao;
            this.dataVencimento = dataVencimento;
        }
    }

    private static ValidatedData validate(
            TipoTitulo tipo,
            String descricao,
            String numeroDocumento,
            Pessoa pessoa,
            TituloCategoria tituloCategoria,
            UnidadeNegocio unidadeNegocio,
            BigDecimal valorOriginal,
            LocalDate dataEmissao,
            LocalDate dataVencimento,
            LocalDate dataPagamento) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(tipo, "tipo do título", violations).notNull();
        Validator.of(descricao, "descrição", violations).notBlank().maxLength(500);
        Validator.of(pessoa, "pessoa", violations).notNull();
        Validator.of(tituloCategoria, "categoria", violations).notNull();
        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();
        Validator.of(dataEmissao, "data de emissão", violations).notNull();
        Validator.of(dataVencimento, "data de vencimento", violations).notNull();

        if (dataVencimento != null && dataEmissao != null && dataVencimento.isBefore(dataEmissao)) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.dataVencimentoInvalida",
                    "Data de vencimento não pode ser anterior à data de emissão."));
        }

        if (dataPagamento != null && dataEmissao != null && dataPagamento.isBefore(dataEmissao)) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.dataPagamentoInvalida",
                    "Data de pagamento não pode ser anterior à data de emissão."));
        }

        Money money = ValidationUtils.validateAndGet(
                () -> Money.positive(valorOriginal), violations);

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        return new ValidatedData(
                tipo,
                descricao,
                numeroDocumento,
                pessoa,
                tituloCategoria,
                unidadeNegocio,
                money,
                dataEmissao,
                dataVencimento);
    }

    public void aplicarDesconto(Money desconto) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        // Money.positiveOrZero() já garante >= 0
        Validator.of(desconto, "desconto", violations).notNull();
        if (desconto != null && desconto.isGreaterThan(valorOriginal)) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.descontoAcimaOriginal",
                    "Desconto não pode ser maior que o valor original."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.valorDesconto = desconto;
        atualizarStatus(); // Recalcular status após alterar desconto
    }

    public void aplicarJuros(Money juros) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        // Money.positiveOrZero() já garante >= 0
        Validator.of(juros, "juros", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.valorJuros = juros;
        atualizarStatus(); // Recalcular status após alterar juros
    }

    public void aplicarMulta(Money multa) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        // Money.positiveOrZero() já garante >= 0
        Validator.of(multa, "multa", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.valorMulta = multa;
        atualizarStatus(); // Recalcular status após alterar multa
    }

    /**
     * Calcula o valor pago a partir do somatório das movimentações financeiras não
     * deletadas Campo
     * transiente - não é armazenado no banco de dados
     */
    @Transient
    public Money getValorPago() {
        if (movimentacoes == null || movimentacoes.isEmpty()) {
            return Money.zero();
        }
        return movimentacoes.stream()
                .filter(mt -> !mt.getMovimentacaoFinanceira().isDeleted())
                .map(MovimentacaoFinanceiraTitulo::getValor)
                .reduce(Money.zero(), Money::add);
    }

    public Money calcularSaldo() {
        return valorOriginal
                .add(valorJuros)
                .add(valorMulta)
                .subtract(valorDesconto)
                .subtract(getValorPago());
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

    public boolean isOrigemParcelamento() {
        return totalParcelas != null && totalParcelas > 1
                && numeroParcela == null;
    }

    /**
     * Gera as parcelas filhas a partir da condição de pagamento.
     * O valor original é dividido igualmente entre as parcelas,
     * com a última absorvendo o arredondamento.
     * A data de vencimento de cada parcela é calculada a partir
     * da data de emissão + dias definidos na condição.
     * Marca este título como origem de parcelamento (totalParcelas = N).
     */
    public List<Titulo> gerarParcelas() {
        // Sem condição de pagamento ou parcela única → não é parcelado
        if (condicaoPagamento == null
                || condicaoPagamento.getQuantidadeParcelas() <= 1) {
            return List.of();
        }

        int qtd = condicaoPagamento.getQuantidadeParcelas();
        List<Integer> diasVencimento =
                condicaoPagamento.getDiasVencimento();

        Money valorParcela =
                valorOriginal.divide(BigDecimal.valueOf(qtd));
        Money somaAnterior = Money.zero();

        List<Titulo> parcelas = new ArrayList<>();

        for (int i = 0; i < qtd; i++) {
            Money valorDesta;
            if (i == qtd - 1) {
                valorDesta = valorOriginal.subtract(somaAnterior);
            } else {
                valorDesta = valorParcela;
                somaAnterior = somaAnterior.add(valorParcela);
            }

            LocalDate vencimento =
                    dataEmissao.plusDays(diasVencimento.get(i));

            Titulo parcela = new Titulo.Builder()
                    .tipo(this.tipo)
                    .descricao(this.descricao)
                    .numeroDocumento(this.numeroDocumento)
                    .pessoa(this.pessoa)
                    .tituloCategoria(this.tituloCategoria)
                    .unidadeNegocio(this.unidadeNegocio)
                    .valorOriginal(valorDesta)
                    .dataEmissao(this.dataEmissao)
                    .dataVencimento(vencimento)
                    .rateioAutomatico(this.rateioAutomatico)
                    .build();

            parcela.setCondicaoPagamento(this.condicaoPagamento);
            parcela.definirParcelamento(i + 1, qtd, this);

            for (TituloSetor ts : this.setores) {
                parcela.adicionarSetor(
                        ts.getSetor(), ts.getPercentualRateio());
            }
            parcela.validarSetores();

            parcelas.add(parcela);
        }

        // Mark parent as origin of parcelas
        this.totalParcelas = qtd;

        return parcelas;
    }

    public void definirParcelamento(
            Integer numeroParcela, Integer totalParcelas, Titulo tituloOrigem) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (numeroParcela == null || numeroParcela <= 0) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.numeroParcela",
                    "Número da parcela deve ser maior que zero."));
        }
        if (totalParcelas == null || totalParcelas <= 0) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.totalParcelas",
                    "Total de parcelas deve ser maior que zero."));
        }
        if (numeroParcela != null && totalParcelas != null && numeroParcela > totalParcelas) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.parcelaAcimaTotal",
                    "Número da parcela não pode ser maior que o total."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.numeroParcela = numeroParcela;
        this.totalParcelas = totalParcelas;
        this.tituloOrigem = tituloOrigem;
    }

    public void atualizar(
            String descricao,
            LocalDate dataVencimento,
            BigDecimal valorDesconto,
            BigDecimal valorJuros,
            BigDecimal valorMulta,
            LocalDate dataPagamento,
            String observacoes,
            Boolean rateioAutomatico) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (isOrigemParcelamento()) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.origemParcelamento",
                    "Não é possível alterar título origem de parcelamento."));
        }

        if (!movimentacoes.isEmpty()) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.comMovimentacoes",
                    "Não é possível alterar título com movimentações financeiras."));
        }

        if (status == StatusTitulo.CANCELADO) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.cancelado",
                    "Não é possível alterar título " + status.getDescricao() + "."));
        }

        if (descricao != null && !descricao.isBlank() && descricao.length() > 500) {
            Validator.of(descricao, "descrição", violations).maxLength(500);
        }

        if (dataVencimento != null && dataVencimento.isBefore(dataEmissao)) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.dataVencimentoInvalida",
                    "Data de vencimento não pode ser anterior à data de emissão."));
        }

        if (dataPagamento != null && dataPagamento.isBefore(dataEmissao)) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.dataPagamentoInvalida",
                    "Data de pagamento não pode ser anterior à data de emissão."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        // Atualizar campos se fornecidos
        if (descricao != null && !descricao.isBlank()) {
            this.descricao = descricao;
        }
        if (dataVencimento != null) {
            this.dataVencimento = dataVencimento;
        }
        if (dataPagamento != null) {
            this.dataPagamento = dataPagamento;
        }
        if (observacoes != null && !observacoes.isBlank()) {
            this.observacoes = observacoes;
        }

        // Converter e aplicar valores monetários
        if (valorDesconto != null) {
            aplicarDesconto(Money.positiveOrZero(valorDesconto));
        }
        if (valorJuros != null) {
            aplicarJuros(Money.positiveOrZero(valorJuros));
        }
        if (valorMulta != null) {
            aplicarMulta(Money.positiveOrZero(valorMulta));
        }

        if (rateioAutomatico != null) {
            this.rateioAutomatico = rateioAutomatico;
        }
    }

    public void atualizarUnidadeNegocio(UnidadeNegocio unidadeNegocio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        // Títulos com movimentações financeiras não podem ter unidade de negócio
        // alterada
        if (!movimentacoes.isEmpty()) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.comMovimentacoes",
                    "Não é possível alterar unidade de negócio de título com movimentações financeiras."));
        }

        if (status == StatusTitulo.CANCELADO) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.cancelado",
                    "Não é possível alterar título " + status.getDescricao() + "."));
        }

        Validator.of(unidadeNegocio, "unidade de negócio", violations).notNull();

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        this.unidadeNegocio = unidadeNegocio;
    }

    public void cancelar() {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (status == StatusTitulo.PAGO) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.cancelarPago",
                    "Não é possível cancelar título já pago."));
        }
        if (!movimentacoes.isEmpty()) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.cancelarComMovimentacoes",
                    "Não é possível cancelar título com movimentações."));
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

    /**
     * Método interno usado por MovimentacaoFinanceira para atualizar status após
     * pagamento Como
     * valorPago agora é calculado a partir das movimentações, apenas atualiza o
     * status
     */
    void registrarPagamento(Money valor) {
        atualizarStatus();
    }

    /**
     * Método público usado ao deletar uma MovimentacaoFinanceira para reverter
     * o pagamento e atualizar o status do título
     */
    public void reverterPagamento() {
        atualizarStatus();

        // Se o título estava pago e agora não está mais, limpar a data de pagamento
        if (this.status != StatusTitulo.PAGO && this.dataPagamento != null) {
            this.dataPagamento = null;
        }
    }

    private void atualizarStatus() {
        Money saldo = calcularSaldo();
        Money valorPago = getValorPago();

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

    public TituloCategoria getTituloCategoria() {
        return tituloCategoria;
    }

    public UnidadeNegocio getUnidadeNegocio() {
        return unidadeNegocio;
    }

    public Money getValorOriginal() {
        return valorOriginal;
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

    public CondicaoPagamento getCondicaoPagamento() {
        return condicaoPagamento;
    }

    public void setCondicaoPagamento(CondicaoPagamento condicaoPagamento) {
        this.condicaoPagamento = condicaoPagamento;
    }

    public Set<MovimentacaoFinanceiraTitulo> getMovimentacoes() {
        return movimentacoes;
    }

    public Set<TituloSetor> getSetores() {
        return setores;
    }

    public Boolean getRateioAutomatico() {
        return rateioAutomatico;
    }

    public void setRateioAutomatico(Boolean rateioAutomatico) {
        this.rateioAutomatico = rateioAutomatico != null ? rateioAutomatico : false;
    }

    public void adicionarSetor(
            br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor setor,
            BigDecimal percentualRateio) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        Validator.of(setor, "setor", violations).notNull();

        if (percentualRateio == null || percentualRateio.compareTo(BigDecimal.ZERO) <= 0) {
            violations.add(new BeanValidationMessage(
                    "validation.tituloSetor.percentualZero",
                    "Percentual de rateio deve ser maior que zero."));
        }

        if (percentualRateio != null && percentualRateio.compareTo(new BigDecimal("100")) > 0) {
            violations.add(new BeanValidationMessage(
                    "validation.tituloSetor.percentualMaximo",
                    "Percentual de rateio não pode ser maior que 100."));
        }

        // Note: Removed duplicate sector validation as we clear sectors before adding
        // in update flow
        // The validation would fail when updating because Hibernate hasn't flushed the
        // delete yet

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }

        TituloSetor tituloSetor = new TituloSetor.Builder()
                .titulo(this)
                .setor(setor)
                .percentualRateio(percentualRateio)
                .build();

        this.setores.add(tituloSetor);
    }

    public void removerSetor(br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor setor) {
        setores.removeIf(ts -> ts.getSetor().equals(setor));
    }

    public void limparSetores() {
        // Remove all elements to trigger orphanRemoval
        // Using removeIf ensures proper Hibernate collection tracking
        setores.removeIf(s -> true);
    }

    public void validarSetores() {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (setores.isEmpty()) {
            violations.add(new BeanValidationMessage(
                    "validation.titulo.setoresObrigatorio",
                    "Pelo menos um setor deve ser vinculado ao título."));
        }

        if (!setores.isEmpty()) {
            BigDecimal somaPercentuais = setores.stream()
                    .map(TituloSetor::getPercentualRateio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (somaPercentuais.compareTo(new BigDecimal("100")) != 0) {
                violations.add(new BeanValidationMessage(
                        "validation.titulo.percentualTotal",
                        "A soma dos percentuais deve ser exatamente 100%."));
            }
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("titulo", violations);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Titulo)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
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
        private TituloCategoria tituloCategoria;
        private UnidadeNegocio unidadeNegocio;
        private BigDecimal valorOriginal;
        private LocalDate dataEmissao;
        private LocalDate dataVencimento;
        private LocalDate dataPagamento;
        private BigDecimal valorDesconto;
        private BigDecimal valorJuros;
        private BigDecimal valorMulta;
        private Boolean rateioAutomatico;

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

        public Builder tituloCategoria(TituloCategoria tituloCategoria) {
            this.tituloCategoria = tituloCategoria;
            return this;
        }

        public Builder unidadeNegocio(UnidadeNegocio unidadeNegocio) {
            this.unidadeNegocio = unidadeNegocio;
            return this;
        }

        // Compatibility shim: some tests (and older code) may call `.planoContas(...)`
        // on the
        // builder even after the campo 'planoContas' was removed from the entity. Keep
        // a no-op
        // method to preserve binary/source compatibility for tests while the codebase
        // is updated.
        public Builder planoContas(
                br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas ignored) {
            return this;
        }

        public Builder valorOriginal(BigDecimal valorOriginal) {
            this.valorOriginal = valorOriginal;
            return this;
        }

        public Builder valorOriginal(Money valorOriginal) {
            this.valorOriginal = valorOriginal != null
                    ? valorOriginal.getValue() : null;
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

        public Builder dataPagamento(LocalDate dataPagamento) {
            this.dataPagamento = dataPagamento;
            return this;
        }

        public Builder valorDesconto(BigDecimal valorDesconto) {
            this.valorDesconto = valorDesconto;
            return this;
        }

        public Builder valorJuros(BigDecimal valorJuros) {
            this.valorJuros = valorJuros;
            return this;
        }

        public Builder valorMulta(BigDecimal valorMulta) {
            this.valorMulta = valorMulta;
            return this;
        }

        public Builder rateioAutomatico(Boolean rateioAutomatico) {
            this.rateioAutomatico = rateioAutomatico;
            return this;
        }

        public Titulo build() {
            ValidatedData data = validate(
                    this.tipo,
                    this.descricao,
                    this.numeroDocumento,
                    this.pessoa,
                    this.tituloCategoria,
                    this.unidadeNegocio,
                    this.valorOriginal,
                    this.dataEmissao,
                    this.dataVencimento,
                    this.dataPagamento);
            Titulo titulo = new Titulo(
                    data.tipo,
                    data.descricao,
                    data.numeroDocumento,
                    data.pessoa,
                    data.tituloCategoria,
                    data.unidadeNegocio,
                    data.valorOriginal,
                    data.dataEmissao,
                    data.dataVencimento);
            // Definir dataPagamento se fornecida (já foi validada)
            if (this.dataPagamento != null) {
                titulo.dataPagamento = this.dataPagamento;
            }
            // Aplicar desconto, juros, multa se fornecidos
            if (this.valorDesconto != null
                    && this.valorDesconto.compareTo(BigDecimal.ZERO) > 0) {
                titulo.aplicarDesconto(
                        Money.positiveOrZero(this.valorDesconto));
            }
            if (this.valorJuros != null
                    && this.valorJuros.compareTo(BigDecimal.ZERO) > 0) {
                titulo.aplicarJuros(
                        Money.positiveOrZero(this.valorJuros));
            }
            if (this.valorMulta != null
                    && this.valorMulta.compareTo(BigDecimal.ZERO) > 0) {
                titulo.aplicarMulta(
                        Money.positiveOrZero(this.valorMulta));
            }
            titulo.setRateioAutomatico(this.rateioAutomatico);
            return titulo;
        }
    }
}
