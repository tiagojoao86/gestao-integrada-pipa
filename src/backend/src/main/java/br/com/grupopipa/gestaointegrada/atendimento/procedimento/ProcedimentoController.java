package br.com.grupopipa.gestaointegrada.atendimento.procedimento;

import static br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoConstants.R_PROCEDIMENTO;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.dto.ProcedimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.dto.ProcedimentoGridDTO;
import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_PROCEDIMENTO)
public class ProcedimentoController
        extends BaseController<ProcedimentoDTO, ProcedimentoGridDTO, ProcedimentoService> {

    public ProcedimentoController(ProcedimentoService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_PROCEDIMENTO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_PROCEDIMENTO_EDITAR')")
    public Response save(@RequestBody ProcedimentoDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_PROCEDIMENTO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_PROCEDIMENTO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('ATENDIMENTO_PROCEDIMENTO_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }
}
