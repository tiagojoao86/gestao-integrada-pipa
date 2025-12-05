package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.core.valueobject.CNPJ;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UnidadeNegocioServiceImpl
        extends CrudServiceImpl<UnidadeNegocioDTO, UnidadeNegocioGridDTO, UnidadeNegocio, UnidadeNegocioRepository>
        implements UnidadeNegocioService {

    public UnidadeNegocioServiceImpl(UnidadeNegocioRepository repository,
            Specifications<UnidadeNegocio> specifications) {
        super(repository, specifications);
    }

    @Override
    protected UnidadeNegocio mergeEntityAndDTO(UnidadeNegocio entity, UnidadeNegocioDTO dto) {
        if (Objects.isNull(entity)) {
            entity = new UnidadeNegocio.Builder()
                    .codigo(dto.getCodigo())
                    .nome(dto.getNome())
                    .descricao(dto.getDescricao())
                    .cnpj(dto.getCnpj())
                    .build();
            return entity;
        }

        CNPJ cnpj = null;
        if (dto.getCnpj() != null && !dto.getCnpj().isBlank()) {
            cnpj = new CNPJ(dto.getCnpj());
        }

        entity.atualizar(dto.getNome(), dto.getDescricao(), cnpj);

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
                .cnpj(entity.getCnpj())
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
                .cnpj(entity.getCnpj())
                .ativa(entity.getAtiva())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("codigo", "nome", "cnpj", "ativa");
    }

    @Override
    protected Class<UnidadeNegocio> getEntityClass() {
        return UnidadeNegocio.class;
    }
}
