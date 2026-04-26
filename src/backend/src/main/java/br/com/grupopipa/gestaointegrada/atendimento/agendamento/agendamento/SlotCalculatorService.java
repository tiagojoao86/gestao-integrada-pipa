package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.SlotDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity.Agendamento;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity.AgendamentoHorario;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.AgendaRegraRepository;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.entity.AgendaRegra;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.entity.DiaSemana;

@Service
@Transactional(readOnly = true)
public class SlotCalculatorService {

    private final AgendaRegraRepository regraRepository;
    private final AgendamentoRepository agendamentoRepository;

    public SlotCalculatorService(
            AgendaRegraRepository regraRepository,
            AgendamentoRepository agendamentoRepository) {
        this.regraRepository = regraRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    public List<SlotDTO> calcularSlots(UUID agendaId, LocalDate dataInicio, LocalDate dataFim) {
        List<AgendaRegra> regras = regraRepository.findByAgendaId(agendaId);
        Map<LocalDateTime, Agendamento> ocupados = getOcupados(agendaId, dataInicio, dataFim);

        List<SlotDTO> slots = new ArrayList<>();
        for (LocalDate data = dataInicio; !data.isAfter(dataFim); data = data.plusDays(1)) {
            DiaSemana dia = toDiaSemana(data.getDayOfWeek());
            final LocalDate dataFinal = data;
            regras.stream()
                .filter(r -> regraAplica(r, dataFinal, dia))
                .forEach(r -> slots.addAll(gerarSlots(r, dataFinal, ocupados)));
        }

        return slots.stream()
            .filter(s -> !contemDuplicata(slots, s))
            .sorted(Comparator.comparing(SlotDTO::getDataHoraInicio))
            .collect(java.util.stream.Collectors.toList());
    }

    private boolean regraAplica(AgendaRegra regra, LocalDate data, DiaSemana dia) {
        if (data.isBefore(regra.getDataInicio())) return false;
        if (regra.getDataFim() != null && data.isAfter(regra.getDataFim())) return false;
        return regra.getDiasSemana().isEmpty() || regra.getDiasSemana().contains(dia);
    }

    private List<SlotDTO> gerarSlots(AgendaRegra regra, LocalDate data,
            Map<LocalDateTime, Agendamento> ocupados) {
        List<SlotDTO> slots = new ArrayList<>();
        LocalDateTime inicio = LocalDateTime.of(data, regra.getHoraInicio());
        LocalDateTime fimRegra = LocalDateTime.of(data, regra.getHoraFim());
        int duracao = regra.getDuracaoSessaoMinutos();

        while (!inicio.plusMinutes(duracao).isAfter(fimRegra)) {
            LocalDateTime fim = inicio.plusMinutes(duracao);
            Agendamento agendamento = ocupados.get(inicio);
            slots.add(buildSlot(inicio, fim, agendamento));
            inicio = fim;
        }
        return slots;
    }

    private SlotDTO buildSlot(LocalDateTime inicio, LocalDateTime fim, Agendamento agendamento) {
        SlotDTO.SlotDTOBuilder builder = SlotDTO.builder()
            .dataHoraInicio(inicio)
            .dataHoraFim(fim)
            .livre(agendamento == null);
        if (agendamento != null) {
            builder.agendamentoId(agendamento.getId())
                .pacienteNome(agendamento.getPaciente() != null
                    ? agendamento.getPaciente().getNome() : null)
                .convenioNome(agendamento.getConvenio() != null
                    ? agendamento.getConvenio().getNome() : null)
                .procedimentoNome(agendamento.getProcedimento() != null
                    ? agendamento.getProcedimento().getDescricao() : null)
                .status(agendamento.getStatus() != null
                    ? agendamento.getStatus().name() : null);
        }
        return builder.build();
    }

    private Map<LocalDateTime, Agendamento> getOcupados(UUID agendaId, LocalDate inicio,
            LocalDate fim) {
        LocalDateTime inicioTs = inicio.atStartOfDay();
        LocalDateTime fimTs = fim.plusDays(1).atStartOfDay();
        List<Agendamento> agendamentos =
            agendamentoRepository.findOcupadosByAgendaEPeriodo(agendaId, inicioTs, fimTs);
        Map<LocalDateTime, Agendamento> map = new HashMap<>();
        for (Agendamento a : agendamentos) {
            for (AgendamentoHorario h : a.getHorarios()) {
                map.put(h.getDataHoraInicio(), a);
            }
        }
        return map;
    }

    private boolean contemDuplicata(List<SlotDTO> todos, SlotDTO slot) {
        return todos.stream()
            .anyMatch(s -> s != slot && s.getDataHoraInicio().equals(slot.getDataHoraInicio()));
    }

    private DiaSemana toDiaSemana(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> DiaSemana.SEG;
            case TUESDAY -> DiaSemana.TER;
            case WEDNESDAY -> DiaSemana.QUA;
            case THURSDAY -> DiaSemana.QUI;
            case FRIDAY -> DiaSemana.SEX;
            case SATURDAY -> DiaSemana.SAB;
            case SUNDAY -> DiaSemana.DOM;
        };
    }
}
