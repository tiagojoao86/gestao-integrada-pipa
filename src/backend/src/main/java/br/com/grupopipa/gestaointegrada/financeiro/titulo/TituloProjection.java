package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;

/**
 * Projeção para consultas otimizadas de Titulo com cálculo de valorPago via SUM de movimentações
 * Evita N+1 queries ao buscar lista de títulos
 */
public interface TituloProjection {
  UUID getId();

  TipoTitulo getTipo();

  StatusTitulo getStatus();

  String getNumeroDocumento();

  String getDescricao();

  // Pessoa
  UUID getPessoaId();

  String getPessoaNome();

  // Categoria
  UUID getTituloCategoriaId();

  String getTituloCategoriaNome();

  // Unidade Negócio
  UUID getUnidadeNegocioId();

  String getUnidadeNegocioCodigo();

  String getUnidadeNegocioNome();

  // Valores monetários
  BigDecimal getValorOriginal();

  BigDecimal getValorDesconto();

  BigDecimal getValorJuros();

  BigDecimal getValorMulta();

  /**
   * Valor pago calculado via SUM das movimentações financeiras Retorna 0 se não houver
   * movimentações
   */
  BigDecimal getValorPago();

  // Datas
  LocalDate getDataEmissao();

  LocalDate getDataVencimento();

  LocalDate getDataPagamento();

  // Parcelamento
  Integer getNumeroParcela();

  Integer getTotalParcelas();

  // Auditoria
  Boolean getDeleted();
}
