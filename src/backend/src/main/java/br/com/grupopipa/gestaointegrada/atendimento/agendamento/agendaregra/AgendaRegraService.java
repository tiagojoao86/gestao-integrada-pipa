package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra;

import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.dto.AgendaRegraDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.dto.AgendaRegraGridDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface AgendaRegraService extends CrudService<AgendaRegraDTO, AgendaRegraGridDTO> {

    List<AgendaRegraGridDTO> listByAgenda(UUID agendaId);
}
