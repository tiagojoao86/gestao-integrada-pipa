package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TituloCategoriaServiceImpl
        extends CrudServiceImpl<TituloCategoriaDTO, TituloCategoriaGridDTO, TituloCategoria, TituloCategoriaRepository>
        implements TituloCategoriaService {

    public TituloCategoriaServiceImpl(TituloCategoriaRepository repository,
            Specifications<TituloCategoria> specifications) {
        super(repository, specifications);
    }

    @Override
    protected TituloCategoria mergeEntityAndDTO(TituloCategoria entity, TituloCategoriaDTO dto) {
        if (entity == null) {
            return new TituloCategoria.Builder()
                    .nome(dto.getNome())
                    .descricao(dto.getDescricao())
                    .tipo(dto.getTipo())
                    .build();
        }

        // update existing
        entity.atualizar(dto.getNome(), dto.getDescricao(), dto.getTipo());
        return entity;
    }

    @Override
    protected TituloCategoriaDTO buildDTOFromEntity(TituloCategoria entity) {
        return TituloCategoriaDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome().getValue())
                .descricao(entity.getDescricao())
                .tipo(entity.getTipo())
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
                .nome(entity.getNome()
                        .getValue())
                .descricao(entity.getDescricao())
                .tipo(entity.getTipo())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "descricao", "tipo");
    }

    @Override
    protected Class<TituloCategoria> getEntityClass() {
        return TituloCategoria.class;
    }
}
