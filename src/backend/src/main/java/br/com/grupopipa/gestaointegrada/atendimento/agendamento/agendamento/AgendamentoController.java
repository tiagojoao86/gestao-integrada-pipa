package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento;

import static br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.AgendamentoConstants.R_AGENDAMENTO;
import static br.com.grupopipa.gestaointegrada.core.constants.Constants.F_ID;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.AgendamentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.AgendamentoGridDTO;
import br.com.grupopipa.gestaointegrada.core.controller.BaseController;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(R_AGENDAMENTO)
public class AgendamentoController
        extends BaseController<AgendamentoDTO, AgendamentoGridDTO, AgendamentoService> {

    public AgendamentoController(AgendamentoService service) {
        super(service);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDAMENTO_LISTAR')")
    public Response list(@RequestBody PageRequest request) {
        return super.list(request);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDAMENTO_EDITAR')")
    public Response save(@RequestBody AgendamentoDTO body) {
        return super.save(body);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDAMENTO_VISUALIZAR')")
    public Response findById(@RequestParam(F_ID) UUID id) {
        return super.findById(id);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDAMENTO_DELETAR')")
    public Response delete(@PathVariable(F_ID) UUID id) {
        return super.delete(id);
    }

    @Override
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDAMENTO_AUDITAR')")
    public Response getAuditInfo(@PathVariable(F_ID) UUID id) {
        return super.getAuditInfo(id);
    }

    @GetMapping("/slots")
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDAMENTO_LISTAR')")
    public Response listarSlots(
            @RequestParam UUID agendaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return Response.ok(service.listarSlots(agendaId, dataInicio, dataFim));
    }

    @GetMapping("/conflito-paciente")
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDAMENTO_LISTAR')")
    public Response conflitoPaciente(
            @RequestParam UUID pacienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        return Response.ok(service.listarConflitosParaPaciente(pacienteId, dataInicio, dataFim));
    }

    @PatchMapping("/cancelar/{id}")
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDAMENTO_EDITAR')")
    public Response cancelar(@PathVariable UUID id) {
        return Response.ok(service.cancelar(id));
    }

    @PatchMapping("/realizar/{id}")
    @PreAuthorize("hasAuthority('AGENDAMENTO_AGENDAMENTO_EDITAR')")
    public Response realizar(@PathVariable UUID id) {
        return Response.ok(service.realizar(id));
    }
}
