package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.contabancaria.ContaBancariaRepository;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.entity.MovimentacaoFinanceira;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
import br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class MovimentacaoFinanceiraServiceImpl extends
        CrudServiceImpl<MovimentacaoFinanceiraDTO, MovimentacaoFinanceiraGridDTO, MovimentacaoFinanceira, MovimentacaoFinanceiraRepository>
        implements MovimentacaoFinanceiraService {

    private final TituloRepository tituloRepository;
    private final ContaBancariaRepository contaBancariaRepository;

    public MovimentacaoFinanceiraServiceImpl(MovimentacaoFinanceiraRepository repository,
            Specifications<MovimentacaoFinanceira> specifications,
            TituloRepository tituloRepository,
            ContaBancariaRepository contaBancariaRepository) {
        super(repository, specifications);
        this.tituloRepository = tituloRepository;
        this.contaBancariaRepository = contaBancariaRepository;
    }

    @Override
    protected MovimentacaoFinanceira mergeEntityAndDTO(MovimentacaoFinanceira entity, MovimentacaoFinanceiraDTO dto) {
        if (Objects.isNull(entity)) {
            // Buscar entidades relacionadas
            Titulo titulo = tituloRepository.findById(dto.getTituloId())
                    .orElseThrow(() -> new IllegalArgumentException("Título não encontrado"));
            ContaBancaria contaBancaria = contaBancariaRepository.findById(dto.getContaBancariaId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta bancária não encontrada"));

            TipoMovimentacao tipo = TipoMovimentacao.valueOf(dto.getTipo());
            FormaPagamento formaPagamento = FormaPagamento.valueOf(dto.getFormaPagamento());

            entity = new MovimentacaoFinanceira.Builder()
                    .titulo(titulo)
                    .contaBancaria(contaBancaria)
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
        return MovimentacaoFinanceiraDTO.builder()
                .id(entity.getId())
                .tituloId(entity.getTitulo().getId())
                .tituloDescricao(entity.getTitulo().getDescricao())
                .contaBancariaId(entity.getContaBancaria().getId())
                .contaBancariaNome(entity.getContaBancaria().getNome())
                .tipo(entity.getTipo().name())
                .formaPagamento(entity.getFormaPagamento().name())
                .valor(entity.getValor().getValue())
                .data(entity.getData())
                .observacoes(entity.getObservacoes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected MovimentacaoFinanceiraGridDTO buildGridDTOFromEntity(MovimentacaoFinanceira entity) {
        return MovimentacaoFinanceiraGridDTO.builder()
                .id(entity.getId())
                .tituloDescricao(entity.getTitulo().getDescricao())
                .contaBancariaNome(entity.getContaBancaria().getNome())
                .tipo(entity.getTipo().name())
                .formaPagamento(entity.getFormaPagamento().name())
                .valor(entity.getValor().getValue())
                .data(entity.getData())
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
}
