package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.entity.AgendaRegra;

@Service
@Transactional(readOnly = true)
public class ConflitosRegraService {

    private final AgendaRegraRepository regraRepository;

    public ConflitosRegraService(AgendaRegraRepository regraRepository) {
        this.regraRepository = regraRepository;
    }

    public List<ConflitoPar> detectarConflitos(UUID agendaId) {
        List<AgendaRegra> regras = regraRepository.findByAgendaId(agendaId);
        List<ConflitoPar> conflitos = new ArrayList<>();

        for (int i = 0; i < regras.size(); i++) {
            for (int j = i + 1; j < regras.size(); j++) {
                if (conflitam(regras.get(i), regras.get(j))) {
                    conflitos.add(new ConflitoPar(regras.get(i).getId(), regras.get(j).getId()));
                }
            }
        }
        return conflitos;
    }

    private boolean conflitam(AgendaRegra a, AgendaRegra b) {
        return dataseSobrepoem(a, b) && horariosSobrepoem(a, b) && diasSobrepoem(a, b);
    }

    private boolean dataseSobrepoem(AgendaRegra a, AgendaRegra b) {
        LocalDate aFim = a.getDataFim() != null ? a.getDataFim() : LocalDate.MAX;
        LocalDate bFim = b.getDataFim() != null ? b.getDataFim() : LocalDate.MAX;
        return !a.getDataInicio().isAfter(bFim) && !b.getDataInicio().isAfter(aFim);
    }

    private boolean horariosSobrepoem(AgendaRegra a, AgendaRegra b) {
        return a.getHoraInicio().isBefore(b.getHoraFim()) && b.getHoraInicio().isBefore(a.getHoraFim());
    }

    private boolean diasSobrepoem(AgendaRegra a, AgendaRegra b) {
        if (a.getDiasSemana().isEmpty() || b.getDiasSemana().isEmpty()) {
            return true;
        }
        return a.getDiasSemana().stream().anyMatch(b.getDiasSemana()::contains);
    }
}
