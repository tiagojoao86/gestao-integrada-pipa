package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

import static br.com.grupopipa.gestaointegrada.cadastro.usuario.UsuarioConstants.R_USUARIO;

import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_USUARIO)
public class UsuarioController extends BaseController<UsuarioDTO, UsuarioGridDTO, UsuarioService> {
    
    @Override
    @PreAuthorize("hasAuthority('CADASTRO_USUARIO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }
    
    @Override
    @PreAuthorize("hasAuthority('CADASTRO_USUARIO_EDITAR')")
    public Response save(@RequestBody UsuarioDTO body) {
        return super.save(body);
    }
    
    @Override
    @PreAuthorize("hasAuthority('CADASTRO_USUARIO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }
    
    @Override
    @PreAuthorize("hasAuthority('CADASTRO_USUARIO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

}
