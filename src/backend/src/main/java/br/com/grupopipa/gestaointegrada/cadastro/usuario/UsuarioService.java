package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import java.util.List;

import br.com.grupopipa.gestaointegrada.config.security.dto.AuthorityDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface UsuarioService extends CrudService<UsuarioDTO, UsuarioGridDTO> {

    UsuarioDTO findUsuarioDTOByLogin(String login);
    List<AuthorityDTO> findAuthoritiesByLogin(String login);
}
