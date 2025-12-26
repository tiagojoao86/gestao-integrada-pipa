package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;
import static br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaConstants.R_TITULO_CATEGORIA;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_TITULO_CATEGORIA)
public class TituloCategoriaController
        extends BaseController<TituloCategoriaDTO, TituloCategoriaGridDTO, TituloCategoriaService> {

    public TituloCategoriaController(TituloCategoriaService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_CATEGORIA_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_CATEGORIA_EDITAR')")
    public Response save(@RequestBody TituloCategoriaDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_CATEGORIA_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_CATEGORIA_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_CATEGORIA_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }
}
