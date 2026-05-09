package br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TabelaRegraGridDTO implements GridDTO {
    private UUID id;
    private String convenioNome;
    private String convenioCategoriaNome;
    private String tabelaNome;
    private LocalDateTime createdAt;
    private Boolean deleted;
}
