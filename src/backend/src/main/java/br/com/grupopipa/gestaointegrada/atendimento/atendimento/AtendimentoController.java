package br.com.grupopipa.gestaointegrada.atendimento.atendimento;

import static br.com.grupopipa.gestaointegrada.atendimento.atendimento.AtendimentoConstants.R_ATENDIMENTO;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto.AtendimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto.AtendimentoGridDTO;
import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_ATENDIMENTO)
public class AtendimentoController
        extends BaseController<AtendimentoDTO, AtendimentoGridDTO, AtendimentoService> {

    public AtendimentoController(AtendimentoService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_EDITAR')")
    public Response save(@RequestBody AtendimentoDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }
}
