package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class PlanoContasServiceImpl extends CrudServiceImpl<PlanoContasDTO, PlanoContasGridDTO, PlanoContas, PlanoContasRepository>
        implements PlanoContasService {

    public PlanoContasServiceImpl(PlanoContasRepository repository, Specifications<PlanoContas> specifications) {
        super(repository, specifications);
    }

    @Override
    protected PlanoContas mergeEntityAndDTO(PlanoContas entity, PlanoContasDTO dto) {
        if (Objects.isNull(entity)) {
            TipoPlanoContas tipo = TipoPlanoContas.valueOf(dto.getTipo());
            entity = new PlanoContas(dto.getCodigo(), dto.getDescricao(), tipo);
            
            if (dto.getPlanoPaiId() != null) {
                PlanoContas planoPai = repository.findById(dto.getPlanoPaiId())
                        .orElseThrow(() -> new IllegalArgumentException("Plano pai não encontrado"));
                entity.definirPlanoPai(planoPai);
            }
            
            return entity;
        }

        entity.atualizar(dto.getDescricao());
        
        if (dto.getPlanoPaiId() != null) {
            PlanoContas planoPai = repository.findById(dto.getPlanoPaiId())
                    .orElseThrow(() -> new IllegalArgumentException("Plano pai não encontrado"));
            entity.definirPlanoPai(planoPai);
        }
        
        if (dto.getAtivo() != null) {
            if (dto.getAtivo()) {
                entity.ativar();
            } else {
                entity.inativar();
            }
        }

        return entity;
    }

    @Override
    protected PlanoContasDTO buildDTOFromEntity(PlanoContas entity) {
        return PlanoContasDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .descricao(entity.getDescricao())
                .tipo(entity.getTipo().name())
                .planoPaiId(entity.getPlanoPai() != null ? entity.getPlanoPai().getId() : null)
                .planoPaiDescricao(entity.getPlanoPai() != null ? entity.getPlanoPai().toString() : null)
                .ativo(entity.getAtivo())
                .analitico(entity.isAnalitico())
                .nivel(entity.getNivel())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected PlanoContasGridDTO buildGridDTOFromEntity(PlanoContas entity) {
        return PlanoContasGridDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .descricao(entity.getDescricao())
                .tipo(entity.getTipo().name())
                .ativo(entity.getAtivo())
                .analitico(entity.isAnalitico())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("codigo", "descricao", "tipo", "ativo");
    }

    @Override
    protected Class<PlanoContas> getEntityClass() {
        return PlanoContas.class;
    }
}
