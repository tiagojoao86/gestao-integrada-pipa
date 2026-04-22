package br.com.grupopipa.gestaointegrada.dashboard;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de saída para o dashboard de atendimentos por mês.
 */
@Data
@Builder
public class AtendimentoMesItemDTO {

    private String mes;
    private Long total;
}
