package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.entity.Agenda;

@Repository
public interface AgendaRepository
        extends JpaRepository<Agenda, UUID>, JpaSpecificationExecutor<Agenda> {
}
