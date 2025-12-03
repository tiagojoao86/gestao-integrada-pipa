package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UnidadeNegocioServiceImpl extends CrudServiceImpl<UnidadeNegocioDTO, UnidadeNegocioGridDTO, UnidadeNegocio, UnidadeNegocioRepository>
        implements UnidadeNegocioService {

    public UnidadeNegocioServiceImpl(UnidadeNegocioRepository repository, Specifications<UnidadeNegocio> specifications) {
        super(repository, specifications);
    }

    @Override
    protected UnidadeNegocio mergeEntityAndDTO(UnidadeNegocio entity, UnidadeNegocioDTO dto) {
        if (Objects.isNull(entity)) {
            entity = new UnidadeNegocio(dto.getCodigo(), dto.getNome(), dto.getDescricao());
            return entity;
        }

        entity.atualizar(dto.getNome(), dto.getDescricao());
        
        if (dto.getAtiva() != null) {
            if (dto.getAtiva()) {
                entity.ativar();
            } else {
                entity.inativar();
            }
        }

        return entity;
    }

    @Override
    protected UnidadeNegocioDTO buildDTOFromEntity(UnidadeNegocio entity) {
        return UnidadeNegocioDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .nome(entity.getNome())
                .descricao(entity.getDescricao())
                .ativa(entity.getAtiva())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected UnidadeNegocioGridDTO buildGridDTOFromEntity(UnidadeNegocio entity) {
        return UnidadeNegocioGridDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .nome(entity.getNome())
                .ativa(entity.getAtiva())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("codigo", "nome", "ativa");
    }

    @Override
    protected Class<UnidadeNegocio> getEntityClass() {
        return UnidadeNegocio.class;
    }
}
