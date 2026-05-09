package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;

@Service
public class ContaBancariaServiceImpl
        extends CrudServiceImpl<ContaBancariaDTO, ContaBancariaGridDTO, ContaBancaria, ContaBancariaRepository>
        implements ContaBancariaService {

    private final UnidadeNegocioRepository unidadeNegocioRepository;
    private final UnidadeNegocioService unidadeNegocioService;

    public ContaBancariaServiceImpl(
            ContaBancariaRepository repository,
            Specifications<ContaBancaria> specifications,
            UnidadeNegocioRepository unidadeNegocioRepository,
            UnidadeNegocioService unidadeNegocioService) {
        super(repository, specifications);
        this.unidadeNegocioRepository = unidadeNegocioRepository;
        this.unidadeNegocioService = unidadeNegocioService;
    }

    @Override
    protected ContaBancaria mergeEntityAndDTO(ContaBancaria entity, ContaBancariaDTO dto) {
        UnidadeNegocio unidadeNegocio = unidadeNegocioRepository
                .findById(dto.getUnidadeNegocioId())
                .orElseThrow(() -> new BeanValidationException("contaBancaria",
                        Set.of(new BeanValidationMessage("unidadeNegocio", "Unidade de negócio não encontrada"))));

        Set<FormaPagamento> novasFormas = parseFormasPagamento(dto.getFormasPagamento());

        if (Objects.isNull(entity)) {
            TipoConta tipo = TipoConta.valueOf(dto.getTipo());
            validarFormasPagamentoUnicas(novasFormas, null);
            entity = new ContaBancaria.Builder()
                    .nome(dto.getNome())
                    .tipo(tipo)
                    .banco(dto.getBanco())
                    .agencia(dto.getAgencia())
                    .numeroConta(dto.getNumeroConta())
                    .saldoInicial(Money.of(dto.getSaldoInicial()))
                    .unidadeNegocio(unidadeNegocio)
                    .build();
            entity.atualizarFormasPagamento(novasFormas);
            return entity;
        }

        validarRemocaoFormasPagamento(entity, novasFormas);
        entity.atualizar(dto.getNome(), dto.getBanco(), dto.getAgencia(), dto.getNumeroConta());
        entity.atualizarUnidadeNegocio(unidadeNegocio);
        entity.atualizarFormasPagamento(novasFormas);

        if (dto.getAtiva() != null) {
            if (dto.getAtiva()) {
                entity.ativar();
            } else {
                entity.inativar();
            }
        }

        return entity;
    }

    private Set<FormaPagamento> parseFormasPagamento(List<String> formasPagamentoStr) {
        if (formasPagamentoStr == null) {
            return new HashSet<>();
        }
        return formasPagamentoStr.stream()
                .map(FormaPagamento::valueOf)
                .collect(Collectors.toSet());
    }

    private void validarFormasPagamentoUnicas(Set<FormaPagamento> formas, java.util.UUID excludeId) {
        for (FormaPagamento forma : formas) {
            List<ContaBancaria> existentes = excludeId != null
                    ? repository.findByFormaPagamentoExcluindo(forma, excludeId)
                    : repository.findByFormaPagamento(forma);
            if (!existentes.isEmpty()) {
                throw new BeanValidationException("contaBancaria",
                        Set.of(new BeanValidationMessage("formasPagamento",
                                "A forma de pagamento '" + forma.getDescricao()
                                        + "' já está configurada em outra conta financeira.")));
            }
        }
    }

    private void validarRemocaoFormasPagamento(ContaBancaria entity, Set<FormaPagamento> novasFormas) {
        Set<FormaPagamento> removidas = new HashSet<>(entity.getFormasPagamento());
        removidas.removeAll(novasFormas);

        for (FormaPagamento removida : removidas) {
            List<ContaBancaria> outrasComForma =
                    repository.findByFormaPagamentoExcluindo(removida, entity.getId());
            if (outrasComForma.isEmpty()) {
                throw new BeanValidationException("contaBancaria",
                        Set.of(new BeanValidationMessage("formasPagamento",
                                "Não é possível remover a forma de pagamento '" + removida.getDescricao()
                                        + "' pois nenhuma outra conta financeira está configurada para ela.")));
            }
        }

        Set<FormaPagamento> adicionadas = new HashSet<>(novasFormas);
        adicionadas.removeAll(entity.getFormasPagamento());
        if (!adicionadas.isEmpty()) {
            validarFormasPagamentoUnicas(adicionadas, entity.getId());
        }
    }

    @Override
    protected ContaBancariaDTO buildDTOFromEntity(ContaBancaria entity) {
        List<String> formas = entity.getFormasPagamento().stream()
                .map(FormaPagamento::name)
                .collect(Collectors.toList());
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
                .formasPagamento(formas)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected ContaBancariaGridDTO buildGridDTOFromEntity(ContaBancaria entity) {
        List<String> formas = entity.getFormasPagamento().stream()
                .map(FormaPagamento::name)
                .collect(Collectors.toList());
        return ContaBancariaGridDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .banco(entity.getBanco())
                .tipo(entity.getTipo().name())
                .saldoInicial(entity.getSaldoInicial() != null ? entity.getSaldoInicial().getValue() : null)
                .unidadeNegocioCodigo(
                        entity.getUnidadeNegocio() != null ? entity.getUnidadeNegocio().getCodigo() : null)
                .ativa(entity.getAtiva())
                .formasPagamento(formas)
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("nome", "banco", "tipo", "ativa", "unidadeNegocio");
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
