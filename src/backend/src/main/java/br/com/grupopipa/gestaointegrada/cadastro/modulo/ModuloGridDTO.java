package br.com.grupopipa.gestaointegrada.cadastro.modulo;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ModuloGridDTO implements GridDTO {
    private UUID id;
    private String nome;
}
