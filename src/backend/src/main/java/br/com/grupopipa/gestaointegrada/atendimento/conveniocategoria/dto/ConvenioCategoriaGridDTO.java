package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ConvenioCategoriaGridDTO implements GridDTO {
    private UUID id;
    private String convenioNome;
    private String nome;
    private String codigoAnsPlano;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private Boolean deleted;
}
