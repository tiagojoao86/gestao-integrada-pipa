package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.SlotDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity.Agendamento;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity.AgendamentoHorario;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.AgendaRegraRepository;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.entity.AgendaRegra;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.entity.DiaSemana;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;

@DisplayName("SlotCalculatorService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class SlotCalculatorServiceTest {

    @Mock private AgendaRegraRepository regraRepository;
    @Mock private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private SlotCalculatorService service;

    // 2026-04-28 = terça-feira (TER)
    private static final LocalDate TERCA = LocalDate.of(2026, 4, 28);
    private UUID agendaId;

    @BeforeEach
    void setup() {
        agendaId = UUID.randomUUID();
        when(agendamentoRepository.findOcupadosByAgendaEPeriodo(any(), any(), any()))
                .thenReturn(Collections.emptyList());
    }

    private AgendaRegra regraSimples(LocalDate inicio, LocalDate fim, LocalTime horaInicio,
            LocalTime horaFim, int duracao, Set<DiaSemana> dias) {
        AgendaRegra regra = mock(AgendaRegra.class);
        lenient().when(regra.getDataInicio()).thenReturn(inicio);
        lenient().when(regra.getDataFim()).thenReturn(fim);
        lenient().when(regra.getHoraInicio()).thenReturn(horaInicio);
        lenient().when(regra.getHoraFim()).thenReturn(horaFim);
        lenient().when(regra.getDuracaoSessaoMinutos()).thenReturn(duracao);
        lenient().when(regra.getDiasSemana()).thenReturn(dias);
        return regra;
    }

    // =========================================================================
    // Geração básica de slots
    // =========================================================================

    @Test
    @DisplayName("Deve gerar slots corretos para uma regra simples sem filtro de dia")
    void deveGerarSlotsSimples() {
        AgendaRegra regra = regraSimples(
                TERCA, null,
                LocalTime.of(8, 0), LocalTime.of(10, 0), 60,
                Collections.emptySet());
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(List.of(regra));

        List<SlotDTO> slots = service.calcularSlots(agendaId, TERCA, TERCA);

        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getDataHoraInicio()).isEqualTo(LocalDateTime.of(TERCA, LocalTime.of(8, 0)));
        assertThat(slots.get(0).getDataHoraFim()).isEqualTo(LocalDateTime.of(TERCA, LocalTime.of(9, 0)));
        assertThat(slots.get(1).getDataHoraInicio()).isEqualTo(LocalDateTime.of(TERCA, LocalTime.of(9, 0)));
        assertThat(slots.get(1).getDataHoraFim()).isEqualTo(LocalDateTime.of(TERCA, LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("Deve gerar slots livres quando não há agendamentos")
    void deveGerarSlotsLivres() {
        AgendaRegra regra = regraSimples(TERCA, null, LocalTime.of(8, 0), LocalTime.of(9, 0),
                30, Collections.emptySet());
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(List.of(regra));

        List<SlotDTO> slots = service.calcularSlots(agendaId, TERCA, TERCA);

        assertThat(slots).allMatch(SlotDTO::isLivre);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há regras")
    void deveRetornarVazioSemRegras() {
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(Collections.emptyList());

        List<SlotDTO> slots = service.calcularSlots(agendaId, TERCA, TERCA);

        assertThat(slots).isEmpty();
    }

    // =========================================================================
    // Filtro por dia da semana
    // =========================================================================

    @Test
    @DisplayName("Deve aplicar regra apenas no dia da semana configurado")
    void deveAplicarFiltroDisaemana() {
        // Regra somente para segunda (SEG) — não deve gerar slots na terça
        AgendaRegra regra = regraSimples(TERCA, null, LocalTime.of(8, 0), LocalTime.of(10, 0),
                60, Set.of(DiaSemana.SEG));
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(List.of(regra));

        List<SlotDTO> slots = service.calcularSlots(agendaId, TERCA, TERCA);

        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("Deve gerar slots quando o dia da semana coincide com a regra")
    void deveGerarSlotsDiaCorrespondente() {
        // Regra somente para terça (TER) — deve gerar slots na terça
        AgendaRegra regra = regraSimples(TERCA, null, LocalTime.of(8, 0), LocalTime.of(9, 0),
                30, Set.of(DiaSemana.TER));
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(List.of(regra));

        List<SlotDTO> slots = service.calcularSlots(agendaId, TERCA, TERCA);

        assertThat(slots).hasSize(2);
    }

    // =========================================================================
    // Filtro por período da regra
    // =========================================================================

    @Test
    @DisplayName("Deve ignorar regra que começa após a data solicitada")
    void deveIgnorarRegraFutura() {
        AgendaRegra regra = regraSimples(
                TERCA.plusDays(1), null,
                LocalTime.of(8, 0), LocalTime.of(9, 0), 30, Collections.emptySet());
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(List.of(regra));

        List<SlotDTO> slots = service.calcularSlots(agendaId, TERCA, TERCA);

        assertThat(slots).isEmpty();
    }

    @Test
    @DisplayName("Deve ignorar regra que terminou antes da data solicitada")
    void deveIgnorarRegraExpirada() {
        AgendaRegra regra = regraSimples(
                TERCA.minusDays(7), TERCA.minusDays(1),
                LocalTime.of(8, 0), LocalTime.of(9, 0), 30, Collections.emptySet());
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(List.of(regra));

        List<SlotDTO> slots = service.calcularSlots(agendaId, TERCA, TERCA);

        assertThat(slots).isEmpty();
    }

    // =========================================================================
    // Deduplicação
    // =========================================================================

    @Test
    @DisplayName("Deve remover todos os slots conflitantes quando duas regras geram o mesmo horário")
    void deveRemoverSlotsConflitantes() {
        // Duas regras que geram exatamente o mesmo slot — ambos são removidos (comportamento de
        // deduplicação conservador: evita ambiguidade em vez de escolher arbitrariamente um)
        AgendaRegra regra1 = regraSimples(TERCA, null, LocalTime.of(8, 0), LocalTime.of(9, 0),
                60, Collections.emptySet());
        AgendaRegra regra2 = regraSimples(TERCA, null, LocalTime.of(8, 0), LocalTime.of(9, 0),
                60, Collections.emptySet());
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(List.of(regra1, regra2));

        List<SlotDTO> slots = service.calcularSlots(agendaId, TERCA, TERCA);

        assertThat(slots).isEmpty();
    }

    // =========================================================================
    // Slots ocupados
    // =========================================================================

    @Test
    @DisplayName("Deve marcar slot como ocupado quando há agendamento no horário")
    void deveMarcarSlotOcupado() {
        AgendaRegra regra = regraSimples(TERCA, null, LocalTime.of(8, 0), LocalTime.of(9, 0),
                60, Collections.emptySet());
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(List.of(regra));

        LocalDateTime inicioSlot = LocalDateTime.of(TERCA, LocalTime.of(8, 0));
        Pessoa paciente = mock(Pessoa.class);
        when(paciente.getNome()).thenReturn("João Paciente");

        AgendamentoHorario horario = mock(AgendamentoHorario.class);
        when(horario.getDataHoraInicio()).thenReturn(inicioSlot);

        Agendamento agendamento = mock(Agendamento.class);
        when(agendamento.getPaciente()).thenReturn(paciente);
        when(agendamento.getConvenio()).thenReturn(null);
        when(agendamento.getProcedimento()).thenReturn(null);
        when(agendamento.getStatus()).thenReturn(null);
        when(agendamento.getId()).thenReturn(UUID.randomUUID());
        when(agendamento.getHorarios()).thenReturn(List.of(horario));

        when(agendamentoRepository.findOcupadosByAgendaEPeriodo(eq(agendaId), any(), any()))
                .thenReturn(List.of(agendamento));

        List<SlotDTO> slots = service.calcularSlots(agendaId, TERCA, TERCA);

        assertThat(slots).hasSize(1);
        assertThat(slots.get(0).isLivre()).isFalse();
        assertThat(slots.get(0).getPacienteNome()).isEqualTo("João Paciente");
    }

    // =========================================================================
    // Período de múltiplos dias
    // =========================================================================

    @Test
    @DisplayName("Deve gerar slots para cada dia no período solicitado")
    void deveGerarSlotsParaMultiplosDias() {
        LocalDate segunda = LocalDate.of(2026, 4, 27); // SEG
        // Regra sem filtro de dia — gera todos os dias
        AgendaRegra regra = regraSimples(segunda, null, LocalTime.of(8, 0), LocalTime.of(9, 0),
                60, Collections.emptySet());
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(List.of(regra));

        List<SlotDTO> slots = service.calcularSlots(agendaId, segunda, TERCA); // 2 dias

        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getDataHoraInicio().toLocalDate()).isEqualTo(segunda);
        assertThat(slots.get(1).getDataHoraInicio().toLocalDate()).isEqualTo(TERCA);
    }

    @Test
    @DisplayName("Deve ordenar slots por data/hora de início")
    void deveOrdenarSlotsPorDataHora() {
        AgendaRegra regra = regraSimples(TERCA, null, LocalTime.of(9, 0), LocalTime.of(11, 0),
                60, Collections.emptySet());
        when(regraRepository.findByAgendaId(agendaId)).thenReturn(List.of(regra));

        List<SlotDTO> slots = service.calcularSlots(agendaId, TERCA, TERCA);

        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getDataHoraInicio())
                .isBefore(slots.get(1).getDataHoraInicio());
    }
}
