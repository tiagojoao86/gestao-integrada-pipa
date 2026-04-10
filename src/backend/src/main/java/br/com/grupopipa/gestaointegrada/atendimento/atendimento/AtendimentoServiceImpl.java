package br.com.grupopipa.gestaointegrada.atendimento.atendimento;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto.AtendimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.atendimento.dto.AtendimentoGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity.Atendimento;
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

import java.util.HashSet;
import java.util.Set;

@Service
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
        Setor setor = resolverSetor(dto.getSetorId());
        Pessoa paciente = resolverPessoa(dto.getPacienteId());
        Pessoa responsavel = resolverResponsavel(dto.getResponsavelId(), paciente);
        Convenio convenio = resolverConvenio(dto.getConvenioId());
        ConvenioCategoria convenioCategoria = resolverCategoria(dto.getConvenioCategoriaId(), convenio);
        Profissional profAtendimento = resolverProfissional(dto.getProfissionalAtendimentoId());
        Profissional profResponsavel = resolverProfissional(dto.getProfissionalResponsavelId());
        Procedimento procedimento = resolverProcedimento(dto.getProcedimentoId());
        TabelaItem tabelaItem = resolverTabelaItem(
            dto.getTabelaItemId(), procedimento, dto.getDataHora(), convenio);

        if (Objects.isNull(entity)) {
            return new Atendimento.Builder()
                    .dataHora(dto.getDataHora())
                    .setor(setor)
                    .paciente(paciente)
                    .responsavel(responsavel)
                    .convenio(convenio)
                    .convenioCategoria(convenioCategoria)
                    .profissionalAtendimento(profAtendimento)
                    .profissionalResponsavel(profResponsavel)
                    .procedimento(procedimento)
                    .tabelaItem(tabelaItem)
                    .status(dto.getStatus() != null ? dto.getStatus() : StatusAtendimento.AGENDADO)
                    .observacoes(dto.getObservacoes())
                    .build();
        }

        entity.atualizar(
            dto.getDataHora(), setor, paciente, responsavel,
            convenio, convenioCategoria,
            profAtendimento, profResponsavel,
            procedimento, tabelaItem,
            dto.getStatus() != null ? dto.getStatus() : entity.getStatus(),
            dto.getObservacoes()
        );
        return entity;
    }

    @Override
    protected AtendimentoDTO buildDTOFromEntity(Atendimento entity) {
        return AtendimentoDTO.builder()
                .id(entity.getId())
                .dataHora(entity.getDataHora())
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
                .procedimentoId(entity.getProcedimento() != null ? entity.getProcedimento().getId() : null)
                .procedimentoCodigo(entity.getProcedimento() != null
                    ? entity.getProcedimento().getCodigo() : null)
                .procedimentoDescricao(entity.getProcedimento() != null
                    ? entity.getProcedimento().getDescricao() : null)
                .tabelaItemId(entity.getTabelaItem() != null ? entity.getTabelaItem().getId() : null)
                .tabelaItemValor(entity.getTabelaItem() != null ? entity.getTabelaItem().getValor() : null)
                .status(entity.getStatus())
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
                .dataHora(entity.getDataHora())
                .pacienteNome(entity.getPaciente() != null ? entity.getPaciente().getNome() : null)
                .profissionalAtendimentoNome(entity.getProfissionalAtendimento() != null
                    ? entity.getProfissionalAtendimento().getPessoa().getNome() : null)
                .procedimentoCodigo(entity.getProcedimento() != null
                    ? entity.getProcedimento().getCodigo() : null)
                .convenioNome(entity.getConvenio() != null ? entity.getConvenio().getNome() : null)
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("dataHora", "status", "createdAt");
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

    private Procedimento resolverProcedimento(UUID id) {
        if (id == null) return null;
        return procedimentoRepository.findById(id).orElse(null);
    }

    private TabelaItem resolverTabelaItem(
            UUID tabelaItemId,
            Procedimento procedimento,
            java.time.LocalDateTime dataHora,
            Convenio convenio) {
        // Se foi enviado explicitamente, usar
        if (tabelaItemId != null) {
            return tabelaItemRepository.findById(tabelaItemId).orElse(null);
        }
        // Tentar resolver automaticamente
        if (procedimento == null || dataHora == null) {
            return null;
        }
        java.time.LocalDate data = dataHora.toLocalDate();
        // Busca em todas as tabelas vigentes para o procedimento na data
        return tabelaItemRepository
            .findItemVigenteParaProcedimento(procedimento.getId(), data, convenio != null)
            .orElse(null);
    }
}
