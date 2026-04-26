package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.entity.AgendaRegra;

public interface AgendaRegraRepository
        extends JpaRepository<AgendaRegra, UUID>, JpaSpecificationExecutor<AgendaRegra> {

    @Query("SELECT r FROM AgendaRegra r WHERE r.agenda.id = :agendaId "
        + "AND (r.deleted IS NULL OR r.deleted = FALSE)")
    List<AgendaRegra> findByAgendaId(@Param("agendaId") UUID agendaId);
}
