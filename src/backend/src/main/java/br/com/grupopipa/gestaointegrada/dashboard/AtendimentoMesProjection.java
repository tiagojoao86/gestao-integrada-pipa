package br.com.grupopipa.gestaointegrada.dashboard;

/**
 * Projeção para a query de atendimentos agrupados por mês.
 */
public interface AtendimentoMesProjection {

    String getMes();

    Long getTotal();
}
