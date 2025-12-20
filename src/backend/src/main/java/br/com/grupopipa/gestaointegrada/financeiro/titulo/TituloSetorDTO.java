package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TituloSetorDTO {
    private UUID setorId;
    private String setorNome;
    private BigDecimal percentualRateio;
}
