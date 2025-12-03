package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ContaBancariaServiceImpl extends CrudServiceImpl<ContaBancariaDTO, ContaBancariaGridDTO, ContaBancaria, ContaBancariaRepository>
        implements ContaBancariaService {

    public ContaBancariaServiceImpl(ContaBancariaRepository repository, Specifications<ContaBancaria> specifications) {
        super(repository, specifications);
    }

    @Override
    protected ContaBancaria mergeEntityAndDTO(ContaBancaria entity, ContaBancariaDTO dto) {
        if (Objects.isNull(entity)) {
            TipoConta tipo = TipoConta.valueOf(dto.getTipo());
            entity = new ContaBancaria(dto.getNome(), tipo, dto.getBanco(), dto.getAgencia(), dto.getNumeroConta());
            
            if (dto.getSaldoInicial() != null) {
                entity.definirSaldoInicial(new Money(dto.getSaldoInicial()));
            }
            
            return entity;
        }

        entity.atualizar(dto.getNome(), dto.getBanco(), dto.getAgencia(), dto.getNumeroConta());
        
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
    protected ContaBancariaDTO buildDTOFromEntity(ContaBancaria entity) {
        return ContaBancariaDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .banco(entity.getBanco())
                .agencia(entity.getAgencia())
                .numeroConta(entity.getNumeroConta())
                .tipo(entity.getTipo().name())
                .saldoInicial(entity.getSaldoInicial() != null ? entity.getSaldoInicial().getValue() : null)
                .ativa(entity.getAtiva())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected ContaBancariaGridDTO buildGridDTOFromEntity(ContaBancaria entity) {
        return ContaBancariaGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .banco(entity.getBanco())
                .tipo(entity.getTipo().name())
                .saldoInicial(entity.getSaldoInicial() != null ? entity.getSaldoInicial().getValue() : null)
                .ativa(entity.getAtiva())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "banco", "tipo", "ativa");
    }

    @Override
    protected Class<ContaBancaria> getEntityClass() {
        return ContaBancaria.class;
    }
}
