package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import br.com.grupopipa.gestaointegrada.core.dto.FilterDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UsuarioFilter extends FilterDTO {

    private String login;

}
