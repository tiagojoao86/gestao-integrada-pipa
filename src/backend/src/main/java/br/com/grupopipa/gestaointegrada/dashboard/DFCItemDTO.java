package br.com.grupopipa.gestaointegrada.dashboard;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DFCItemDTO {

    private String mes;
    private BigDecimal entradas;
    private BigDecimal saidas;
}
