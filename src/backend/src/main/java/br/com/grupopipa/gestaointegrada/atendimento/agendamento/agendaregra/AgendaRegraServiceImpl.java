package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.AgendaRepository;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.entity.Agenda;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.dto.AgendaRegraDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.dto.AgendaRegraGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.entity.AgendaRegra;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendaregra.entity.DiaSemana;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoRepository;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
@Transactional(readOnly = true)
public class AgendaRegraServiceImpl
        extends CrudServiceImpl<AgendaRegraDTO, AgendaRegraGridDTO, AgendaRegra, AgendaRegraRepository>
        implements AgendaRegraService {

    private final AgendaRepository agendaRepository;
    private final ConvenioRepository convenioRepository;
    private final ProcedimentoRepository procedimentoRepository;

    public AgendaRegraServiceImpl(
            AgendaRegraRepository repository,
            Specifications<AgendaRegra> specifications,
            AgendaRepository agendaRepository,
            ConvenioRepository convenioRepository,
            ProcedimentoRepository procedimentoRepository) {
        super(repository, specifications);
        this.agendaRepository = agendaRepository;
        this.convenioRepository = convenioRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    @Override
    @Transactional
    public AgendaRegraDTO save(AgendaRegraDTO dto) {
        AgendaRegraDTO saved = super.save(dto);
        AgendaRegra entity = this.findEntityById(saved.getId());
        syncColecoes(entity, dto);
        return buildDTOFromEntity(entity);
    }

    @Override
    protected AgendaRegra mergeEntityAndDTO(AgendaRegra entity, AgendaRegraDTO dto) {
        if (Objects.isNull(entity)) {
            return criarNovaRegra(dto);
        }
        return atualizarRegra(entity, dto);
    }

    private AgendaRegra criarNovaRegra(AgendaRegraDTO dto) {
        Agenda agenda = buscarAgenda(dto.getAgendaId());
        return new AgendaRegra.Builder()
                .agenda(agenda)
                .dataInicio(dto.getDataInicio())
                .dataFim(dto.getDataFim())
                .horaInicio(dto.getHoraInicio())
                .horaFim(dto.getHoraFim())
                .duracaoSessaoMinutos(dto.getDuracaoSessaoMinutos())
                .build();
    }

    private AgendaRegra atualizarRegra(AgendaRegra entity, AgendaRegraDTO dto) {
        Agenda agenda = buscarAgenda(dto.getAgendaId());
        entity.atualizar(
            agenda,
            dto.getDataInicio(),
            dto.getDataFim(),
            dto.getHoraInicio(),
            dto.getHoraFim(),
            dto.getDuracaoSessaoMinutos()
        );
        return entity;
    }

    private void syncColecoes(AgendaRegra entity, AgendaRegraDTO dto) {
        entity.setDiasSemana(parseDiasSemana(dto.getDiasSemana()));
        entity.setConvenios(resolverConvenios(dto.getConvenioIds()));
        entity.setProcedimentos(resolverProcedimentos(dto.getProcedimentoIds()));
        repository.save(entity);
    }

    private Set<DiaSemana> parseDiasSemana(Set<String> keys) {
        if (keys == null) {
            return new HashSet<>();
        }
        return keys.stream()
                .map(DiaSemana::valueOf)
                .collect(Collectors.toSet());
    }

    private Set<Convenio> resolverConvenios(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return ids.stream()
                .map(id -> convenioRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Procedimento> resolverProcedimentos(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return ids.stream()
                .map(id -> procedimentoRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Agenda buscarAgenda(UUID agendaId) {
        if (agendaId == null) {
            return null;
        }
        return agendaRepository.findById(agendaId)
                .orElseThrow(() -> new IllegalArgumentException("Agenda não encontrada."));
    }

    @Override
    public List<AgendaRegraGridDTO> listByAgenda(UUID agendaId) {
        return repository.findByAgendaId(agendaId).stream()
                .map(this::buildGridDTOFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    protected AgendaRegraDTO buildDTOFromEntity(AgendaRegra entity) {
        return AgendaRegraDTO.builder()
                .id(entity.getId())
                .agendaId(entity.getAgenda() != null ? entity.getAgenda().getId() : null)
                .dataInicio(entity.getDataInicio())
                .dataFim(entity.getDataFim())
                .horaInicio(entity.getHoraInicio())
                .horaFim(entity.getHoraFim())
                .duracaoSessaoMinutos(entity.getDuracaoSessaoMinutos())
                .diasSemana(entity.getDiasSemana().stream()
                        .map(DiaSemana::name).collect(Collectors.toSet()))
                .convenioIds(entity.getConvenios().stream()
                        .map(Convenio::getId).collect(Collectors.toSet()))
                .convenioNomes(entity.getConvenios().stream()
                        .map(Convenio::getNome).collect(Collectors.toList()))
                .procedimentoIds(entity.getProcedimentos().stream()
                        .map(Procedimento::getId).collect(Collectors.toSet()))
                .procedimentoNomes(entity.getProcedimentos().stream()
                        .map(Procedimento::getDescricao).collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected AgendaRegraGridDTO buildGridDTOFromEntity(AgendaRegra entity) {
        return AgendaRegraGridDTO.builder()
                .id(entity.getId())
                .agendaId(entity.getAgenda() != null ? entity.getAgenda().getId() : null)
                .dataInicio(entity.getDataInicio())
                .dataFim(entity.getDataFim())
                .horaInicio(entity.getHoraInicio())
                .horaFim(entity.getHoraFim())
                .duracaoSessaoMinutos(entity.getDuracaoSessaoMinutos())
                .diasSemanaFormatado(formatarDias(entity.getDiasSemana()))
                .qtdConvenios(entity.getConvenios().size())
                .qtdProcedimentos(entity.getProcedimentos().size())
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    private String formatarDias(Set<DiaSemana> dias) {
        if (dias == null || dias.isEmpty()) {
            return "Todos os dias";
        }
        return dias.stream()
                .sorted()
                .map(DiaSemana::name)
                .collect(Collectors.joining(", "));
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("dataInicio", "horaInicio");
    }

    @Override
    protected Class<AgendaRegra> getEntityClass() {
        return AgendaRegra.class;
    }
}
