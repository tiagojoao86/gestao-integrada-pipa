package br.com.grupopipa.gestaointegrada.atendimento.tabela;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoRepository;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.dto.TabelaDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.dto.TabelaGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.dto.TabelaItemDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.Tabela;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.TabelaItem;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import jakarta.transaction.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class TabelaServiceImpl
        extends CrudServiceImpl<TabelaDTO, TabelaGridDTO, Tabela, TabelaRepository>
        implements TabelaService {

    private final TabelaItemRepository tabelaItemRepository;
    private final ProcedimentoRepository procedimentoRepository;

    public TabelaServiceImpl(
            TabelaRepository repository,
            Specifications<Tabela> specifications,
            TabelaItemRepository tabelaItemRepository,
            ProcedimentoRepository procedimentoRepository) {
        super(repository, specifications);
        this.tabelaItemRepository = tabelaItemRepository;
        this.procedimentoRepository = procedimentoRepository;
    }

    @Override
    @Transactional
    public TabelaDTO save(TabelaDTO dto) {
        TabelaDTO saved = super.save(dto);
        Tabela tabela = this.findEntityById(saved.getId());
        syncItens(tabela, dto.getItens());
        saved.setItens(loadItensDTO(saved.getId()));
        return saved;
    }

    @Override
    protected Tabela mergeEntityAndDTO(Tabela entity, TabelaDTO dto) {
        if (Objects.isNull(entity)) {
            return new Tabela.Builder()
                    .nome(dto.getNome())
                    .tipo(dto.getTipo())
                    .ativo(dto.getAtivo())
                    .build();
        }
        entity.atualizar(dto.getNome(), dto.getTipo(), dto.getAtivo());
        return entity;
    }

    @Override
    protected TabelaDTO buildDTOFromEntity(Tabela entity) {
        return TabelaDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .tipo(entity.getTipo())
                .ativo(entity.getAtivo())
                .itens(entity.getId() != null ? loadItensDTO(entity.getId()) : List.of())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected TabelaGridDTO buildGridDTOFromEntity(Tabela entity) {
        return TabelaGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .tipo(entity.getTipo())
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<TabelaGridDTO> listarAtivas() {
        return repository.findAllByAtivoTrueAndDeletedFalseOrderByNomeAsc()
            .stream()
            .map(this::buildGridDTOFromEntity)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "tipo", "ativo", "createdAt");
    }

    @Override
    protected Class<Tabela> getEntityClass() {
        return Tabela.class;
    }

    // =========================================================================
    // Gestão inline de TabelaItem
    // =========================================================================

    private List<TabelaItemDTO> loadItensDTO(UUID tabelaId) {
        return tabelaItemRepository.findAllByTabelaId(tabelaId).stream()
                .map(this::toItemDTO)
                .toList();
    }

    private TabelaItemDTO toItemDTO(TabelaItem item) {
        return TabelaItemDTO.builder()
                .id(item.getId())
                .tabelaId(item.getTabela().getId())
                .procedimentoId(item.getProcedimento().getId())
                .procedimentoCodigo(item.getProcedimento().getCodigo())
                .procedimentoDescricao(item.getProcedimento().getDescricao())
                .valor(item.getValor())
                .vigenciaInicio(item.getVigenciaInicio())
                .vigenciaFim(item.getVigenciaFim())
                .build();
    }

    private void syncItens(Tabela tabela, List<TabelaItemDTO> novosItens) {
        if (novosItens == null) {
            return;
        }

        List<TabelaItem> existentes = tabelaItemRepository.findAllByTabelaId(tabela.getId());

        // Determina IDs enviados (não nulos = item existente a manter)
        List<UUID> idsEnviados = novosItens.stream()
                .map(TabelaItemDTO::getId)
                .filter(Objects::nonNull)
                .toList();

        // Deleta os que foram removidos
        existentes.stream()
                .filter(e -> !idsEnviados.contains(e.getId()))
                .forEach(e -> tabelaItemRepository.deleteById(e.getId()));

        // Adiciona/atualiza
        for (TabelaItemDTO itemDTO : novosItens) {
            Procedimento procedimento = resolverProcedimento(itemDTO.getProcedimentoId());
            validarVigencia(tabela.getId(), procedimento, itemDTO);

            if (itemDTO.getId() == null) {
                TabelaItem novo = new TabelaItem.Builder()
                        .tabela(tabela)
                        .procedimento(procedimento)
                        .valor(itemDTO.getValor())
                        .vigenciaInicio(itemDTO.getVigenciaInicio())
                        .vigenciaFim(itemDTO.getVigenciaFim())
                        .build();
                tabelaItemRepository.save(novo);
            } else {
                tabelaItemRepository.findById(itemDTO.getId()).ifPresent(item ->
                    item.atualizar(itemDTO.getValor(), itemDTO.getVigenciaInicio(), itemDTO.getVigenciaFim())
                );
            }
        }
    }

    private Procedimento resolverProcedimento(UUID procedimentoId) {
        if (procedimentoId == null) {
            return null;
        }
        return procedimentoRepository.findById(procedimentoId).orElse(null);
    }

    private void validarVigencia(UUID tabelaId, Procedimento procedimento, TabelaItemDTO itemDTO) {
        if (procedimento == null || itemDTO.getVigenciaInicio() == null) {
            return;
        }
        LocalDate referencia = itemDTO.getVigenciaFim() != null
                ? itemDTO.getVigenciaFim()
                : itemDTO.getVigenciaInicio();

        List<TabelaItem> conflitantes = tabelaItemRepository.findItensAtivosConflitantes(
                tabelaId, procedimento.getId(), referencia, itemDTO.getId());

        if (!conflitantes.isEmpty()) {
            Set<BeanValidationMessage> violations = new HashSet<>();
            violations.add(new BeanValidationMessage(
                    "vigencia",
                    "Já existe um item ativo para o procedimento '" + procedimento.getCodigo()
                    + "' nesta tabela no período informado."));
            throw new BeanValidationException("tabelaItem", violations);
        }
    }

    // =========================================================================
    // Consulta pública para resolução de tabela item vigente
    // =========================================================================

    public List<TabelaItem> findItensAtivos(UUID tabelaId) {
        return tabelaItemRepository.findAllByTabelaId(tabelaId).stream()
                .filter(i -> i.getVigenciaFim() == null || !i.getVigenciaFim().isBefore(LocalDate.now()))
                .toList();
    }
}
