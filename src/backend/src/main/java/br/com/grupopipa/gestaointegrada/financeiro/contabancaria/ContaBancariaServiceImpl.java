package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioDTO;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioService;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;
import org.springframework.stereotype.Service;

import java.util.Set;

import java.util.List;
import java.util.Objects;

@Service
public class ContaBancariaServiceImpl
        extends CrudServiceImpl<ContaBancariaDTO, ContaBancariaGridDTO, ContaBancaria, ContaBancariaRepository>
        implements ContaBancariaService {

    private final UnidadeNegocioRepository unidadeNegocioRepository;
    private final UnidadeNegocioService unidadeNegocioService;

    public ContaBancariaServiceImpl(ContaBancariaRepository repository,
            Specifications<ContaBancaria> specifications,
            UnidadeNegocioRepository unidadeNegocioRepository,
            UnidadeNegocioService unidadeNegocioService) {
        super(repository, specifications);
        this.unidadeNegocioRepository = unidadeNegocioRepository;
        this.unidadeNegocioService = unidadeNegocioService;
    }

    @Override
    protected ContaBancaria mergeEntityAndDTO(ContaBancaria entity, ContaBancariaDTO dto) {
        // Buscar UnidadeNegocio
        UnidadeNegocio unidadeNegocio = unidadeNegocioRepository.findById(dto.getUnidadeNegocioId())
                .orElseThrow(() -> new BeanValidationException("contaBancaria",
                        Set.of(new BeanValidationMessage("unidadeNegocio", "Unidade de negócio não encontrada"))));

        if (Objects.isNull(entity)) {
            TipoConta tipo = TipoConta.valueOf(dto.getTipo());
            entity = new ContaBancaria.Builder()
                    .nome(dto.getNome())
                    .tipo(tipo)
                    .banco(dto.getBanco())
                    .agencia(dto.getAgencia())
                    .numeroConta(dto.getNumeroConta())
                    .saldoInicial(Money.of(dto.getSaldoInicial()))
                    .unidadeNegocio(unidadeNegocio)
                    .build();

            return entity;
        }

        entity.atualizar(dto.getNome(), dto.getBanco(), dto.getAgencia(), dto.getNumeroConta());
        entity.atualizarUnidadeNegocio(unidadeNegocio);

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
                .unidadeNegocioId(entity.getUnidadeNegocio() != null ? entity.getUnidadeNegocio().getId() : null)
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
                .unidadeNegocioCodigo(
                        entity.getUnidadeNegocio() != null ? entity.getUnidadeNegocio().getCodigo() : null)
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

    @Override
    public List<UnidadeNegocioDTO> listarUnidadesDisponiveis() {
        return unidadeNegocioService.listarDisponiveisParaUsuario();
    }
}
