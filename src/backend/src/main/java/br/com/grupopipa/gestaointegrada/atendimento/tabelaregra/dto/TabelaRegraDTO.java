package br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TabelaRegraDTO implements DTO {
    private UUID id;
    private UUID convenioId;
    private String convenioNome;
    private UUID convenioCategoriaId;
    private String convenioCategoriaNome;
    private UUID tabelaId;
    private String tabelaNome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
