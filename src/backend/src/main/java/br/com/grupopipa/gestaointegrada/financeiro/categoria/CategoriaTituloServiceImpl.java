package br.com.grupopipa.gestaointegrada.financeiro.categoria;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CategoriaTitulo;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaTituloServiceImpl
        extends CrudServiceImpl<CategoriaTituloDTO, CategoriaTituloGridDTO, CategoriaTitulo, CategoriaTituloRepository>
        implements CategoriaTituloService {

    public CategoriaTituloServiceImpl(CategoriaTituloRepository repository,
            Specifications<CategoriaTitulo> specifications) {
        super(repository, specifications);
    }

    @Override
    protected CategoriaTitulo mergeEntityAndDTO(CategoriaTitulo entity, CategoriaTituloDTO dto) {
        if (entity == null) {
            return new CategoriaTitulo.Builder()
                    .nome(dto.getNome())
                    .descricao(dto.getDescricao())
                    .build();
        }

        // update existing
        entity.atualizar(dto.getNome(), dto.getDescricao());
        return entity;
    }

    @Override
    protected CategoriaTituloDTO buildDTOFromEntity(CategoriaTitulo entity) {
        return CategoriaTituloDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .descricao(entity.getDescricao())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected CategoriaTituloGridDTO buildGridDTOFromEntity(CategoriaTitulo entity) {
        return CategoriaTituloGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .descricao(entity.getDescricao())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "descricao");
    }

    @Override
    protected Class<CategoriaTitulo> getEntityClass() {
        return CategoriaTitulo.class;
    }
}
