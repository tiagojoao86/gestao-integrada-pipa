package br.com.grupopipa.gestaointegrada.financeiro.categoria;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static br.com.grupopipa.gestaointegrada.financeiro.categoria.CategoriaTituloConstants.R_CATEGORIA_TITULO;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_CATEGORIA_TITULO)
public class CategoriaTituloController
        extends BaseController<CategoriaTituloDTO, CategoriaTituloGridDTO, CategoriaTituloService> {

    public CategoriaTituloController(CategoriaTituloService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CATEGORIA_TITULO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CATEGORIA_TITULO_EDITAR')")
    public Response save(@RequestBody CategoriaTituloDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CATEGORIA_TITULO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CATEGORIA_TITULO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }
}
