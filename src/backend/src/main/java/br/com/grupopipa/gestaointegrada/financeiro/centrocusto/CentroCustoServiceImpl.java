package br.com.grupopipa.gestaointegrada.financeiro.centrocusto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CentroCusto;

@Service
public class CentroCustoServiceImpl
    extends CrudServiceImpl<CentroCustoDTO, CentroCustoGridDTO, CentroCusto, CentroCustoRepository>
    implements CentroCustoService {

  private final UnidadeNegocioRepository unidadeNegocioRepository;

  public CentroCustoServiceImpl(
      CentroCustoRepository repository,
      Specifications<CentroCusto> specifications,
      UnidadeNegocioRepository unidadeNegocioRepository) {
    super(repository, specifications);
    this.unidadeNegocioRepository = unidadeNegocioRepository;
  }

  @Override
  protected CentroCusto mergeEntityAndDTO(CentroCusto entity, CentroCustoDTO dto) {
    UUID unidadeId = dto.getUnidadeNegocioId();
    UnidadeNegocio unidadeNegocio =
        unidadeNegocioRepository
            .findById(unidadeId)
            .orElseThrow(
                () -> new EntityNotFoundException("Unidade de Negócio não encontrada", unidadeId));

    if (Objects.isNull(entity)) {
      return new CentroCusto.Builder()
          .nome(dto.getNome())
          .centroResultado(dto.getCentroResultado())
          .unidadeNegocio(unidadeNegocio)
          .build();
    }

    entity.atualizar(dto.getNome(), dto.getCentroResultado(), unidadeNegocio);
    return entity;
  }

  @Override
  protected CentroCustoDTO buildDTOFromEntity(CentroCusto entity) {
    UnidadeNegocio unidadeNegocio = entity.getUnidadeNegocio();

    return CentroCustoDTO.builder()
        .id(entity.getId())
        .nome(entity.getNome())
        .centroResultado(entity.getCentroResultado())
        .unidadeNegocioId(unidadeNegocio != null ? unidadeNegocio.getId() : null)
        .unidadeNegocioNome(unidadeNegocio != null ? unidadeNegocio.getNome() : null)
        .unidadeNegocioCodigo(unidadeNegocio != null ? unidadeNegocio.getCodigo() : null)
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .updatedBy(entity.getUpdatedBy())
        .build();
  }

  @Override
  protected CentroCustoGridDTO buildGridDTOFromEntity(CentroCusto entity) {
    UnidadeNegocio unidadeNegocio = entity.getUnidadeNegocio();

    return CentroCustoGridDTO.builder()
        .id(entity.getId())
        .nome(entity.getNome())
        .centroResultado(entity.getCentroResultado())
        .unidadeNegocioCodigo(unidadeNegocio != null ? unidadeNegocio.getCodigo() : null)
        .deleted(entity.getDeleted())
        .build();
  }

  @Override
  protected List<String> getPropertiesToFilter() {
    return List.of("nome");
  }

  @Override
  protected Class<CentroCusto> getEntityClass() {
    return CentroCusto.class;
  }
}
