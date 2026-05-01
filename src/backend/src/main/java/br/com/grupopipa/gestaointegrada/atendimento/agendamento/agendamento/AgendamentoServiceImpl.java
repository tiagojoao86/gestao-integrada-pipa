package br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.AgendaRepository;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agenda.entity.Agenda;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.AgendamentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.AgendamentoGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.dto.SlotDTO;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity.Agendamento;
import br.com.grupopipa.gestaointegrada.atendimento.agendamento.agendamento.entity.AgendamentoHorario;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.ConvenioCategoriaRepository;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoRepository;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
@Transactional(readOnly = true)
public class AgendamentoServiceImpl
        extends CrudServiceImpl<AgendamentoDTO, AgendamentoGridDTO, Agendamento,
                                AgendamentoRepository>
        implements AgendamentoService {

    private final AgendaRepository agendaRepository;
    private final PessoaRepository pessoaRepository;
    private final ConvenioRepository convenioRepository;
    private final ConvenioCategoriaRepository convenioCategoriaRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final SlotCalculatorService slotCalculatorService;

    public AgendamentoServiceImpl(
            AgendamentoRepository repository,
            Specifications<Agendamento> specifications,
            AgendaRepository agendaRepository,
            PessoaRepository pessoaRepository,
            ConvenioRepository convenioRepository,
            ConvenioCategoriaRepository convenioCategoriaRepository,
            ProcedimentoRepository procedimentoRepository,
            SlotCalculatorService slotCalculatorService) {
        super(repository, specifications);
        this.agendaRepository = agendaRepository;
        this.pessoaRepository = pessoaRepository;
        this.convenioRepository = convenioRepository;
        this.convenioCategoriaRepository = convenioCategoriaRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.slotCalculatorService = slotCalculatorService;
    }

    @Override
    @Transactional
    public AgendamentoDTO save(AgendamentoDTO dto) {
        AgendamentoDTO saved = super.save(dto);
        Agendamento entity = this.findEntityById(saved.getId());
        sincronizarHorarios(entity, dto);
        return buildDTOFromEntity(entity);
    }

    @Override
    protected Agendamento mergeEntityAndDTO(Agendamento entity, AgendamentoDTO dto) {
        if (Objects.isNull(entity)) {
            return criarNovoAgendamento(dto);
        }
        return atualizarAgendamento(entity, dto);
    }

    private Agendamento criarNovoAgendamento(AgendamentoDTO dto) {
        return new Agendamento.Builder()
            .agenda(buscarAgenda(dto.getAgendaId()))
            .paciente(buscarPaciente(dto.getPacienteId()))
            .convenio(dto.getConvenioId() != null ? buscarConvenio(dto.getConvenioId()) : null)
            .categoria(
                dto.getCategoriaId() != null ? buscarCategoria(dto.getCategoriaId()) : null)
            .procedimento(
                dto.getProcedimentoId() != null ? buscarProcedimento(dto.getProcedimentoId()) : null)
            .observacao(dto.getObservacao())
            .build();
    }

    private Agendamento atualizarAgendamento(Agendamento entity, AgendamentoDTO dto) {
        entity.atualizar(
            buscarAgenda(dto.getAgendaId()),
            buscarPaciente(dto.getPacienteId()),
            dto.getConvenioId() != null ? buscarConvenio(dto.getConvenioId()) : null,
            dto.getCategoriaId() != null ? buscarCategoria(dto.getCategoriaId()) : null,
            dto.getProcedimentoId() != null ? buscarProcedimento(dto.getProcedimentoId()) : null,
            dto.getObservacao()
        );
        return entity;
    }

    private void sincronizarHorarios(Agendamento entity, AgendamentoDTO dto) {
        entity.limparHorarios();
        if (dto.getHorariosInicio() != null && dto.getHorariosFim() != null) {
            int size = Math.min(dto.getHorariosInicio().size(), dto.getHorariosFim().size());
            for (int i = 0; i < size; i++) {
                entity.addHorario(new AgendamentoHorario(
                    entity,
                    dto.getHorariosInicio().get(i),
                    dto.getHorariosFim().get(i)
                ));
            }
        }
        repository.save(entity);
    }

    @Override
    public List<SlotDTO> listarSlots(UUID agendaId, LocalDate dataInicio, LocalDate dataFim) {
        return slotCalculatorService.calcularSlots(agendaId, dataInicio, dataFim);
    }

    @Override
    public List<AgendamentoDTO> listarConflitosParaPaciente(
            UUID pacienteId, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.plusDays(1).atStartOfDay();
        return repository.findConflitosParaPaciente(pacienteId, inicio, fim).stream()
            .map(this::buildDTOFromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public List<AgendamentoGridDTO> listarPorPaciente(UUID pessoaId, LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.plusDays(1).atStartOfDay();
        return repository.findByPacienteAndPeriodo(pessoaId, inicio, fim).stream()
            .sorted(Comparator.comparing(a -> a.getHorarios().stream()
                .map(AgendamentoHorario::getDataHoraInicio)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MAX)))
            .map(this::buildGridDTOFromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AgendamentoDTO cancelar(UUID id) {
        Agendamento entity = findEntityById(id);
        entity.cancelar();
        repository.save(entity);
        return buildDTOFromEntity(entity);
    }

    @Override
    @Transactional
    public AgendamentoDTO realizar(UUID id) {
        Agendamento entity = findEntityById(id);
        entity.realizar();
        repository.save(entity);
        return buildDTOFromEntity(entity);
    }

    @Override
    protected AgendamentoDTO buildDTOFromEntity(Agendamento e) {
        List<LocalDateTime> inicios = e.getHorarios().stream()
            .map(AgendamentoHorario::getDataHoraInicio).sorted().collect(Collectors.toList());
        List<LocalDateTime> fins = e.getHorarios().stream()
            .sorted(java.util.Comparator.comparing(AgendamentoHorario::getDataHoraInicio))
            .map(AgendamentoHorario::getDataHoraFim).collect(Collectors.toList());

        UUID profissionalId = null;
        String profissionalNome = null;
        if (e.getAgenda() != null && e.getAgenda().getProfissional() != null) {
            profissionalId = e.getAgenda().getProfissional().getId();
            if (e.getAgenda().getProfissional().getPessoa() != null) {
                profissionalNome = e.getAgenda().getProfissional().getPessoa().getNome();
            }
        }

        return AgendamentoDTO.builder()
            .id(e.getId())
            .agendaId(e.getAgenda() != null ? e.getAgenda().getId() : null)
            .agendaNome(e.getAgenda() != null ? e.getAgenda().getNome() : null)
            .profissionalId(profissionalId)
            .profissionalNome(profissionalNome)
            .pacienteId(e.getPaciente() != null ? e.getPaciente().getId() : null)
            .pacienteNome(e.getPaciente() != null ? e.getPaciente().getNome() : null)
            .convenioId(e.getConvenio() != null ? e.getConvenio().getId() : null)
            .convenioNome(e.getConvenio() != null ? e.getConvenio().getNome() : null)
            .categoriaId(e.getCategoria() != null ? e.getCategoria().getId() : null)
            .categoriaNome(e.getCategoria() != null ? e.getCategoria().getNome() : null)
            .procedimentoId(e.getProcedimento() != null ? e.getProcedimento().getId() : null)
            .procedimentoNome(e.getProcedimento() != null ? e.getProcedimento().getDescricao() : null)
            .observacao(e.getObservacao())
            .status(e.getStatus() != null ? e.getStatus().name() : null)
            .horariosInicio(inicios)
            .horariosFim(fins)
            .createdAt(e.getCreatedAt())
            .updatedAt(e.getUpdatedAt())
            .createdBy(e.getCreatedBy())
            .updatedBy(e.getUpdatedBy())
            .build();
    }

    @Override
    protected AgendamentoGridDTO buildGridDTOFromEntity(Agendamento e) {
        LocalDateTime primeiraDataHora = e.getHorarios().stream()
            .map(AgendamentoHorario::getDataHoraInicio)
            .min(LocalDateTime::compareTo)
            .orElse(null);

        String profissionalNome = null;
        if (e.getAgenda() != null && e.getAgenda().getProfissional() != null
                && e.getAgenda().getProfissional().getPessoa() != null) {
            profissionalNome = e.getAgenda().getProfissional().getPessoa().getNome();
        }

        return AgendamentoGridDTO.builder()
            .id(e.getId())
            .agendaNome(e.getAgenda() != null ? e.getAgenda().getNome() : null)
            .profissionalNome(profissionalNome)
            .pacienteNome(e.getPaciente() != null ? e.getPaciente().getNome() : null)
            .convenioNome(e.getConvenio() != null ? e.getConvenio().getNome() : null)
            .categoriaNome(e.getCategoria() != null ? e.getCategoria().getNome() : null)
            .procedimentoNome(e.getProcedimento() != null ? e.getProcedimento().getDescricao() : null)
            .status(e.getStatus() != null ? e.getStatus().name() : null)
            .primeiraData(primeiraDataHora != null ? primeiraDataHora.toLocalDate() : null)
            .primeiraDataHora(primeiraDataHora)
            .qtdHorarios(e.getHorarios().size())
            .deleted(e.getDeleted())
            .createdAt(e.getCreatedAt())
            .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("status");
    }

    @Override
    protected Class<Agendamento> getEntityClass() {
        return Agendamento.class;
    }

    private Agenda buscarAgenda(UUID id) {
        return agendaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Agenda não encontrada."));
    }

    private Pessoa buscarPaciente(UUID id) {
        return pessoaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));
    }

    private Convenio buscarConvenio(UUID id) {
        return convenioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Convênio não encontrado."));
    }

    private ConvenioCategoria buscarCategoria(UUID id) {
        return convenioCategoriaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
    }

    private Procedimento buscarProcedimento(UUID id) {
        return procedimentoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Procedimento não encontrado."));
    }
}
