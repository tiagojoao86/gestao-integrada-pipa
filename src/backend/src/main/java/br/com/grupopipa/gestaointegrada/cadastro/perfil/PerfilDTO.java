package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PerfilDTO implements DTO {
    private UUID id;
    private String nome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<PerfilModuloDTO> permissoes;
}
