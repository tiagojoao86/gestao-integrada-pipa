package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto.ConvenioCategoriaDTO;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto.ConvenioCategoriaGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
public class ConvenioCategoriaServiceImpl
        extends CrudServiceImpl<
            ConvenioCategoriaDTO, ConvenioCategoriaGridDTO, ConvenioCategoria, ConvenioCategoriaRepository>
        implements ConvenioCategoriaService {

    private final ConvenioRepository convenioRepository;

    public ConvenioCategoriaServiceImpl(
            ConvenioCategoriaRepository repository,
            Specifications<ConvenioCategoria> specifications,
            ConvenioRepository convenioRepository) {
        super(repository, specifications);
        this.convenioRepository = convenioRepository;
    }

    @Override
    protected ConvenioCategoria mergeEntityAndDTO(ConvenioCategoria entity, ConvenioCategoriaDTO dto) {
        Convenio convenio = resolverConvenio(dto.getConvenioId());

        if (Objects.isNull(entity)) {
            return new ConvenioCategoria.Builder()
                    .convenio(convenio)
                    .nome(dto.getNome())
                    .codigoAnsPlano(dto.getCodigoAnsPlano())
                    .ativo(dto.getAtivo())
                    .build();
        }

        entity.atualizar(convenio, dto.getNome(), dto.getCodigoAnsPlano(), dto.getAtivo());
        return entity;
    }

    private Convenio resolverConvenio(UUID convenioId) {
        if (convenioId == null) {
            return null;
        }
        return convenioRepository.findById(convenioId).orElse(null);
    }

    @Override
    protected ConvenioCategoriaDTO buildDTOFromEntity(ConvenioCategoria entity) {
        return ConvenioCategoriaDTO.builder()
                .id(entity.getId())
                .convenioId(entity.getConvenio() != null ? entity.getConvenio().getId() : null)
                .convenioNome(entity.getConvenio() != null ? entity.getConvenio().getNome() : null)
                .nome(entity.getNome())
                .codigoAnsPlano(entity.getCodigoAnsPlano())
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected ConvenioCategoriaGridDTO buildGridDTOFromEntity(ConvenioCategoria entity) {
        return ConvenioCategoriaGridDTO.builder()
                .id(entity.getId())
                .convenioNome(entity.getConvenio() != null ? entity.getConvenio().getNome() : null)
                .nome(entity.getNome())
                .codigoAnsPlano(entity.getCodigoAnsPlano())
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    public List<ConvenioCategoriaGridDTO> listarPorConvenio(UUID convenioId) {
        return repository.findAllByConvenioIdAndDeletedFalseOrderByNomeAsc(convenioId)
                .stream()
                .map(this::buildGridDTOFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "codigoAnsPlano", "ativo", "createdAt");
    }

    @Override
    protected Class<ConvenioCategoria> getEntityClass() {
        return ConvenioCategoria.class;
    }
}
