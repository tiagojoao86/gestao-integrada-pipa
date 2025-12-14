package br.com.grupopipa.gestaointegrada.financeiro.categoria;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CategoriaTituloDTO implements DTO {

    private UUID id;
    private String nome;
    private String descricao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
