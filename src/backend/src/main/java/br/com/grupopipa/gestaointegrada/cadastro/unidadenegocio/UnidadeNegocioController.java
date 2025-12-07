package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioConstants.R_UNIDADE_NEGOCIO;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_UNIDADE_NEGOCIO)
public class UnidadeNegocioController
        extends BaseController<UnidadeNegocioDTO, UnidadeNegocioGridDTO, UnidadeNegocioService> {

    public UnidadeNegocioController(UnidadeNegocioService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_UNIDADE_NEGOCIO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_UNIDADE_NEGOCIO_EDITAR')")
    public Response save(@RequestBody UnidadeNegocioDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_UNIDADE_NEGOCIO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_UNIDADE_NEGOCIO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }
}
