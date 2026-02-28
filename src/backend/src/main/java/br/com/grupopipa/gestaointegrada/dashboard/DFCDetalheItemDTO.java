package br.com.grupopipa.gestaointegrada.dashboard;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DFCDetalheItemDTO {

    private String mes;
    private String tipo;
    private String agrupadorId;
    private String agrupadorNome;
    private String agrupadorCodigo;
    private String categoriaId;
    private String categoriaNome;
    private String categoriaCodigo;
    private boolean temAgrupador;
    private BigDecimal total;
}
