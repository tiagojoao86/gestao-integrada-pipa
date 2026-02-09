package br.com.grupopipa.gestaointegrada.financeiro.condicaopagamento;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CondicaoPagamento;

@Service
public class CondicaoPagamentoServiceImpl
        extends CrudServiceImpl<CondicaoPagamentoDTO, CondicaoPagamentoGridDTO,
            CondicaoPagamento, CondicaoPagamentoRepository>
        implements CondicaoPagamentoService {

    public CondicaoPagamentoServiceImpl(
            CondicaoPagamentoRepository repository, Specifications<CondicaoPagamento> specifications) {
        super(repository, specifications);
    }

    @Override
    protected CondicaoPagamento mergeEntityAndDTO(CondicaoPagamento entity, CondicaoPagamentoDTO dto) {
        if (entity == null) {
            return new CondicaoPagamento.Builder()
                    .condicao(dto.getCondicao())
                    .descricao(dto.getDescricao())
                    .ativo(dto.getAtivo())
                    .build();
        }

        entity.atualizar(dto.getCondicao(), dto.getDescricao(), dto.getAtivo());
        return entity;
    }

    @Override
    protected CondicaoPagamentoDTO buildDTOFromEntity(CondicaoPagamento entity) {
        return CondicaoPagamentoDTO.builder()
                .id(entity.getId())
                .condicao(entity.getCondicao())
                .descricao(entity.getDescricao())
                .ativo(entity.getAtivo())
                .quantidadeParcelas(entity.getQuantidadeParcelas())
                .diasVencimento(entity.getDiasVencimento())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected CondicaoPagamentoGridDTO buildGridDTOFromEntity(CondicaoPagamento entity) {
        return CondicaoPagamentoGridDTO.builder()
                .id(entity.getId())
                .condicao(entity.getCondicao())
                .ativo(entity.getAtivo())
                .quantidadeParcelas(entity.getQuantidadeParcelas())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("condicao", "descricao");
    }

    @Override
    protected Class<CondicaoPagamento> getEntityClass() {
        return CondicaoPagamento.class;
    }
}
