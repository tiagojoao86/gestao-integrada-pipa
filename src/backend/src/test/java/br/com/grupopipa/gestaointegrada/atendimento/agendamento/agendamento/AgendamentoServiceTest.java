package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.AgendaRepository;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.entity.Agenda;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.AgendamentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.AgendamentoGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.SlotDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity.Agendamento;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity.AgendamentoHorario;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity.AgendamentoStatus;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;

@DisplayName("AgendamentoService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock private AgendamentoRepository repository;
    @Mock private AgendaRepository agendaRepository;
    @Mock private PessoaRepository pessoaRepository;
    @Mock private ConvenioRepository convenioRepository;
    @Mock private ProcedimentoRepository procedimentoRepository;
    @Mock private SlotCalculatorService slotCalculatorService;
    @Mock private Specifications<Agendamento> specifications;

    @InjectMocks
    private AgendamentoServiceImpl service;

    private UUID agendamentoId;
    private UUID agendaId;
    private UUID pacienteId;
    private Agendamento agendamento;
    private Agenda agenda;
    private Pessoa paciente;

    @BeforeEach
    void setup() {
        agendamentoId = UUID.randomUUID();
        agendaId = UUID.randomUUID();
        pacienteId = UUID.randomUUID();

        agenda = mock(Agenda.class);
        lenient().when(agenda.getId()).thenReturn(agendaId);
        lenient().when(agenda.getNome()).thenReturn("Agenda Teste");
        lenient().when(agenda.getProfissional()).thenReturn(null);

        paciente = mock(Pessoa.class);
        lenient().when(paciente.getId()).thenReturn(pacienteId);
        lenient().when(paciente.getNome()).thenReturn("Maria Paciente");

        agendamento = mock(Agendamento.class);
        lenient().when(agendamento.getId()).thenReturn(agendamentoId);
        lenient().when(agendamento.getAgenda()).thenReturn(agenda);
        lenient().when(agendamento.getPaciente()).thenReturn(paciente);
        lenient().when(agendamento.getConvenio()).thenReturn(null);
        lenient().when(agendamento.getProcedimento()).thenReturn(null);
        lenient().when(agendamento.getObservacao()).thenReturn(null);
        lenient().when(agendamento.getStatus()).thenReturn(AgendamentoStatus.AGENDADO);
        lenient().when(agendamento.getHorarios()).thenReturn(new ArrayList<>());
        lenient().when(agendamento.getCreatedAt()).thenReturn(null);
        lenient().when(agendamento.getUpdatedAt()).thenReturn(null);
        lenient().when(agendamento.getCreatedBy()).thenReturn(null);
        lenient().when(agendamento.getUpdatedBy()).thenReturn(null);
        lenient().when(agendamento.getDeleted()).thenReturn(false);
    }

    // =========================================================================
    // cancelar
    // =========================================================================

    @Test
    @DisplayName("Deve cancelar agendamento e persistir a alteração")
    void deveCancelarAgendamento() {
        when(repository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(repository.save(any(Agendamento.class))).thenReturn(agendamento);

        AgendamentoDTO resultado = service.cancelar(agendamentoId);

        assertThat(resultado).isNotNull();
        verify(agendamento, times(1)).cancelar();
        verify(repository, times(1)).save(agendamento);
    }

    @Test
    @DisplayName("Deve realizar agendamento e persistir a alteração")
    void deveRealizarAgendamento() {
        when(repository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(repository.save(any(Agendamento.class))).thenReturn(agendamento);

        AgendamentoDTO resultado = service.realizar(agendamentoId);

        assertThat(resultado).isNotNull();
        verify(agendamento, times(1)).realizar();
        verify(repository, times(1)).save(agendamento);
    }

    // =========================================================================
    // listarSlots — delegação para SlotCalculatorService
    // =========================================================================

    @Test
    @DisplayName("Deve delegar cálculo de slots ao SlotCalculatorService")
    void deveDelegarCalculoDeSlots() {
        LocalDate data = LocalDate.of(2026, 4, 28);
        List<SlotDTO> slotsEsperados = List.of(
                SlotDTO.builder()
                        .dataHoraInicio(LocalDateTime.of(2026, 4, 28, 8, 0))
                        .dataHoraFim(LocalDateTime.of(2026, 4, 28, 9, 0))
                        .livre(true)
                        .build());
        when(slotCalculatorService.calcularSlots(agendaId, data, data)).thenReturn(slotsEsperados);

        List<SlotDTO> resultado = service.listarSlots(agendaId, data, data);

        assertThat(resultado).isEqualTo(slotsEsperados);
        verify(slotCalculatorService, times(1)).calcularSlots(agendaId, data, data);
    }

    // =========================================================================
    // buildGridDTOFromEntity
    // =========================================================================

    @Test
    @DisplayName("Deve construir GridDTO com primeiraDataHora a partir dos horários")
    void deveConstruirGridDTOComPrimeiraDataHora() {
        LocalDateTime dataHora = LocalDateTime.of(2026, 4, 28, 9, 0);

        AgendamentoHorario horario = mock(AgendamentoHorario.class);
        when(horario.getDataHoraInicio()).thenReturn(dataHora);

        when(agendamento.getHorarios()).thenReturn(List.of(horario));

        AgendamentoGridDTO grid = service.buildGridDTOFromEntity(agendamento);

        assertThat(grid.getPrimeiraDataHora()).isEqualTo(dataHora);
        assertThat(grid.getPrimeiraData()).isEqualTo(dataHora.toLocalDate());
        assertThat(grid.getQtdHorarios()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve construir GridDTO com primeiraDataHora nulo quando sem horários")
    void deveConstruirGridDTOSemHorarios() {
        AgendamentoGridDTO grid = service.buildGridDTOFromEntity(agendamento);

        assertThat(grid.getPrimeiraDataHora()).isNull();
        assertThat(grid.getQtdHorarios()).isZero();
    }

    // =========================================================================
    // buildDTOFromEntity
    // =========================================================================

    @Test
    @DisplayName("Deve construir DTO com status correto")
    void deveConstruirDTOComStatus() {
        when(agendamento.getStatus()).thenReturn(AgendamentoStatus.CANCELADO);

        AgendamentoDTO dto = service.buildDTOFromEntity(agendamento);

        assertThat(dto.getStatus()).isEqualTo("CANCELADO");
    }

    @Test
    @DisplayName("Deve construir DTO com listas de horários ordenadas")
    void deveConstruirDTOComHorariosOrdenados() {
        LocalDateTime hora1 = LocalDateTime.of(2026, 4, 28, 9, 0);
        LocalDateTime hora2 = LocalDateTime.of(2026, 4, 28, 10, 0);

        AgendamentoHorario h1 = mock(AgendamentoHorario.class);
        when(h1.getDataHoraInicio()).thenReturn(hora1);
        when(h1.getDataHoraFim()).thenReturn(hora1.plusMinutes(60));

        AgendamentoHorario h2 = mock(AgendamentoHorario.class);
        when(h2.getDataHoraInicio()).thenReturn(hora2);
        when(h2.getDataHoraFim()).thenReturn(hora2.plusMinutes(60));

        when(agendamento.getHorarios()).thenReturn(List.of(h2, h1)); // propositalmente desordenado

        AgendamentoDTO dto = service.buildDTOFromEntity(agendamento);

        assertThat(dto.getHorariosInicio()).hasSize(2);
        assertThat(dto.getHorariosInicio().get(0)).isEqualTo(hora1);
        assertThat(dto.getHorariosInicio().get(1)).isEqualTo(hora2);
    }

    // =========================================================================
    // Soft delete
    // =========================================================================

    @Test
    @DisplayName("Deve realizar soft delete do agendamento sem chamar deleteById")
    void deveRealizarSoftDelete() {
        when(repository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(repository.save(any(Agendamento.class))).thenReturn(agendamento);

        UUID resultado = service.delete(agendamentoId);

        assertThat(resultado).isEqualTo(agendamentoId);
        verify(repository, times(1)).save(agendamento);
        verify(repository, never()).deleteById(any());
    }
}
