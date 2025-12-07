package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaConstants.R_PESSOA;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_PESSOA)
public class PessoaController extends BaseController<PessoaDTO, PessoaGridDTO, PessoaService> {

    public PessoaController(PessoaService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PESSOA_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PESSOA_EDITAR')")
    public Response save(@RequestBody PessoaDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PESSOA_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('CADASTRO_PESSOA_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }
}
