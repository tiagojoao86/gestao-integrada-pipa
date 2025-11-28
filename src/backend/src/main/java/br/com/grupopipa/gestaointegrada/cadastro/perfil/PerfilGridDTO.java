package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PerfilGridDTO implements GridDTO {
    private UUID id;
    private String nome;
    private LocalDateTime createdAt;
}
