package br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResolverProcedimentoResponse {
    private UUID tabelaItemId;
    private BigDecimal valor;
}
