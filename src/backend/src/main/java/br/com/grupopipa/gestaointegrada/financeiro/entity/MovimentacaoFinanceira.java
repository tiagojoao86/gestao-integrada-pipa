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
import jakarta.persistence.Table;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.entity.UnidadeNegocioFiltravel;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;

/**
 * Entidade para MovimentacaoFinanceira - representa o dinheiro real no caixa/banco (regime de
 * caixa)
 */
@Entity
@Table(
    name = "movimentacao_financeira",
    indexes = {
      @Index(name = "idx_movimentacao_data", columnList = "data"),
      @Index(name = "idx_movimentacao_titulo", columnList = "titulo_id"),
      @Index(name = "idx_movimentacao_conta", columnList = "conta_bancaria_id")
    })
public class MovimentacaoFinanceira extends BaseEntity implements UnidadeNegocioFiltravel {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "unidade_negocio_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_movimentacao_unidade_negocio"))
  private UnidadeNegocio unidadeNegocio;

  @ManyToMany
  @JoinTable(
      name = "movimentacao_financeira_titulo",
      joinColumns =
          @JoinColumn(
              name = "movimentacao_financeira_id",
              foreignKey = @ForeignKey(name = "fk_movimentacao_titulo_mov")),
      inverseJoinColumns =
          @JoinColumn(
              name = "titulo_id",
              foreignKey = @ForeignKey(name = "fk_movimentacao_titulo_tit")))
  private Set<Titulo> titulos = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "conta_bancaria_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_movimentacao_conta"))
  private ContaBancaria contaBancaria;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo", nullable = false, length = 20)
  private TipoMovimentacao tipo;

  @Enumerated(EnumType.STRING)
  @Column(name = "forma_pagamento", nullable = false, length = 20)
  private FormaPagamento formaPagamento;

  @Embedded
  @AttributeOverride(
      name = "value",
      column = @Column(name = "valor", nullable = false, precision = 15, scale = 2))
  private Money valor;

  @Column(name = "data", nullable = false)
  private LocalDate data;

  @Column(name = "observacoes", columnDefinition = "TEXT")
  private String observacoes;

  private MovimentacaoFinanceira(
      Set<Titulo> titulos,
      ContaBancaria contaBancaria,
      TipoMovimentacao tipo,
      FormaPagamento formaPagamento,
      Money valor,
      LocalDate data,
      UnidadeNegocio unidadeNegocio) {
    this.titulos = titulos;
    this.contaBancaria = contaBancaria;
    this.tipo = tipo;
    this.formaPagamento = formaPagamento;
    this.valor = valor;
    this.data = data;
    this.unidadeNegocio = unidadeNegocio;
    // Sincronizar relacionamento bidirecional: adicionar esta movimentação ao Set de movimentações
    // de cada título
    // Isso garante que getValorPago() em Titulo consiga calcular corretamente
    if (titulos != null) {
      titulos.forEach(
          t -> {
            t.getMovimentacoes().add(this);
            t.registrarPagamento(valor); // Atualiza o status do título
          });
    }
  }

  protected MovimentacaoFinanceira() {}

  private static class ValidatedData {
    final Set<Titulo> titulos;
    final ContaBancaria contaBancaria;
    final TipoMovimentacao tipo;
    final FormaPagamento formaPagamento;
    final Money valor;
    final LocalDate data;
    final UnidadeNegocio unidadeNegocio;

    ValidatedData(
        Set<Titulo> titulos,
        ContaBancaria contaBancaria,
        TipoMovimentacao tipo,
        FormaPagamento formaPagamento,
        Money valor,
        LocalDate data,
        UnidadeNegocio unidadeNegocio) {
      this.titulos = titulos;
      this.contaBancaria = contaBancaria;
      this.tipo = tipo;
      this.formaPagamento = formaPagamento;
      this.valor = valor;
      this.data = data;
      this.unidadeNegocio = unidadeNegocio;
    }
  }

  private static ValidatedData validate(
      Set<Titulo> titulos,
      ContaBancaria contaBancaria,
      TipoMovimentacao tipo,
      FormaPagamento formaPagamento,
      BigDecimal valor,
      LocalDate data,
      UnidadeNegocio unidadeNegocio) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    if (titulos == null || titulos.isEmpty()) {
      violations.add(new BeanValidationMessage("titulos", "Pelo menos um título é obrigatório"));
    }
    if (contaBancaria == null) {
      violations.add(new BeanValidationMessage("contaBancaria", "Conta bancária é obrigatória"));
    }
    if (tipo == null) {
      violations.add(new BeanValidationMessage("tipo", "Tipo de movimentação é obrigatório"));
    }
    if (formaPagamento == null) {
      violations.add(
          new BeanValidationMessage("formaPagamento", "Forma de pagamento é obrigatória"));
    }
    if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
      violations.add(new BeanValidationMessage("valor", "Valor deve ser maior que zero"));
    }
    if (data == null) {
      violations.add(new BeanValidationMessage("data", "Data é obrigatória"));
    }
    if (unidadeNegocio == null) {
      violations.add(
          new BeanValidationMessage("unidadeNegocio", "Unidade de negócio é obrigatória"));
    }

    Money money = ValidationUtils.validateAndGet(() -> Money.of(valor), violations);

    // Validar regras de negócio para cada título
    if (titulos != null && money != null) {
      for (Titulo titulo : titulos) {
        if (!titulo.getStatus().permiteMovimentacao()) {
          violations.add(
              new BeanValidationMessage(
                  "titulo.status",
                  "Não é possível criar movimentação para título "
                      + titulo.getStatus().getDescricao()));
        }

        // Calcular o valor total que o título pode receber (valor original + juros + multa -
        // desconto)
        Money valorTotal =
            titulo
                .getValorOriginal()
                .add(titulo.getValorJuros())
                .add(titulo.getValorMulta())
                .subtract(titulo.getValorDesconto());

        // Calcular o valor que será pago após esta movimentação
        Money valorPagoAtual = titulo.getValorPago();
        Money valorPagoAposMovimentacao = valorPagoAtual.add(money);

        // Verificar se o valor pago ultrapassaria o valor total do título
        if (valorPagoAposMovimentacao.isGreaterThan(valorTotal)) {
          violations.add(
              new BeanValidationMessage(
                  "valor.valorPagoUltrapassaTotal",
                  "Valor pago após a movimentação ("
                      + valorPagoAposMovimentacao
                      + ") ultrapassaria o valor total do título ("
                      + valorTotal
                      + "). Valor já pago: "
                      + valorPagoAtual
                      + ", valor da movimentação: "
                      + money));
        }

        // Também manter a verificação de saldo para melhor UX
        Money saldoTitulo = titulo.calcularSaldo();
        if (money.isGreaterThan(saldoTitulo)) {
          violations.add(
              new BeanValidationMessage(
                  "valor.valorMovimentoMaiorSaldo",
                  "Valor da movimentação ("
                      + money
                      + ") excede o saldo do título ("
                      + saldoTitulo
                      + ")"));
        }
      }
    }

    if (!violations.isEmpty()) {
      throw new BeanValidationException("movimentacaoFinanceira", violations);
    }

    return new ValidatedData(
        titulos, contaBancaria, tipo, formaPagamento, money, data, unidadeNegocio);
  }

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
  public Set<Titulo> getTitulos() {
    return titulos;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MovimentacaoFinanceira)) return false;
    if (!super.equals(o)) return false;
    MovimentacaoFinanceira that = (MovimentacaoFinanceira) o;
    return Objects.equals(titulos, that.titulos)
        && Objects.equals(data, that.data)
        && Objects.equals(valor, that.valor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), titulos, data, valor);
  }

  public static class Builder {
    private Set<Titulo> titulos = new HashSet<>();
    private ContaBancaria contaBancaria;
    private TipoMovimentacao tipo;
    private FormaPagamento formaPagamento;
    private Money valor;
    private LocalDate data;
    private UnidadeNegocio unidadeNegocio;

    public Builder titulos(Set<Titulo> titulos) {
      this.titulos = titulos;
      return this;
    }

    public Builder addTitulo(Titulo titulo) {
      this.titulos.add(titulo);
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

    public MovimentacaoFinanceira build() {
      // Inferir unidadeNegocio a partir da contaBancaria se não fornecida
      // explicitamente
      if (this.unidadeNegocio == null && this.contaBancaria != null) {
        this.unidadeNegocio = this.contaBancaria.getUnidadeNegocio();
      }

      BigDecimal valorValue = (this.valor != null) ? this.valor.getValue() : null;
      ValidatedData data =
          validate(
              this.titulos,
              this.contaBancaria,
              this.tipo,
              this.formaPagamento,
              valorValue,
              this.data,
              this.unidadeNegocio);
      return new MovimentacaoFinanceira(
          data.titulos,
          data.contaBancaria,
          data.tipo,
          data.formaPagamento,
          data.valor,
          data.data,
          data.unidadeNegocio);
    }
  }
}
