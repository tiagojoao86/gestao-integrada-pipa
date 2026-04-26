package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra;

import static br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.AgendaRegraConstants.R_AGENDA_REGRA;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.dto.AgendaRegraDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.dto.AgendaRegraGridDTO;
import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_AGENDA_REGRA)
public class AgendaRegraController
        extends BaseController<AgendaRegraDTO, AgendaRegraGridDTO, AgendaRegraService> {

    private final ConflitosRegraService conflitosService;

    public AgendaRegraController(AgendaRegraService service, ConflitosRegraService conflitosService) {
        super(service);
        this.conflitosService = conflitosService;
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_REGRA_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_REGRA_EDITAR')")
    public Response save(@RequestBody AgendaRegraDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_REGRA_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_REGRA_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_REGRA_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }

    @GetMapping("/by-agenda")
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_REGRA_LISTAR')")
    public Response listByAgenda(@RequestParam UUID agendaId) {
        return Response.ok(service.listByAgenda(agendaId));
    }

    @GetMapping("/conflitos")
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_REGRA_LISTAR')")
    public Response getConflitos(@RequestParam UUID agendaId) {
        return Response.ok(conflitosService.detectarConflitos(agendaId));
    }
}
