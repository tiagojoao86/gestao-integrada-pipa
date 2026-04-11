package br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AtendimentoProcedimentoDTO {

    private UUID id;
    private UUID procedimentoId;
    private String procedimentoCodigo;
    private String procedimentoDescricao;
    private UUID tabelaItemId;
    private BigDecimal tabelaItemValor;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
}
