package br.com.grupopipa.gestaointegrada.cadastro.modulo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.cadastro.modulo.entity.ModuloEntity;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;

@Service
public class ModuloServiceImpl extends CrudServiceImpl<ModuloDTO, ModuloGridDTO, ModuloEntity, ModuloRepository>
        implements ModuloService {

    public ModuloServiceImpl(ModuloRepository repository, Specifications<ModuloEntity> specifications) {
        super(repository, specifications);
    }

    @Override
    public List<ModuloDTO> findAllSimple() {
        return repository.findAll().stream().map(this::buildDTOFromEntity).collect(Collectors.toList());
    }

    @Override
    protected ModuloEntity mergeEntityAndDTO(ModuloEntity entity, ModuloDTO dto) {
        throw new UnsupportedOperationException("Save not supported for Modulo");
    }

    @Override
    protected ModuloDTO buildDTOFromEntity(ModuloEntity entity) {
        return ModuloDTO
                .builder()
                .id(entity.getId())
                .chave(entity.getChave())
                .nome(entity.getNome())
                .grupoEnum(entity.getGrupo())
                .build();
    }

    @Override
    protected ModuloGridDTO buildGridDTOFromEntity(ModuloEntity entity) {
        return ModuloGridDTO
                .builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .build();
    }

    @Override
    protected java.util.List<String> getPropertiesToFilter() {
        return java.util.List.of("nome", "chave");
    }

    @Override
    protected Class<ModuloEntity> getEntityClass() {
        return ModuloEntity.class;
    }

}
