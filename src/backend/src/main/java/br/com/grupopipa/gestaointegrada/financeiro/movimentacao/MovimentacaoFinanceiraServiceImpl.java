package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.LancamentoFinanceiroRepository;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.contabancaria.ContaBancariaRepository;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.entity.MovimentacaoFinanceira;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
import br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloRepository;

@Service
public class MovimentacaoFinanceiraServiceImpl extends
        CrudServiceImpl<MovimentacaoFinanceiraDTO, MovimentacaoFinanceiraGridDTO,
        MovimentacaoFinanceira, MovimentacaoFinanceiraRepository>
        implements MovimentacaoFinanceiraService {

    private final TituloRepository tituloRepository;
    private final ContaBancariaRepository contaBancariaRepository;
    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    public MovimentacaoFinanceiraServiceImpl(
            MovimentacaoFinanceiraRepository repository,
            Specifications<MovimentacaoFinanceira> specifications,
            TituloRepository tituloRepository,
            ContaBancariaRepository contaBancariaRepository,
            LancamentoFinanceiroRepository lancamentoFinanceiroRepository) {
        super(repository, specifications);
        this.tituloRepository = tituloRepository;
        this.contaBancariaRepository = contaBancariaRepository;
        this.lancamentoFinanceiroRepository = lancamentoFinanceiroRepository;
    }

    @Override
    @Transactional
    public MovimentacaoFinanceiraDTO save(MovimentacaoFinanceiraDTO dto) {
        MovimentacaoFinanceiraDTO result = super.save(dto);
        sincronizarLancamentosComTitulosPagos(dto);
        return result;
    }

    private void sincronizarLancamentosComTitulosPagos(MovimentacaoFinanceiraDTO dto) {
        if (dto.getTitulos() == null) {
            return;
        }
        dto.getTitulos().forEach(t -> {
            tituloRepository.findById(t.getId()).ifPresent(titulo -> {
                if (titulo.getStatus() == StatusTitulo.PAGO) {
                    lancamentoFinanceiroRepository.findByTituloId(titulo.getId())
                        .ifPresent(lf -> {
                            lf.marcarComoPago();
                            lancamentoFinanceiroRepository.save(lf);
                        });
                }
            });
        });
    }

    @Override
    protected MovimentacaoFinanceira mergeEntityAndDTO(
            MovimentacaoFinanceira entity, MovimentacaoFinanceiraDTO dto) {
        if (Objects.isNull(entity)) {
            // Buscar entidades relacionadas
            List<Titulo> titulos = dto.getTitulos() == null
                    ? List.of()
                    : dto.getTitulos().stream()
                            .map(
                                    t -> tituloRepository
                                            .findById(t.getId())
                                            .orElseThrow(
                                                    () -> new IllegalArgumentException(
                                                            "Título não encontrado: " + t.getId())))
                            .toList();
            ContaBancaria contaBancaria = contaBancariaRepository
                    .findById(dto.getContaBancariaId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta bancária não encontrada"));

            TipoMovimentacao tipo = TipoMovimentacao.valueOf(dto.getTipo());
            FormaPagamento formaPagamento = FormaPagamento.valueOf(dto.getFormaPagamento());

            entity = new MovimentacaoFinanceira.Builder()
                    .titulos(new java.util.HashSet<>(titulos))
                    .contaBancaria(contaBancaria)
                    .unidadeNegocio(contaBancaria.getUnidadeNegocio())
                    .tipo(tipo)
                    .formaPagamento(formaPagamento)
                    .valor(Money.of(dto.getValor()))
                    .data(dto.getData())
                    .build();

            if (dto.getObservacoes() != null && !dto.getObservacoes().isBlank()) {
                entity.adicionarObservacao(dto.getObservacoes());
            }

            return entity;
        }

        // MovimentacaoFinanceira é imutável após criação (apenas observações podem ser
        // adicionadas)
        if (dto.getObservacoes() != null && !dto.getObservacoes().isBlank()) {
            entity.adicionarObservacao(dto.getObservacoes());
        }

        return entity;
    }

    @Override
    protected MovimentacaoFinanceiraDTO buildDTOFromEntity(MovimentacaoFinanceira entity) {
        List<MovimentacaoTituloDTO> titulos = entity.getTitulosAssociados().stream()
                .map(
                        mt -> MovimentacaoTituloDTO.builder()
                                .id(mt.getTitulo().getId())
                                .descricao(mt.getTitulo().getDescricao())
                                .valor(mt.getValor().getValue())
                                .tipo(mt.getTitulo().getTipo().name())
                                .build())
                .toList();
        return MovimentacaoFinanceiraDTO.builder()
                .id(entity.getId())
                .titulos(titulos)
                .contaBancariaId(entity.getContaBancaria().getId())
                .contaBancariaNome(entity.getContaBancaria().getNome())
                .tipo(entity.getTipo().name())
                .formaPagamento(entity.getFormaPagamento().name())
                .valor(entity.getValor().getValue())
                .data(entity.getData())
                .observacoes(entity.getObservacoes())
                .unidadeNegocioId(entity.getUnidadeNegocio().getId())
                .unidadeNegocioCodigo(entity.getUnidadeNegocio().getCodigo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected MovimentacaoFinanceiraGridDTO buildGridDTOFromEntity(MovimentacaoFinanceira entity) {
        String titulosDescricao = entity.getTitulos().stream()
                .map(t -> t.getDescricao())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return MovimentacaoFinanceiraGridDTO.builder()
                .id(entity.getId())
                .tituloDescricao(titulosDescricao)
                .contaBancariaNome(entity.getContaBancaria().getNome())
                .unidadeNegocioId(
                        entity.getUnidadeNegocio() != null ? entity.getUnidadeNegocio().getId() : null)
                .unidadeNegocioNome(
                        entity.getUnidadeNegocio() != null ? entity.getUnidadeNegocio().getNome() : null)
                .tipo(entity.getTipo().name())
                .formaPagamento(entity.getFormaPagamento().name())
                .valor(entity.getValor().getValue())
                .data(entity.getData())
                .deleted(entity.getDeleted())
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("tipo", "formaPagamento", "data");
    }

    @Override
    protected Class<MovimentacaoFinanceira> getEntityClass() {
        return MovimentacaoFinanceira.class;
    }

    /**
     * Sobrescreve o método delete para reverter o pagamento dos títulos
     * após realizar o soft delete da movimentação
     */
    @Override
    @Transactional
    public UUID delete(UUID id) {
        // Buscar a movimentação antes de deletar
        MovimentacaoFinanceira movimentacao = this.findEntityById(id);

        // Chamar o delete da classe pai para fazer o soft delete PRIMEIRO
        // Isso marca a movimentação como deletada, fazendo com que getValorPago()
        // não a considere mais
        UUID result = super.delete(id);

        // Agora reverter o pagamento em todos os títulos associados
        // Como a movimentação já está deletada, getValorPago() não a contará
        movimentacao.getTitulos().forEach(titulo -> {
            titulo.reverterPagamento();
            tituloRepository.save(titulo);
        });

        return result;
    }
}
