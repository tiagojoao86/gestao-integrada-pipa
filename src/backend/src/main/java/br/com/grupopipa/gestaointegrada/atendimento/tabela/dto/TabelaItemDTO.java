package br.com.grupopipa.gestaointegrada.atendimento.tabela.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TabelaItemDTO implements DTO {

    private UUID id;
    private UUID tabelaId;
    private UUID procedimentoId;
    private String procedimentoCodigo;
    private String procedimentoDescricao;
    private BigDecimal valor;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
}
