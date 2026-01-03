package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlanoContasDTO implements DTO {

    private UUID id;
    private String codigo;
    private String descricao;
    private String tipo; // RECEITA, DESPESA, ATIVO, PASSIVO
    private UUID planoPaiId;
    private String planoPaiDescricao; // Para exibição
    private UUID unidadeNegocioId;
    private String unidadeNegocioNome; // Para exibição
    private Boolean ativo;
    private Boolean analitico; // Calculado
    private Integer nivel; // Calculado
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
