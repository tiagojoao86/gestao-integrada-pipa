package br.com.grupopipa.gestaointegrada.cadastro.perfil;

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

import static br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilConstants.R_PERFIL;

import java.util.UUID;;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_PERFIL)
public class PerfilController extends BaseController<PerfilDTO, PerfilGridDTO, PerfilService> {

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PERFIL_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }
    
    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PERFIL_EDITAR')")
    public Response save(@RequestBody PerfilDTO body) {
        return super.save(body);
    }
    
    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PERFIL_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }
    
    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PERFIL_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

}
