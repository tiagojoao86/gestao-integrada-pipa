package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.AgendamentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.AgendamentoGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.SlotDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface AgendamentoService
        extends CrudService<AgendamentoDTO, AgendamentoGridDTO> {

    List<SlotDTO> listarSlots(UUID agendaId, LocalDate dataInicio, LocalDate dataFim);

    List<AgendamentoDTO> listarConflitosParaPaciente(
            UUID pacienteId, LocalDate dataInicio, LocalDate dataFim);

    AgendamentoDTO cancelar(UUID id);
    AgendamentoDTO realizar(UUID id);
}
