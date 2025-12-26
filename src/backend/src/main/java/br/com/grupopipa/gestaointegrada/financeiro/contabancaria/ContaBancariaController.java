package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static br.com.grupopipa.gestaointegrada.financeiro.contabancaria.ContaBancariaConstants.R_CONTA_BANCARIA;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;
import static br.com.grupopipa.gestaointegrada.core.controller.Response.ok;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_CONTA_BANCARIA)
public class ContaBancariaController
        extends BaseController<ContaBancariaDTO, ContaBancariaGridDTO, ContaBancariaService> {

    public ContaBancariaController(ContaBancariaService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_BANCARIA_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_BANCARIA_EDITAR')")
    public Response save(@RequestBody ContaBancariaDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_BANCARIA_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_BANCARIA_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @GetMapping("/unidades-disponiveis")
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_BANCARIA_EDITAR')")
    public Response listarUnidadesDisponiveis() {
        return ok(service.listarUnidadesDisponiveis());
    }

    @Override
    @PreAuthorize("hasAuthority('FINANCEIRO_CONTA_BANCARIA_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }
}
