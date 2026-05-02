package br.com.grupopipa.gestaointegrada.financeiro.caixa;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.caixa.entity.Caixa;

@Service
public class CaixaServiceImpl
        extends CrudServiceImpl<CaixaDTO, CaixaGridDTO, Caixa, CaixaRepository>
        implements CaixaService {

    public CaixaServiceImpl(CaixaRepository repository, Specifications<Caixa> specifications) {
        super(repository, specifications);
    }

    @Override
    protected Caixa mergeEntityAndDTO(Caixa entity, CaixaDTO dto) {
        if (Objects.isNull(entity)) {
            return criarCaixa(dto);
        }
        return atualizarCaixa(entity, dto);
    }

    private Caixa criarCaixa(CaixaDTO dto) {
        return new Caixa.Builder()
                .nome(dto.getNome())
                .valorPadraoAbertura(defaultZero(dto.getValorPadraoAbertura()))
                .percentualPagamentoParcial(dto.getPercentualPagamentoParcial())
                .valorMinimoParcela(dto.getValorMinimoParcela())
                .build();
    }

    private Caixa atualizarCaixa(Caixa entity, CaixaDTO dto) {
        entity.atualizar(
                dto.getNome(),
                defaultZero(dto.getValorPadraoAbertura()),
                dto.getPercentualPagamentoParcial(),
                dto.getValorMinimoParcela());

        if (Boolean.TRUE.equals(dto.getAtivo())) {
            entity.ativar();
        } else if (Boolean.FALSE.equals(dto.getAtivo())) {
            entity.inativar();
        }

        return entity;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Override
    protected CaixaDTO buildDTOFromEntity(Caixa entity) {
        return CaixaDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .valorPadraoAbertura(entity.getValorPadraoAbertura())
                .percentualPagamentoParcial(entity.getPercentualPagamentoParcial())
                .valorMinimoParcela(entity.getValorMinimoParcela())
                .ativo(entity.getAtivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected CaixaGridDTO buildGridDTOFromEntity(Caixa entity) {
        return CaixaGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .valorPadraoAbertura(entity.getValorPadraoAbertura())
                .percentualParcialConfigurado(entity.getPercentualPagamentoParcial() != null)
                .ativo(entity.getAtivo())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "ativo");
    }

    @Override
    protected Class<Caixa> getEntityClass() {
        return Caixa.class;
    }
}
