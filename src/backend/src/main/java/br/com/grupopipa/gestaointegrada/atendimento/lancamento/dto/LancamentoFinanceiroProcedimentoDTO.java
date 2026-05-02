package br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LancamentoFinanceiroProcedimentoDTO {

    private UUID id;
    private UUID procedimentoId;
    private String procedimentoCodigo;
    private String procedimentoDescricao;
    private UUID convenioId;
    private String convenioNome;
    private UUID tabelaItemId;
    private BigDecimal valor;
}
