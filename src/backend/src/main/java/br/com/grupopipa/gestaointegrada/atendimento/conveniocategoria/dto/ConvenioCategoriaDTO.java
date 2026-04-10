package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConvenioCategoriaDTO implements DTO {

    private UUID id;

    private UUID convenioId;
    private String convenioNome;

    private String nome;
    private String codigoAnsPlano;
    private Boolean ativo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
