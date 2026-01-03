package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;

@Service
public class TituloCategoriaServiceImpl
        extends CrudServiceImpl<TituloCategoriaDTO, TituloCategoriaGridDTO, TituloCategoria, TituloCategoriaRepository>
        implements TituloCategoriaService {

    public TituloCategoriaServiceImpl(
            TituloCategoriaRepository repository, Specifications<TituloCategoria> specifications) {
        super(repository, specifications);
    }

    @Override
    protected TituloCategoria mergeEntityAndDTO(TituloCategoria entity, TituloCategoriaDTO dto) {
        // Buscar agrupador se fornecido
        TituloCategoria agrupador = null;
        if (dto.getAgrupadorId() != null) {
            agrupador = repository
                    .findById(dto.getAgrupadorId())
                    .orElseThrow(() -> new IllegalArgumentException("Agrupador não encontrado"));
        }

        if (entity == null) {
            return new TituloCategoria.Builder()
                    .codigo(dto.getCodigo())
                    .nome(dto.getNome())
                    .descricao(dto.getDescricao())
                    .tipo(dto.getTipo())
                    .agrupador(agrupador)
                    .build();
        }

        // update existing
        entity.atualizar(dto.getCodigo(), dto.getNome(), dto.getDescricao(), dto.getTipo(), agrupador);
        return entity;
    }

    @Override
    protected TituloCategoriaDTO buildDTOFromEntity(TituloCategoria entity) {
        return TituloCategoriaDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .nome(entity.getNome().getValue())
                .descricao(entity.getDescricao())
                .tipo(entity.getTipo())
                .agrupadorId(entity.getAgrupador() != null ? entity.getAgrupador().getId() : null)
                .agrupadorNome(
                        entity.getAgrupador() != null ? entity.getAgrupador().getNome().getValue() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected TituloCategoriaGridDTO buildGridDTOFromEntity(TituloCategoria entity) {
        return TituloCategoriaGridDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .nome(entity.getNome().getValue())
                .descricao(entity.getDescricao())
                .tipo(entity.getTipo())
                .agrupadorNome(
                        entity.getAgrupador() != null ? entity.getAgrupador().getNome().getValue() : null)
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("codigo", "nome", "descricao", "tipo");
    }

    @Override
    protected Class<TituloCategoria> getEntityClass() {
        return TituloCategoria.class;
    }
}
