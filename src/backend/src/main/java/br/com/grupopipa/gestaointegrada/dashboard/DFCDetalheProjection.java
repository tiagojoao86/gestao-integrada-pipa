package br.com.grupopipa.gestaointegrada.dashboard;

import java.math.BigDecimal;

public interface DFCDetalheProjection {

    String getMes();

    String getTipo();

    String getAgrupadorId();

    String getAgrupadorNome();

    String getAgrupadorCodigo();

    String getCategoriaId();

    String getCategoriaNome();

    String getCategoriaCodigo();

    Boolean getTemAgrupador();

    BigDecimal getTotal();
}
