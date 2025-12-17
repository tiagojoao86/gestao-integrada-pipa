package br.com.grupopipa.gestaointegrada.cadastro.setor;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/setor")
public class SetorController extends BaseController<SetorDTO, SetorGridDTO, SetorService> {

    public SetorController(SetorService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_SETOR_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_SETOR_EDITAR')")
    public Response save(@RequestBody SetorDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_SETOR_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_SETOR_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }
}
