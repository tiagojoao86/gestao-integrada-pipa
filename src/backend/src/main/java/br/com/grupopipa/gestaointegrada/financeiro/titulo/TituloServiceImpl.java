package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.planocontas.PlanoContasRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TituloServiceImpl extends CrudServiceImpl<TituloDTO, TituloGridDTO, Titulo, TituloRepository>
        implements TituloService {

    private final PessoaRepository pessoaRepository;
    private final PlanoContasRepository planoContasRepository;
    private final UnidadeNegocioRepository unidadeNegocioRepository;

    public TituloServiceImpl(TituloRepository repository, 
                             Specifications<Titulo> specifications,
                             PessoaRepository pessoaRepository,
                             PlanoContasRepository planoContasRepository,
                             UnidadeNegocioRepository unidadeNegocioRepository) {
        super(repository, specifications);
        this.pessoaRepository = pessoaRepository;
        this.planoContasRepository = planoContasRepository;
        this.unidadeNegocioRepository = unidadeNegocioRepository;
    }

    @Override
    protected Titulo mergeEntityAndDTO(Titulo entity, TituloDTO dto) {
        if (Objects.isNull(entity)) {
            // Buscar entidades relacionadas
            Pessoa pessoa = pessoaRepository.findById(dto.getPessoaId())
                    .orElseThrow(() -> new IllegalArgumentException("Pessoa não encontrada"));
            PlanoContas planoContas = planoContasRepository.findById(dto.getPlanoContasId())
                    .orElseThrow(() -> new IllegalArgumentException("Plano de contas não encontrado"));
            
            TipoTitulo tipo = TipoTitulo.valueOf(dto.getTipo());
            Money valorOriginal = new Money(dto.getValorOriginal());
            
            entity = new Titulo(tipo, dto.getDescricao(), pessoa, planoContas, 
                              valorOriginal, dto.getDataEmissao(), dto.getDataVencimento());
            
            // Unidade de negócio (opcional)
            if (dto.getUnidadeNegocioId() != null) {
                UnidadeNegocio unidadeNegocio = unidadeNegocioRepository.findById(dto.getUnidadeNegocioId())
                        .orElseThrow(() -> new IllegalArgumentException("Unidade de negócio não encontrada"));
                entity.definirUnidadeNegocio(unidadeNegocio);
            }
            
            // Aplicar descontos, juros, multa se fornecidos
            if (dto.getValorDesconto() != null && dto.getValorDesconto().compareTo(java.math.BigDecimal.ZERO) > 0) {
                entity.aplicarDesconto(new Money(dto.getValorDesconto()));
            }
            if (dto.getValorJuros() != null && dto.getValorJuros().compareTo(java.math.BigDecimal.ZERO) > 0) {
                entity.aplicarJuros(new Money(dto.getValorJuros()));
            }
            if (dto.getValorMulta() != null && dto.getValorMulta().compareTo(java.math.BigDecimal.ZERO) > 0) {
                entity.aplicarMulta(new Money(dto.getValorMulta()));
            }
            
            // Parcelamento
            if (dto.getNumeroParcela() != null && dto.getTotalParcelas() != null) {
                Titulo tituloOrigem = null;
                if (dto.getTituloOrigemId() != null) {
                    tituloOrigem = repository.findById(dto.getTituloOrigemId()).orElse(null);
                }
                entity.definirParcelamento(dto.getNumeroParcela(), dto.getTotalParcelas(), tituloOrigem);
            }
            
            if (dto.getObservacoes() != null && !dto.getObservacoes().isBlank()) {
                entity.adicionarObservacao(dto.getObservacoes());
            }
            
            return entity;
        }

        // Atualizar título existente
        entity.atualizar(dto.getDescricao(), dto.getDataVencimento());
        
        // Atualizar valores adicionais se fornecidos
        if (dto.getValorDesconto() != null) {
            entity.aplicarDesconto(new Money(dto.getValorDesconto()));
        }
        if (dto.getValorJuros() != null) {
            entity.aplicarJuros(new Money(dto.getValorJuros()));
        }
        if (dto.getValorMulta() != null) {
            entity.aplicarMulta(new Money(dto.getValorMulta()));
        }

        return entity;
    }

    @Override
    protected TituloDTO buildDTOFromEntity(Titulo entity) {
        return TituloDTO.builder()
                .id(entity.getId())
                .tipo(entity.getTipo().name())
                .status(entity.getStatus().name())
                .numeroDocumento(entity.getNumeroDocumento())
                .descricao(entity.getDescricao())
                .pessoaId(entity.getPessoa().getId())
                .pessoaNome(entity.getPessoa().getNome())
                .planoContasId(entity.getPlanoContas().getId())
                .planoContasDescricao(entity.getPlanoContas().toString())
                .unidadeNegocioId(entity.getUnidadeNegocio() != null ? entity.getUnidadeNegocio().getId() : null)
                .unidadeNegocioNome(entity.getUnidadeNegocio() != null ? entity.getUnidadeNegocio().getNome() : null)
                .valorOriginal(entity.getValorOriginal().getValue())
                .valorPago(entity.getValorPago().getValue())
                .valorDesconto(entity.getValorDesconto().getValue())
                .valorJuros(entity.getValorJuros().getValue())
                .valorMulta(entity.getValorMulta().getValue())
                .saldo(entity.calcularSaldo().getValue())
                .dataEmissao(entity.getDataEmissao())
                .dataVencimento(entity.getDataVencimento())
                .dataPagamento(entity.getDataPagamento())
                .observacoes(entity.getObservacoes())
                .numeroParcela(entity.getNumeroParcela())
                .totalParcelas(entity.getTotalParcelas())
                .tituloOrigemId(entity.getTituloOrigem() != null ? entity.getTituloOrigem().getId() : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    @Override
    protected TituloGridDTO buildGridDTOFromEntity(Titulo entity) {
        String parcelamento = null;
        if (entity.isParcelado()) {
            parcelamento = entity.getNumeroParcela() + "/" + entity.getTotalParcelas();
        }
        
        return TituloGridDTO.builder()
                .id(entity.getId())
                .tipo(entity.getTipo().name())
                .status(entity.getStatus().name())
                .numeroDocumento(entity.getNumeroDocumento())
                .descricao(entity.getDescricao())
                .pessoaNome(entity.getPessoa().getNome())
                .valorOriginal(entity.getValorOriginal().getValue())
                .saldo(entity.calcularSaldo().getValue())
                .dataVencimento(entity.getDataVencimento())
                .parcelamento(parcelamento)
                .build();
    }

    @Override
    protected List<String> getPropertiesToFilter() {
        return List.of("tipo", "status", "descricao", "dataVencimento");
    }

    @Override
    protected Class<Titulo> getEntityClass() {
        return Titulo.class;
    }
}
