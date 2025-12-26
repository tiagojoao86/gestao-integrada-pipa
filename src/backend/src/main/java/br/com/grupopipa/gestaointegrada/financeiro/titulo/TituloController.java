package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloConstants.R_TITULO;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;
import static br.com.grupopipa.gestaointegrada.core.controller.Response.ok;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_TITULO)
public class TituloController extends BaseController<TituloDTO, TituloGridDTO, TituloService> {

    public TituloController(TituloService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_EDITAR')")
    public Response save(@RequestBody TituloDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @GetMapping("/unidades-disponiveis")
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_EDITAR')")
    public Response listarUnidadesDisponiveis() {
        return ok(service.listarUnidadesDisponiveis());
    }

    @GetMapping("/pessoas-disponiveis")
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_EDITAR')")
    public Response listarPessoasDisponiveis() {
        return ok(service.listarPessoasDisponiveis());
    }

    @GetMapping("/categorias-disponiveis")
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_EDITAR')")
    public Response listarCategoriasDisponiveis() {
        return ok(service.listarCategoriasDisponiveis());
    }

    @GetMapping("/planos-disponiveis")
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_EDITAR')")
    public Response listarPlanosDisponiveis(@RequestParam("unidadeNegocioId") UUID unidadeNegocioId) {
        return ok(service.listarPlanosDisponiveis(unidadeNegocioId));
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_TITULO_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }
}
