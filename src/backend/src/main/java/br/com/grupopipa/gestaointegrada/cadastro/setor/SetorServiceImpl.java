package br.com.grupopipa.gestaointegrada.cadastro.setor;

import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.centrocusto.CentroCustoRepository;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CentroCusto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SetorServiceImpl extends CrudServiceImpl<SetorDTO, SetorGridDTO, Setor, SetorRepository>
        implements SetorService {

    private final CentroCustoRepository centroCustoRepository;

    public SetorServiceImpl(SetorRepository repository,
                            Specifications<Setor> specifications,
                            CentroCustoRepository centroCustoRepository) {
        super(repository, specifications);
        this.centroCustoRepository = centroCustoRepository;
    }

    @Override
    protected Setor mergeEntityAndDTO(Setor entity, SetorDTO dto) {
        UUID centroCustoId = dto.getCentroCustoId();
        CentroCusto centroCusto = centroCustoRepository.findById(centroCustoId)
                .orElseThrow(() -> new EntityNotFoundException("Centro de Custo não encontrado", centroCustoId));

        if (Objects.isNull(entity)) {
            return new Setor.Builder()
                    .nome(dto.getNome())
                    .descricao(dto.getDescricao())
                    .centroCusto(centroCusto)
                    .build();
        }

        entity.atualizar(dto.getNome(), dto.getDescricao(), centroCusto);
        return entity;
    }

    @Override
    protected SetorDTO buildDTOFromEntity(Setor entity) {
        CentroCusto centroCusto = entity.getCentroCusto();

        return SetorDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .descricao(entity.getDescricao())
                .centroCustoId(centroCusto != null ? centroCusto.getId() : null)
                .centroCustoNome(centroCusto != null ? centroCusto.getNome() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected SetorGridDTO buildGridDTOFromEntity(Setor entity) {
        CentroCusto centroCusto = entity.getCentroCusto();

        return SetorGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .descricao(entity.getDescricao())
                .centroCustoNome(centroCusto != null ? centroCusto.getNome() : null)
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome");
    }

    @Override
    protected Class<Setor> getEntityClass() {
        return Setor.class;
    }
}
