package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda;

import static br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.AgendaConstants.R_AGENDA;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.dto.AgendaDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.dto.AgendaGridDTO;
import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_AGENDA)
public class AgendaController
        extends BaseController<AgendaDTO, AgendaGridDTO, AgendaService> {

    public AgendaController(AgendaService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_EDITAR')")
    public Response save(@RequestBody AgendaDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDA_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }
}
