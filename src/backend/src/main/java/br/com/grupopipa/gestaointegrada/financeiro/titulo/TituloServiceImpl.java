package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaDTO;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.setor.SetorRepository;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioDTO;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioService;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.dto.FilterDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;
import br.com.grupopipa.gestaointegrada.core.service.impl.CrudServiceImpl;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.planocontas.PlanoContasDTO;
import br.com.grupopipa.gestaointegrada.financeiro.planocontas.PlanoContasRepository;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaDTO;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class TituloServiceImpl
        extends CrudServiceImpl<TituloDTO, TituloGridDTO, Titulo, TituloRepository>
        implements TituloService {

    private final PessoaRepository pessoaRepository;
    private final PlanoContasRepository planoContasRepository;
    private final UnidadeNegocioRepository unidadeNegocioRepository;
    private final UnidadeNegocioService unidadeNegocioService;
    private final SetorRepository setorRepository;
    private final TituloCategoriaRepository tituloCategoriaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public TituloServiceImpl(
            TituloRepository repository,
            Specifications<Titulo> specifications,
            PessoaRepository pessoaRepository,
            PlanoContasRepository planoContasRepository,
            UnidadeNegocioRepository unidadeNegocioRepository,
            UnidadeNegocioService unidadeNegocioService,
            SetorRepository setorRepository,
            TituloCategoriaRepository tituloCategoriaRepository) {
        super(repository, specifications);
        this.pessoaRepository = pessoaRepository;
        this.planoContasRepository = planoContasRepository;
        this.unidadeNegocioRepository = unidadeNegocioRepository;
        this.unidadeNegocioService = unidadeNegocioService;
        this.setorRepository = setorRepository;
        this.tituloCategoriaRepository = tituloCategoriaRepository;
    }

    @Override
    protected Titulo mergeEntityAndDTO(Titulo entity, TituloDTO dto) {
        if (Objects.isNull(entity)) {
            // Buscar entidades relacionadas
            Pessoa pessoa = pessoaRepository
                    .findById(dto.getPessoaId())
                    .orElseThrow(() -> new IllegalArgumentException("Pessoa não encontrada"));

            TipoTitulo tipo = TipoTitulo.valueOf(dto.getTipo());

            // Categoria (obrigatória)
            TituloCategoria tituloCategoria = tituloCategoriaRepository
                    .findById(dto.getTituloCategoriaId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));

            // Unidade de negócio (obrigatória)
            UnidadeNegocio unidadeNegocio = unidadeNegocioRepository
                    .findById(dto.getUnidadeNegocioId())
                    .orElseThrow(() -> new IllegalArgumentException("Unidade de negócio não encontrada"));

            entity = new Titulo.Builder()
                    .tipo(tipo)
                    .descricao(dto.getDescricao())
                    .numeroDocumento(dto.getNumeroDocumento())
                    .pessoa(pessoa)
                    .tituloCategoria(tituloCategoria)
                    .unidadeNegocio(unidadeNegocio)
                    .valorOriginal(Money.positive(dto.getValorOriginal())) // Usa positive() - valida > 0
                    .dataEmissao(dto.getDataEmissao())
                    .dataVencimento(dto.getDataVencimento())
                    .dataPagamento(dto.getDataPagamento()) // Valida >= dataEmissao
                    .build();

            // Aplicar descontos, juros, multa se fornecidos (usa positiveOrZero - valida >=
            // 0)
            if (dto.getValorDesconto() != null
                    && dto.getValorDesconto().compareTo(java.math.BigDecimal.ZERO) > 0) {
                entity.aplicarDesconto(Money.positiveOrZero(dto.getValorDesconto()));
            }
            if (dto.getValorJuros() != null
                    && dto.getValorJuros().compareTo(java.math.BigDecimal.ZERO) > 0) {
                entity.aplicarJuros(Money.positiveOrZero(dto.getValorJuros()));
            }
            if (dto.getValorMulta() != null
                    && dto.getValorMulta().compareTo(java.math.BigDecimal.ZERO) > 0) {
                entity.aplicarMulta(Money.positiveOrZero(dto.getValorMulta()));
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

            // Processar setores
            processarSetores(entity, dto);

            // Definir rateio automático
            entity.setRateioAutomatico(
                    dto.getRateioAutomatico() != null ? dto.getRateioAutomatico() : false);

            // Validar setores antes de salvar
            entity.validarSetores();

            return entity;
        }

        // Atualizar título existente - Preparar Money objects (usa positiveOrZero -
        // valida >= 0)
        Money valorDesconto = dto.getValorDesconto() != null ? Money.positiveOrZero(dto.getValorDesconto()) : null;
        Money valorJuros = dto.getValorJuros() != null ? Money.positiveOrZero(dto.getValorJuros()) : null;
        Money valorMulta = dto.getValorMulta() != null ? Money.positiveOrZero(dto.getValorMulta()) : null;

        // Atualizar através do método único da entidade (contém todas as validações)
        entity.atualizar(
                dto.getDescricao(),
                dto.getDataVencimento(),
                valorDesconto,
                valorJuros,
                valorMulta,
                dto.getDataPagamento(),
                dto.getObservacoes());

        // Atualizar unidade de negócio se fornecida
        if (dto.getUnidadeNegocioId() != null) {
            UnidadeNegocio unidadeNegocio = unidadeNegocioRepository
                    .findById(dto.getUnidadeNegocioId())
                    .orElseThrow(() -> new IllegalArgumentException("Unidade de negócio não encontrada"));
            entity.atualizarUnidadeNegocio(unidadeNegocio);
        }

        // Processar setores na atualização
        if (dto.getSetores() != null) {
            processarSetores(entity, dto);
            entity.validarSetores();
        }

        // Atualizar rateio automático
        if (dto.getRateioAutomatico() != null) {
            entity.setRateioAutomatico(dto.getRateioAutomatico());
        }

        return entity;
    }

    private void processarSetores(Titulo entity, TituloDTO dto) {
        if (dto.getSetores() == null || dto.getSetores().isEmpty()) {
            return;
        }

        // Limpar setores existentes (orphanRemoval fará o delete)
        if (!entity.getSetores().isEmpty()) {
            entity.limparSetores();
            // Force flush to delete orphaned records before inserting new ones
            // This prevents unique constraint violation on uk_titulo_setor
            if (entity.getId() != null) {
                entityManager.flush();
            }
        }

        // Adicionar novos setores
        for (TituloSetorDTO setorDTO : dto.getSetores()) {
            Setor setor = setorRepository
                    .findById(setorDTO.getSetorId())
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "Setor não encontrado: " + setorDTO.getSetorId()));

            entity.adicionarSetor(setor, setorDTO.getPercentualRateio());
        }
    }

    @Override
    protected TituloDTO buildDTOFromEntity(Titulo entity) {
        List<TituloSetorDTO> setoresDTO = entity.getSetores().stream()
                .map(
                        ts -> TituloSetorDTO.builder()
                                .setorId(ts.getSetor().getId())
                                .setorNome(ts.getSetor().getNome())
                                .percentualRateio(ts.getPercentualRateio())
                                .build())
                .toList();

        return TituloDTO.builder()
                .id(entity.getId())
                .tipo(entity.getTipo().name())
                .status(entity.getStatus().name())
                .numeroDocumento(entity.getNumeroDocumento())
                .descricao(entity.getDescricao())
                .pessoaId(entity.getPessoa().getId())
                .pessoaNome(entity.getPessoa().getNome())
                .tituloCategoriaId(
                        entity.getTituloCategoria() != null ? entity.getTituloCategoria().getId() : null)
                .tituloCategoriaNome(
                        entity.getTituloCategoria() != null
                                ? entity.getTituloCategoria().getNome().getValue()
                                : null)
                .unidadeNegocioId(
                        entity.getUnidadeNegocio() != null ? entity.getUnidadeNegocio().getId() : null)
                .unidadeNegocioNome(
                        entity.getUnidadeNegocio() != null ? entity.getUnidadeNegocio().getNome() : null)
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
                .setores(setoresDTO)
                .rateioAutomatico(entity.getRateioAutomatico())
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
                .tituloCategoriaNome(
                        entity.getTituloCategoria() != null
                                ? entity.getTituloCategoria().getNome().getValue()
                                : null)
                .unidadeNegocioCodigo(entity.getUnidadeNegocio().getCodigo())
                .valorOriginal(entity.getValorOriginal().getValue())
                .saldo(entity.calcularSaldo().getValue())
                .dataVencimento(entity.getDataVencimento())
                .parcelamento(parcelamento)
                .deleted(entity.getDeleted())
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

    /**
     * Override do método list para usar projeção otimizada com cálculo de valorPago
     * Evita N+1 queries
     * ao buscar títulos com movimentações
     */
    @Override
    @Transactional
    public PageDTO<TituloGridDTO> list(FilterDTO filter, Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<Titulo> specification = this.buildSpecification(filter);

        // Adicionar filtro de soft delete
        specification = addSoftDeleteFilter(specification, filter);

        // Adicionar automaticamente filtro de UnidadeNegocio se aplicável
        specification = addUnidadeNegocioFilterIfApplicable(specification);

        // Usar projeção otimizada
        Page<TituloProjection> page = repository.findAllProjected(specification, pageable);

        // Converter projeção para GridDTO
        List<TituloGridDTO> gridDTOs = page.getContent().stream().map(this::buildGridDTOFromProjection).toList();

        return new br.com.grupopipa.gestaointegrada.core.dto.PageDTO<>(
                gridDTOs, page.getPageable(), page.getTotalElements());
    }

    /** Constrói TituloGridDTO a partir da projeção otimizada */
    private TituloGridDTO buildGridDTOFromProjection(TituloProjection projection) {
        String parcelamento = null;
        if (projection.getNumeroParcela() != null
                && projection.getTotalParcelas() != null
                && projection.getTotalParcelas() > 1) {
            parcelamento = projection.getNumeroParcela() + "/" + projection.getTotalParcelas();
        }

        // Calcular saldo: valorOriginal + juros + multa - desconto - valorPago
        java.math.BigDecimal saldo = projection
                .getValorOriginal()
                .add(projection.getValorJuros())
                .add(projection.getValorMulta())
                .subtract(projection.getValorDesconto())
                .subtract(projection.getValorPago());

        return TituloGridDTO.builder()
                .id(projection.getId())
                .tipo(projection.getTipo().name())
                .status(projection.getStatus().name())
                .numeroDocumento(projection.getNumeroDocumento())
                .descricao(projection.getDescricao())
                .pessoaNome(projection.getPessoaNome())
                .tituloCategoriaNome(projection.getTituloCategoriaNome())
                .unidadeNegocioCodigo(projection.getUnidadeNegocioCodigo())
                .valorOriginal(projection.getValorOriginal())
                .valorPago(projection.getValorPago())
                .saldo(saldo)
                .dataVencimento(projection.getDataVencimento())
                .parcelamento(parcelamento)
                .deleted(projection.getDeleted())
                .build();
    }

    @Override
    public List<UnidadeNegocioDTO> listarUnidadesDisponiveis() {
        return unidadeNegocioService.listarDisponiveisParaUsuario();
    }

    @Transactional
    @Override
    public List<TituloDTO> searchByQuery(String q, int size) {
        if (q == null || q.isBlank()) {
            return List.of();
        }

        String pattern = "%" + q.toLowerCase() + "%";

        Specification<Titulo> descricaoSpec = (root, query, cb) -> cb.like(cb.lower(root.get("descricao")), pattern);

        Specification<Titulo> numeroDocSpec = (root, query, cb) -> cb.like(cb.lower(root.get("numeroDocumento")),
                pattern);

        Specification<Titulo> combined = descricaoSpec.or(numeroDocSpec);

        // Apply automatic unidadeNegocio filter provided by CrudServiceImpl via
        // getUnidadeNegocioFilter()
        var unidadeSpec = this.getUnidadeNegocioFilter();
        if (unidadeSpec != null) {
            combined = combined.and(unidadeSpec);
        }

        var page = ((JpaSpecificationExecutor<Titulo>) this.repository)
                .findAll(combined, PageRequest.of(0, Math.max(1, size)));

        return page.stream().map(this::buildDTOFromEntity).toList();
    }

    @Override
    public List<PessoaDTO> listarPessoasDisponiveis() {
        return pessoaRepository.findByAtivaTrue().stream()
                .map(
                        pessoa -> PessoaDTO.builder()
                                .id(pessoa.getId())
                                .nome(pessoa.getNome())
                                .tipoPessoa(pessoa.getTipoPessoa().name())
                                .cpf(pessoa.getCpf())
                                .cnpj(pessoa.getCnpj())
                                .build())
                .toList();
    }

    @Override
    public List<TituloCategoriaDTO> listarCategoriasDisponiveis() {
        return tituloCategoriaRepository.findAll().stream()
                .map(
                        categoria -> TituloCategoriaDTO.builder()
                                .id(categoria.getId())
                                .codigo(categoria.getCodigo())
                                .nome(categoria.getNome().getValue())
                                .build())
                .toList();
    }

    @Override
    public List<PlanoContasDTO> listarPlanosDisponiveis(java.util.UUID unidadeNegocioId) {
        return planoContasRepository.findByAtivoTrueAndUnidadeNegocioId(unidadeNegocioId).stream()
                .map(
                        plano -> PlanoContasDTO.builder()
                                .id(plano.getId())
                                .codigo(plano.getCodigo())
                                .descricao(plano.getDescricao())
                                .tipo(plano.getTipo().name())
                                .build())
                .toList();
    }
}
