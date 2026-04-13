package br.com.grupopipa.gestaointegrada.atendimento.atendimento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto.AtendimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto.AtendimentoGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto.AtendimentoProcedimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity.Atendimento;
import br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity.AtendimentoProcedimento;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.ConvenioCategoriaRepository;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoRepository;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.ProfissionalRepository;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.TabelaItemRepository;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.TabelaItem;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.setor.SetorRepository;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
@SuppressWarnings("checkstyle:MagicNumber")
public class AtendimentoServiceImpl
        extends CrudServiceImpl<AtendimentoDTO, AtendimentoGridDTO, Atendimento, AtendimentoRepository>
        implements AtendimentoService {

    private final PessoaRepository pessoaRepository;
    private final SetorRepository setorRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ConvenioRepository convenioRepository;
    private final ConvenioCategoriaRepository convenioCategoriaRepository;
    private final ProcedimentoRepository procedimentoRepository;
    private final TabelaItemRepository tabelaItemRepository;

    public AtendimentoServiceImpl(
            AtendimentoRepository repository,
            Specifications<Atendimento> specifications,
            PessoaRepository pessoaRepository,
            SetorRepository setorRepository,
            ProfissionalRepository profissionalRepository,
            ConvenioRepository convenioRepository,
            ConvenioCategoriaRepository convenioCategoriaRepository,
            ProcedimentoRepository procedimentoRepository,
            TabelaItemRepository tabelaItemRepository) {
        super(repository, specifications);
        this.pessoaRepository = pessoaRepository;
        this.setorRepository = setorRepository;
        this.profissionalRepository = profissionalRepository;
        this.convenioRepository = convenioRepository;
        this.convenioCategoriaRepository = convenioCategoriaRepository;
        this.procedimentoRepository = procedimentoRepository;
        this.tabelaItemRepository = tabelaItemRepository;
    }

    @Override
    protected Atendimento mergeEntityAndDTO(Atendimento entity, AtendimentoDTO dto) {
        LocalDateTime dataInicio = dto.getDataInicio();
        LocalDateTime dataFim = dto.getDataFim() != null ? dto.getDataFim() : calcularDataFim(dataInicio);

        Setor setor = resolverSetor(dto.getSetorId());
        Pessoa paciente = resolverPessoa(dto.getPacienteId());
        Pessoa responsavel = resolverResponsavel(dto.getResponsavelId(), paciente);
        Convenio convenio = resolverConvenio(dto.getConvenioId());
        ConvenioCategoria convenioCategoria = resolverCategoria(dto.getConvenioCategoriaId(), convenio);
        Profissional profAtendimento = resolverProfissional(dto.getProfissionalAtendimentoId());
        Profissional profResponsavel = resolverProfissional(dto.getProfissionalResponsavelId());

        validarProcedimentos(dto.getProcedimentos());

        Atendimento atendimento;
        if (Objects.isNull(entity)) {
            atendimento = new Atendimento.Builder()
                    .dataInicio(dataInicio)
                    .dataFim(dataFim)
                    .setor(setor)
                    .paciente(paciente)
                    .responsavel(responsavel)
                    .convenio(convenio)
                    .convenioCategoria(convenioCategoria)
                    .profissionalAtendimento(profAtendimento)
                    .profissionalResponsavel(profResponsavel)
                    .observacoes(dto.getObservacoes())
                    .build();
        } else {
            entity.atualizar(
                dataInicio, dataFim, setor, paciente, responsavel,
                convenio, convenioCategoria,
                profAtendimento, profResponsavel,
                dto.getObservacoes()
            );
            atendimento = entity;
        }

        List<AtendimentoProcedimento> procedimentos = resolverProcedimentos(
            dto.getProcedimentos(), atendimento, dataInicio, dataFim, convenio);
        atendimento.syncProcedimentos(procedimentos);

        return atendimento;
    }

    private LocalDateTime calcularDataFim(LocalDateTime dataInicio) {
        if (dataInicio == null) {
            return LocalDate.now().atTime(LocalTime.of(23, 59, 59));
        }
        return dataInicio.toLocalDate().atTime(LocalTime.of(23, 59, 59));
    }

    @Override
    protected AtendimentoDTO buildDTOFromEntity(Atendimento entity) {
        List<AtendimentoProcedimentoDTO> procedimentos = entity.getProcedimentos().stream()
                .map(ap -> AtendimentoProcedimentoDTO.builder()
                        .id(ap.getId())
                        .procedimentoId(ap.getProcedimento() != null ? ap.getProcedimento().getId() : null)
                        .procedimentoCodigo(ap.getProcedimento() != null
                            ? ap.getProcedimento().getCodigo() : null)
                        .procedimentoDescricao(ap.getProcedimento() != null
                            ? ap.getProcedimento().getDescricao() : null)
                        .convenioId(ap.getConvenio() != null ? ap.getConvenio().getId() : null)
                        .convenioNome(ap.getConvenio() != null ? ap.getConvenio().getNome() : null)
                        .tabelaItemId(ap.getTabelaItem() != null ? ap.getTabelaItem().getId() : null)
                        .tabelaItemValor(ap.getTabelaItem() != null ? ap.getTabelaItem().getValor() : null)
                        .dataInicio(ap.getDataInicio())
                        .dataFim(ap.getDataFim())
                        .build())
                .toList();

        return AtendimentoDTO.builder()
                .id(entity.getId())
                .dataInicio(entity.getDataInicio())
                .dataFim(entity.getDataFim())
                .setorId(entity.getSetor() != null ? entity.getSetor().getId() : null)
                .setorNome(entity.getSetor() != null ? entity.getSetor().getNome() : null)
                .pacienteId(entity.getPaciente() != null ? entity.getPaciente().getId() : null)
                .pacienteNome(entity.getPaciente() != null ? entity.getPaciente().getNome() : null)
                .responsavelId(entity.getResponsavel() != null ? entity.getResponsavel().getId() : null)
                .responsavelNome(entity.getResponsavel() != null ? entity.getResponsavel().getNome() : null)
                .convenioId(entity.getConvenio() != null ? entity.getConvenio().getId() : null)
                .convenioNome(entity.getConvenio() != null ? entity.getConvenio().getNome() : null)
                .convenioCategoriaId(entity.getConvenioCategoria() != null
                    ? entity.getConvenioCategoria().getId() : null)
                .convenioCategoriaNome(entity.getConvenioCategoria() != null
                    ? entity.getConvenioCategoria().getNome() : null)
                .profissionalAtendimentoId(entity.getProfissionalAtendimento() != null
                    ? entity.getProfissionalAtendimento().getId() : null)
                .profissionalAtendimentoNome(entity.getProfissionalAtendimento() != null
                    ? entity.getProfissionalAtendimento().getPessoa().getNome() : null)
                .profissionalResponsavelId(entity.getProfissionalResponsavel() != null
                    ? entity.getProfissionalResponsavel().getId() : null)
                .profissionalResponsavelNome(entity.getProfissionalResponsavel() != null
                    ? entity.getProfissionalResponsavel().getPessoa().getNome() : null)
                .procedimentos(procedimentos)
                .observacoes(entity.getObservacoes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected AtendimentoGridDTO buildGridDTOFromEntity(Atendimento entity) {
        return AtendimentoGridDTO.builder()
                .id(entity.getId())
                .dataInicio(entity.getDataInicio())
                .pacienteNome(entity.getPaciente() != null ? entity.getPaciente().getNome() : null)
                .profissionalAtendimentoNome(entity.getProfissionalAtendimento() != null
                    ? entity.getProfissionalAtendimento().getPessoa().getNome() : null)
                .procedimentosCount(entity.getProcedimentos().size())
                .convenioNome(entity.getConvenio() != null ? entity.getConvenio().getNome() : null)
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("dataInicio", "createdAt");
    }

    @Override
    protected Class<Atendimento> getEntityClass() {
        return Atendimento.class;
    }

    // =========================================================================
    // Resolvers
    // =========================================================================

    private Setor resolverSetor(UUID id) {
        if (id == null) return null;
        return setorRepository.findById(id).orElse(null);
    }

    private Pessoa resolverPessoa(UUID id) {
        if (id == null) return null;
        return pessoaRepository.findById(id).orElse(null);
    }

    private Pessoa resolverResponsavel(UUID responsavelId, Pessoa paciente) {
        if (responsavelId != null) {
            return pessoaRepository.findById(responsavelId).orElse(null);
        }
        if (paciente != null) {
            return paciente.getResponsavel();
        }
        return null;
    }

    private Convenio resolverConvenio(UUID id) {
        if (id == null) return null;
        return convenioRepository.findById(id).orElse(null);
    }

    private ConvenioCategoria resolverCategoria(UUID categoriaId, Convenio convenio) {
        if (categoriaId == null) return null;
        ConvenioCategoria categoria = convenioCategoriaRepository.findById(categoriaId).orElse(null);
        if (categoria != null && convenio != null) {
            if (!categoria.getConvenio().getId().equals(convenio.getId())) {
                Set<BeanValidationMessage> violations = new HashSet<>();
                violations.add(new BeanValidationMessage(
                    "convenioCategoria", "A categoria não pertence ao convênio selecionado."));
                throw new BeanValidationException("atendimento", violations);
            }
        }
        return categoria;
    }

    private Profissional resolverProfissional(UUID id) {
        if (id == null) return null;
        return profissionalRepository.findById(id).orElse(null);
    }

    private void validarProcedimentos(List<AtendimentoProcedimentoDTO> procedimentos) {
        if (procedimentos == null || procedimentos.isEmpty()) {
            Set<BeanValidationMessage> violations = new HashSet<>();
            violations.add(new BeanValidationMessage(
                "procedimentos", "Informe ao menos um procedimento."));
            throw new BeanValidationException("atendimento", violations);
        }
    }

    private List<AtendimentoProcedimento> resolverProcedimentos(
            List<AtendimentoProcedimentoDTO> dtos,
            Atendimento atendimento,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Convenio convenioAtendimento) {
        List<AtendimentoProcedimento> result = new ArrayList<>();
        for (AtendimentoProcedimentoDTO dto : dtos) {
            Procedimento procedimento = procedimentoRepository.findById(dto.getProcedimentoId())
                    .orElse(null);
            Convenio convenioProc = dto.getConvenioId() != null
                    ? convenioRepository.findById(dto.getConvenioId()).orElse(convenioAtendimento)
                    : convenioAtendimento;
            TabelaItem tabelaItem = resolverTabelaItem(
                dto.getTabelaItemId(), procedimento, dataInicio, convenioProc);
            LocalDateTime procInicio = dto.getDataInicio() != null ? dto.getDataInicio() : dataInicio;
            LocalDateTime procFim = dto.getDataFim() != null ? dto.getDataFim() : dataFim;
            result.add(new AtendimentoProcedimento(
                atendimento, procedimento, convenioProc, tabelaItem, procInicio, procFim));
        }
        return result;
    }

    private TabelaItem resolverTabelaItem(
            UUID tabelaItemId,
            Procedimento procedimento,
            LocalDateTime dataInicio,
            Convenio convenio) {
        if (tabelaItemId != null) {
            return tabelaItemRepository.findById(tabelaItemId).orElse(null);
        }
        if (procedimento == null || dataInicio == null) {
            return null;
        }
        LocalDate data = dataInicio.toLocalDate();
        return tabelaItemRepository
            .findItemVigenteParaProcedimento(procedimento.getId(), data, convenio != null)
            .orElse(null);
    }
}
