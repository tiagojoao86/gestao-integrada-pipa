package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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

        entity.atualizar(dto.getNome(), dto.getDescricao(), dto.getCnpj());

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
                .deleted(entity.getDeleted())
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

    @Override
    public List<UnidadeNegocioDTO> listarAtivas() {
        return repository.findByAtivaTrue().stream()
                .map(this::buildDTOFromEntity)
                .toList();
    }

    @Override
    public List<UnidadeNegocioDTO> listarDisponiveisParaUsuario() {
        Set<UUID> unidadesPermitidas = br.com.grupopipa.gestaointegrada.core.Session.getUnidadeNegocioIds();

        return repository.findByAtivaTrue().stream()
                .filter(unidade -> unidadesPermitidas.isEmpty() || unidadesPermitidas.contains(unidade.getId()))
                .map(this::buildSimpleDTO)
                .toList();
    }

    @Override
    public List<UnidadeNegocioDTO> listarTodasDisponiveis() {
        return repository.findByAtivaTrue().stream()
                .map(this::buildSimpleDTO)
                .toList();
    }

    private UnidadeNegocioDTO buildSimpleDTO(UnidadeNegocio unidade) {
        return UnidadeNegocioDTO.builder()
                .id(unidade.getId())
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .build();
    }
}
