package br.com.grupopipa.gestaointegrada.financeiro.centrocusto;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static br.com.grupopipa.gestaointegrada.financeiro.centrocusto.CentroCustoConstants.R_CENTRO_CUSTO;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_CENTRO_CUSTO)
public class CentroCustoController extends BaseController<CentroCustoDTO, CentroCustoGridDTO, CentroCustoService> {

    public CentroCustoController(CentroCustoService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CENTRO_CUSTO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('CADASTRO_SETOR_EDITAR')")
    public Response listAll() {
        return super.listAll();
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CENTRO_CUSTO_EDITAR')")
    public Response save(@RequestBody CentroCustoDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CENTRO_CUSTO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CENTRO_CUSTO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CENTRO_CUSTO_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }

}
