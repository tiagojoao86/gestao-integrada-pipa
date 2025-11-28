package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilDTO;
import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UsuarioDTO implements DTO {

    private UUID id;
    private String nome;
    private String login;
    private String senha;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private List<PerfilDTO> perfis;

}
