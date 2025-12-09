package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static br.com.grupopipa.gestaointegrada.financeiro.planocontas.PlanoContasConstants.R_PLANO_CONTAS;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;
import static br.com.grupopipa.gestaointegrada.core.controller.Response.ok;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_PLANO_CONTAS)
public class PlanoContasController extends BaseController<PlanoContasDTO, PlanoContasGridDTO, PlanoContasService> {

    public PlanoContasController(PlanoContasService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_EDITAR')")
    public Response save(@RequestBody PlanoContasDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @GetMapping("/unidades-disponiveis")
    @PreAuthorize("hasAuthority('CADASTRO_PLANO_CONTAS_EDITAR')")
    public Response listarUnidadesDisponiveis() {
        return ok(service.listarUnidadesDisponiveis());
    }
}
