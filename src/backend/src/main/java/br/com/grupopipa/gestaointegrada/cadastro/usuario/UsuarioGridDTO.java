package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UsuarioGridDTO implements GridDTO {
    private UUID id;
    private String nome;
    private String login;
    private LocalDateTime createdAt;
}