package br.com.grupopipa.gestaointegrada.financeiro.centrocusto;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CentroCustoDTO implements DTO {
    private UUID id;
    private String nome;
    private Boolean centroResultado;
    private UUID unidadeNegocioId;
    private String unidadeNegocioNome;
    private String unidadeNegocioCodigo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
