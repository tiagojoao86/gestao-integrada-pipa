package br.com.grupopipa.gestaointegrada.atendimento.tabelaregra;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.ConvenioCategoriaRepository;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.TabelaItemRepository;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.TabelaRepository;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.Tabela;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.TabelaItem;
import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto.ResolverProcedimentoResponse;
import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto.TabelaRegraDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto.TabelaRegraGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.entity.TabelaRegra;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
@Transactional(readOnly = true)
public class TabelaRegraServiceImpl
        extends CrudServiceImpl<TabelaRegraDTO, TabelaRegraGridDTO, TabelaRegra, TabelaRegraRepository>
        implements TabelaRegraService {

    private final ConvenioRepository convenioRepository;
    private final ConvenioCategoriaRepository convenioCategoriaRepository;
    private final TabelaRepository tabelaRepository;
    private final TabelaItemRepository tabelaItemRepository;

    public TabelaRegraServiceImpl(
            TabelaRegraRepository repository,
            Specifications<TabelaRegra> specifications,
            ConvenioRepository convenioRepository,
            ConvenioCategoriaRepository convenioCategoriaRepository,
            TabelaRepository tabelaRepository,
            TabelaItemRepository tabelaItemRepository) {
        super(repository, specifications);
        this.convenioRepository = convenioRepository;
        this.convenioCategoriaRepository = convenioCategoriaRepository;
        this.tabelaRepository = tabelaRepository;
        this.tabelaItemRepository = tabelaItemRepository;
    }

    @Override
    protected TabelaRegra mergeEntityAndDTO(TabelaRegra entity, TabelaRegraDTO dto) {
        Convenio convenio = buscarConvenio(dto.getConvenioId());
        ConvenioCategoria categoria = buscarCategoria(dto.getConvenioCategoriaId());
        Tabela tabela = buscarTabela(dto.getTabelaId());

        if (Objects.isNull(entity)) {
            return new TabelaRegra.Builder()
                    .convenio(convenio)
                    .convenioCategoria(categoria)
                    .tabela(tabela)
                    .build();
        }
        entity.atualizar(convenio, categoria, tabela);
        return entity;
    }

    @Override
    public ResolverProcedimentoResponse resolverProcedimento(
            UUID convenioId,
            UUID convenioCategoriaId,
            UUID procedimentoId,
            LocalDate dataReferencia) {

        if (convenioId == null) {
            Set<BeanValidationMessage> v = new HashSet<>();
            v.add(new BeanValidationMessage("convenio",
                "Este atendimento não possui convênio. Selecione um convênio ou configure o Particular."));
            throw new BeanValidationException("tabelaRegra", v);
        }

        Tabela tabela = resolverTabela(convenioId, convenioCategoriaId);
        LocalDate data = dataReferencia != null ? dataReferencia : LocalDate.now();
        TabelaItem item = tabelaItemRepository
            .findItemVigente(tabela.getId(), procedimentoId, data)
            .orElseThrow(() -> {
                Set<BeanValidationMessage> v = new HashSet<>();
                v.add(new BeanValidationMessage("procedimento",
                    "Procedimento não encontrado na tabela vigente do convênio."));
                return new BeanValidationException("tabelaRegra", v);
            });

        return ResolverProcedimentoResponse.builder()
                .tabelaItemId(item.getId())
                .valor(item.getValor())
                .build();
    }

    private Tabela resolverTabela(UUID convenioId, UUID convenioCategoriaId) {
        if (convenioCategoriaId != null) {
            Optional<TabelaRegra> regra = repository
                .findByConvenioAndCategoria(convenioId, convenioCategoriaId);
            if (regra.isPresent()) {
                return regra.get().getTabela();
            }
        }
        return repository.findByConvenioSemCategoria(convenioId)
            .map(TabelaRegra::getTabela)
            .orElseThrow(() -> {
                Set<BeanValidationMessage> v = new HashSet<>();
                v.add(new BeanValidationMessage("tabelaRegra",
                    "Sem tabela de preços configurada para este convênio."));
                return new BeanValidationException("tabelaRegra", v);
            });
    }

    private Convenio buscarConvenio(UUID id) {
        if (id == null) {
            return null;
        }
        return convenioRepository.findById(id).orElse(null);
    }

    private ConvenioCategoria buscarCategoria(UUID id) {
        if (id == null) {
            return null;
        }
        return convenioCategoriaRepository.findById(id).orElse(null);
    }

    private Tabela buscarTabela(UUID id) {
        if (id == null) {
            return null;
        }
        return tabelaRepository.findById(id).orElse(null);
    }

    @Override
    protected TabelaRegraDTO buildDTOFromEntity(TabelaRegra entity) {
        return TabelaRegraDTO.builder()
                .id(entity.getId())
                .convenioId(entity.getConvenio() != null ? entity.getConvenio().getId() : null)
                .convenioNome(entity.getConvenio() != null ? entity.getConvenio().getNome() : null)
                .convenioCategoriaId(entity.getConvenioCategoria() != null
                    ? entity.getConvenioCategoria().getId() : null)
                .convenioCategoriaNome(entity.getConvenioCategoria() != null
                    ? entity.getConvenioCategoria().getNome() : null)
                .tabelaId(entity.getTabela() != null ? entity.getTabela().getId() : null)
                .tabelaNome(entity.getTabela() != null ? entity.getTabela().getNome() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected TabelaRegraGridDTO buildGridDTOFromEntity(TabelaRegra entity) {
        return TabelaRegraGridDTO.builder()
                .id(entity.getId())
                .convenioNome(entity.getConvenio() != null ? entity.getConvenio().getNome() : null)
                .convenioCategoriaNome(entity.getConvenioCategoria() != null
                    ? entity.getConvenioCategoria().getNome() : null)
                .tabelaNome(entity.getTabela() != null ? entity.getTabela().getNome() : null)
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("createdAt");
    }

    @Override
    protected Class<TabelaRegra> getEntityClass() {
        return TabelaRegra.class;
    }
}
