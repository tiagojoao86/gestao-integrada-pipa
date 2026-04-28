package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity.Agendamento;

public interface AgendamentoRepository
        extends JpaRepository<Agendamento, UUID>, JpaSpecificationExecutor<Agendamento> {

    @Query("SELECT a FROM Agendamento a JOIN FETCH a.horarios h "
        + "WHERE a.agenda.id = :agendaId "
        + "AND a.status <> 'CANCELADO' "
        + "AND (a.deleted IS NULL OR a.deleted = FALSE) "
        + "AND h.dataHoraInicio >= :inicio "
        + "AND h.dataHoraInicio < :fim")
    List<Agendamento> findOcupadosByAgendaEPeriodo(
        @Param("agendaId") UUID agendaId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT a FROM Agendamento a JOIN FETCH a.horarios h "
        + "WHERE a.paciente.id = :pacienteId "
        + "AND a.status <> 'CANCELADO' "
        + "AND (a.deleted IS NULL OR a.deleted = FALSE) "
        + "AND h.dataHoraInicio < :fim "
        + "AND h.dataHoraFim > :inicio")
    List<Agendamento> findConflitosParaPaciente(
        @Param("pacienteId") UUID pacienteId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );

    @Query("SELECT DISTINCT a FROM Agendamento a LEFT JOIN FETCH a.horarios "
        + "WHERE a.paciente.id = :pessoaId "
        + "AND (a.deleted IS NULL OR a.deleted = FALSE) "
        + "AND EXISTS (SELECT h FROM AgendamentoHorario h "
        + "  WHERE h.agendamento = a "
        + "  AND h.dataHoraInicio >= :inicio "
        + "  AND h.dataHoraInicio < :fim)")
    List<Agendamento> findByPacienteAndPeriodo(
        @Param("pessoaId") UUID pessoaId,
        @Param("inicio") LocalDateTime inicio,
        @Param("fim") LocalDateTime fim
    );
}
