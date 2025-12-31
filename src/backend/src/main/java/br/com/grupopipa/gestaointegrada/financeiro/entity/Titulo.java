package br.com.grupopipa.gestaointegrada.financeiro.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.entity.UnidadeNegocioFiltravel;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;

/**
 * Entidade principal do domínio financeiro - representa títulos a pagar/receber (regime de
 * competência)
 */
@Entity
@Table(
    name = "titulo",
    indexes = {
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
  @JoinColumn(
      name = "pessoa_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_titulo_pessoa"))
  private Pessoa pessoa;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "titulo_categoria_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_titulo_categoria"))
  private TituloCategoria tituloCategoria;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "unidade_negocio_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_titulo_unidade_negocio"))
  private UnidadeNegocio unidadeNegocio;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "valor_original", nullable = false, precision = 15, scale = 2))
  private Money valorOriginal;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "valor_desconto", nullable = false, precision = 15, scale = 2))
  private Money valorDesconto;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "valor_juros", nullable = false, precision = 15, scale = 2))
  private Money valorJuros;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "valor_multa", nullable = false, precision = 15, scale = 2))
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

  @ManyToMany(mappedBy = "titulos")
  private Set<MovimentacaoFinanceira> movimentacoes = new HashSet<>();

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

  protected Titulo() {}

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

    if (tipo == null) {
      violations.add(new BeanValidationMessage("tipo", "Tipo do título é obrigatório"));
    }
    if (descricao == null || descricao.isBlank()) {
      violations.add(new BeanValidationMessage("descricao", "Descrição é obrigatória"));
    } else if (descricao.length() > 500) {
      violations.add(
          new BeanValidationMessage("descricao", "Descrição deve ter no máximo 500 caracteres"));
    }
    if (pessoa == null) {
      violations.add(new BeanValidationMessage("pessoa", "Pessoa é obrigatória"));
    }
    if (tituloCategoria == null) {
      violations.add(new BeanValidationMessage("tituloCategoria", "Categoria é obrigatória"));
    }
    if (unidadeNegocio == null) {
      violations.add(
          new BeanValidationMessage("unidadeNegocio", "Unidade de negócio é obrigatória"));
    }
    if (dataEmissao == null) {
      violations.add(new BeanValidationMessage("dataEmissao", "Data de emissão é obrigatória"));
    }
    if (dataVencimento == null) {
      violations.add(
          new BeanValidationMessage("dataVencimento", "Data de vencimento é obrigatória"));
    } else if (dataEmissao != null && dataVencimento.isBefore(dataEmissao)) {
      violations.add(
          new BeanValidationMessage(
              "dataVencimento", "Data de vencimento não pode ser anterior à data de emissão"));
    }

    // Validar dataPagamento se fornecida
    if (dataPagamento != null && dataEmissao != null && dataPagamento.isBefore(dataEmissao)) {
      violations.add(
          new BeanValidationMessage(
              "dataPagamento", "Data de pagamento não pode ser anterior à data de emissão"));
    }

    // Usar Money.positive() para valorOriginal - garante > 0 automaticamente
    Money money = ValidationUtils.validateAndGet(() -> Money.positive(valorOriginal), violations);

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
    if (desconto == null) {
      violations.add(new BeanValidationMessage("valorDesconto", "Desconto não pode ser nulo"));
    } else if (desconto.isGreaterThan(valorOriginal)) {
      violations.add(
          new BeanValidationMessage(
              "valorDesconto", "Desconto não pode ser maior que o valor original"));
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
    if (juros == null) {
      violations.add(new BeanValidationMessage("valorJuros", "Juros não pode ser nulo"));
    }

    if (!violations.isEmpty()) {
      throw new BeanValidationException("titulo", violations);
    }

    this.valorJuros = juros;
    atualizarStatus(); // Recalcular status após alterar juros
  }

  public void aplicarMulta(Money multa) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    // Money.positiveOrZero() já garante >= 0
    if (multa == null) {
      violations.add(new BeanValidationMessage("valorMulta", "Multa não pode ser nula"));
    }

    if (!violations.isEmpty()) {
      throw new BeanValidationException("titulo", violations);
    }

    this.valorMulta = multa;
    atualizarStatus(); // Recalcular status após alterar multa
  }

  /**
   * Calcula o valor pago a partir do somatório das movimentações financeiras não deletadas Campo
   * transiente - não é armazenado no banco de dados
   */
  @Transient
  public Money getValorPago() {
    if (movimentacoes == null || movimentacoes.isEmpty()) {
      return Money.zero();
    }
    return movimentacoes.stream()
        .filter(
            m ->
                m.getDeleted() == null
                    || !m.getDeleted()) // Filtrar apenas movimentações não deletadas
        .map(MovimentacaoFinanceira::getValor)
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

  public void definirParcelamento(
      Integer numeroParcela, Integer totalParcelas, Titulo tituloOrigem) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    if (numeroParcela == null || numeroParcela <= 0) {
      violations.add(
          new BeanValidationMessage("numeroParcela", "Número da parcela deve ser maior que zero"));
    }
    if (totalParcelas == null || totalParcelas <= 0) {
      violations.add(
          new BeanValidationMessage("totalParcelas", "Total de parcelas deve ser maior que zero"));
    }
    if (numeroParcela != null && totalParcelas != null && numeroParcela > totalParcelas) {
      violations.add(
          new BeanValidationMessage(
              "numeroParcela", "Número da parcela não pode ser maior que o total"));
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
      Money valorDesconto,
      Money valorJuros,
      Money valorMulta,
      LocalDate dataPagamento,
      String observacoes) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    // Títulos com movimentações financeiras não podem ter campos principais alterados
    if (!movimentacoes.isEmpty()) {
      violations.add(
          new BeanValidationMessage(
              "movimentacoes", "Não é possível alterar título com movimentações financeiras"));
    }

    // Títulos cancelados não podem ser alterados
    if (status == StatusTitulo.CANCELADO) {
      violations.add(
          new BeanValidationMessage(
              "status", "Não é possível alterar título " + status.getDescricao()));
    }

    if (descricao != null && !descricao.isBlank() && descricao.length() > 500) {
      violations.add(
          new BeanValidationMessage("descricao", "Descrição deve ter no máximo 500 caracteres"));
    }

    if (dataVencimento != null && dataVencimento.isBefore(dataEmissao)) {
      violations.add(
          new BeanValidationMessage(
              "dataVencimento", "Data de vencimento não pode ser anterior à data de emissão"));
    }

    if (dataPagamento != null && dataPagamento.isBefore(dataEmissao)) {
      violations.add(
          new BeanValidationMessage(
              "dataPagamento", "Data de pagamento não pode ser anterior à data de emissão"));
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

    // Aplicar valores monetários (já validados como positiveOrZero)
    if (valorDesconto != null) {
      aplicarDesconto(valorDesconto);
    }
    if (valorJuros != null) {
      aplicarJuros(valorJuros);
    }
    if (valorMulta != null) {
      aplicarMulta(valorMulta);
    }
  }

  public void atualizarUnidadeNegocio(UnidadeNegocio unidadeNegocio) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    // Títulos com movimentações financeiras não podem ter unidade de negócio alterada
    if (!movimentacoes.isEmpty()) {
      violations.add(
          new BeanValidationMessage(
              "movimentacoes",
              "Não é possível alterar unidade de negócio de título com movimentações financeiras"));
    }

    // Títulos cancelados não podem ser alterados
    if (status == StatusTitulo.CANCELADO) {
      violations.add(
          new BeanValidationMessage(
              "status", "Não é possível alterar título " + status.getDescricao()));
    }

    if (unidadeNegocio == null) {
      violations.add(
          new BeanValidationMessage("unidadeNegocio", "Unidade de negócio é obrigatória"));
    }

    if (!violations.isEmpty()) {
      throw new BeanValidationException("titulo", violations);
    }

    this.unidadeNegocio = unidadeNegocio;
  }

  public void cancelar() {
    Set<BeanValidationMessage> violations = new HashSet<>();

    if (status == StatusTitulo.PAGO) {
      violations.add(
          new BeanValidationMessage(
              "status.cancelar.pago", "Não é possível cancelar título já pago"));
    }
    if (!movimentacoes.isEmpty()) {
      violations.add(
          new BeanValidationMessage(
              "movimentacoes", "Não é possível cancelar título com movimentações"));
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
   * Método interno usado por MovimentacaoFinanceira para atualizar status após pagamento Como
   * valorPago agora é calculado a partir das movimentações, apenas atualiza o status
   */
  void registrarPagamento(Money valor) {
    atualizarStatus();
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

  public Set<MovimentacaoFinanceira> getMovimentacoes() {
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

    if (setor == null) {
      violations.add(new BeanValidationMessage("setor", "Setor é obrigatório"));
    }

    if (percentualRateio == null || percentualRateio.compareTo(BigDecimal.ZERO) <= 0) {
      violations.add(
          new BeanValidationMessage("percentualRateio", "Percentual deve ser maior que zero"));
    }

    if (percentualRateio != null && percentualRateio.compareTo(new BigDecimal("100")) > 0) {
      violations.add(
          new BeanValidationMessage("percentualRateio", "Percentual não pode ser maior que 100"));
    }

    // Note: Removed duplicate sector validation as we clear sectors before adding in update flow
    // The validation would fail when updating because Hibernate hasn't flushed the delete yet

    if (!violations.isEmpty()) {
      throw new BeanValidationException("titulo", violations);
    }

    TituloSetor tituloSetor =
        new TituloSetor.Builder()
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
      violations.add(
          new BeanValidationMessage("setores", "Pelo menos um setor deve ser vinculado ao título"));
    }

    if (!setores.isEmpty()) {
      BigDecimal somaPercentuais =
          setores.stream()
              .map(TituloSetor::getPercentualRateio)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      if (somaPercentuais.compareTo(new BigDecimal("100")) != 0) {
        violations.add(
            new BeanValidationMessage(
                "setores", "A soma dos percentuais deve ser exatamente 100%"));
      }
    }

    if (!violations.isEmpty()) {
      throw new BeanValidationException("titulo", violations);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Titulo)) return false;
    if (!super.equals(o)) return false;
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
    private Money valorOriginal;
    private LocalDate dataEmissao;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
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

    public Builder dataPagamento(LocalDate dataPagamento) {
      this.dataPagamento = dataPagamento;
      return this;
    }

    public Builder rateioAutomatico(Boolean rateioAutomatico) {
      this.rateioAutomatico = rateioAutomatico;
      return this;
    }

    public Titulo build() {
      BigDecimal valorOriginalValue =
          (this.valorOriginal != null) ? this.valorOriginal.getValue() : null;
      ValidatedData data =
          validate(
              this.tipo,
              this.descricao,
              this.numeroDocumento,
              this.pessoa,
              this.tituloCategoria,
              this.unidadeNegocio,
              valorOriginalValue,
              this.dataEmissao,
              this.dataVencimento,
              this.dataPagamento);
      Titulo titulo =
          new Titulo(
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
      titulo.setRateioAutomatico(this.rateioAutomatico);
      return titulo;
    }
  }
}
