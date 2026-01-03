package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Implementação customizada para consultas otimizadas de Titulo Usa
 * CriteriaQuery para combinar
 * Specifications com projeção
 */
public class TituloRepositoryImpl implements TituloRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<TituloProjection> findAllProjected(Specification<Titulo> spec, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Query para os dados
        CriteriaQuery<TituloProjectionImpl> cq = cb.createQuery(TituloProjectionImpl.class);
        Root<Titulo> root = cq.from(Titulo.class);

        // LEFT JOIN com movimentações para calcular valorPago
        // Filtrar apenas movimentações não deletadas (deleted = false ou deleted IS
        // NULL)
        Join<Titulo, ?> movimentacoesJoin = root.join("movimentacoes", JoinType.LEFT);

        // Joins para pessoa, categoria e unidade (sem fetch pois estamos usando
        // projeção)
        Join<Titulo, ?> pessoaJoin = root.join("pessoa", JoinType.LEFT);
        Join<Titulo, ?> categoriaJoin = root.join("tituloCategoria", JoinType.LEFT);
        Join<Titulo, ?> unidadeJoin = root.join("unidadeNegocio", JoinType.LEFT);

        // Expressão para SUM(movimentacoes.valor) apenas de movimentações não deletadas
        // CASE WHEN movimentacao.deleted = false OR movimentacao.deleted IS NULL THEN
        // movimentacao.valor ELSE 0
        Expression<BigDecimal> valorPagoExpr = cb.coalesce(
                cb.sum(
                        cb.<BigDecimal>selectCase()
                                .when(
                                        cb.or(
                                                cb.isFalse(movimentacoesJoin.get("deleted")),
                                                cb.isNull(movimentacoesJoin.get("deleted"))),
                                        movimentacoesJoin.get("valor").get("value"))
                                .otherwise(BigDecimal.ZERO)),
                BigDecimal.ZERO);

        // Projeção
        cq.multiselect(
                root.get("id"),
                root.get("tipo"),
                root.get("status"),
                root.get("numeroDocumento"),
                root.get("descricao"),
                pessoaJoin.get("id"),
                pessoaJoin.get("nome").get("value"), // Nome é value object, pegar .value
                categoriaJoin.get("id"),
                categoriaJoin.get("nome").get("value"),
                unidadeJoin.get("id"),
                unidadeJoin.get("codigo"),
                unidadeJoin.get("nome").get("value"), // Nome é value object, pegar .value
                root.get("valorOriginal").get("value"),
                root.get("valorDesconto").get("value"),
                root.get("valorJuros").get("value"),
                root.get("valorMulta").get("value"),
                valorPagoExpr,
                root.get("dataEmissao"),
                root.get("dataVencimento"),
                root.get("dataPagamento"),
                root.get("numeroParcela"),
                root.get("totalParcelas"),
                root.get("deleted"));

        // Aplicar Specification se fornecida
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, cq, cb);
            if (predicate != null) {
                cq.where(predicate);
            }
        }

        // GROUP BY obrigatório por causa do SUM
        List<Expression<?>> groupByExpressions = new ArrayList<>();
        groupByExpressions.add(root.get("id"));
        groupByExpressions.add(root.get("tipo"));
        groupByExpressions.add(root.get("status"));
        groupByExpressions.add(root.get("numeroDocumento"));
        groupByExpressions.add(root.get("descricao"));
        groupByExpressions.add(pessoaJoin.get("id"));
        groupByExpressions.add(pessoaJoin.get("nome").get("value"));
        groupByExpressions.add(categoriaJoin.get("id"));
        groupByExpressions.add(categoriaJoin.get("nome").get("value"));
        groupByExpressions.add(unidadeJoin.get("id"));
        groupByExpressions.add(unidadeJoin.get("codigo"));
        groupByExpressions.add(unidadeJoin.get("nome").get("value"));
        groupByExpressions.add(root.get("valorOriginal").get("value"));
        groupByExpressions.add(root.get("valorDesconto").get("value"));
        groupByExpressions.add(root.get("valorJuros").get("value"));
        groupByExpressions.add(root.get("valorMulta").get("value"));
        groupByExpressions.add(root.get("dataEmissao"));
        groupByExpressions.add(root.get("dataVencimento"));
        groupByExpressions.add(root.get("dataPagamento"));
        groupByExpressions.add(root.get("numeroParcela"));
        groupByExpressions.add(root.get("totalParcelas"));
        groupByExpressions.add(root.get("deleted"));

        cq.groupBy(groupByExpressions);

        // Aplicar ordenação do Pageable
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable
                    .getSort()
                    .forEach(
                            order -> {
                                Path<Object> path = root.get(order.getProperty());
                                orders.add(order.isAscending() ? cb.asc(path) : cb.desc(path));
                            });
            cq.orderBy(orders);
        }

        // Executar query com paginação
        TypedQuery<TituloProjectionImpl> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<TituloProjectionImpl> results = query.getResultList();

        // Contar total de registros
        long total = countProjected(spec);

        // Converter para Page
        return new PageImpl<>(new ArrayList<>(results), pageable, total);
    }

    private long countProjected(Specification<Titulo> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Titulo> root = cq.from(Titulo.class);

        // Para count, usamos DISTINCT porque temos LEFT JOIN com movimentações
        cq.select(cb.countDistinct(root));

        // Aplicar mesma Specification
        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, cq, cb);
            if (predicate != null) {
                cq.where(predicate);
            }
        }

        return entityManager.createQuery(cq).getSingleResult();
    }

    /**
     * Implementação concreta de TituloProjection Classe interna para encapsular os
     * dados retornados
     * pela projeção
     */
    public static class TituloProjectionImpl implements TituloProjection {
        private final UUID id;
        private final br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo tipo;
        private final br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo status;
        private final String numeroDocumento;
        private final String descricao;
        private final UUID pessoaId;
        private final String pessoaNome;
        private final UUID tituloCategoriaId;
        private final String tituloCategoriaNome;
        private final UUID unidadeNegocioId;
        private final String unidadeNegocioCodigo;
        private final String unidadeNegocioNome;
        private final BigDecimal valorOriginal;
        private final BigDecimal valorDesconto;
        private final BigDecimal valorJuros;
        private final BigDecimal valorMulta;
        private final BigDecimal valorPago;
        private final LocalDate dataEmissao;
        private final LocalDate dataVencimento;
        private final LocalDate dataPagamento;
        private final Integer numeroParcela;
        private final Integer totalParcelas;
        private final Boolean deleted;

        public TituloProjectionImpl(
                UUID id,
                br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo tipo,
                br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo status,
                String numeroDocumento,
                String descricao,
                UUID pessoaId,
                String pessoaNome,
                UUID tituloCategoriaId,
                String tituloCategoriaNome,
                UUID unidadeNegocioId,
                String unidadeNegocioCodigo,
                String unidadeNegocioNome,
                BigDecimal valorOriginal,
                BigDecimal valorDesconto,
                BigDecimal valorJuros,
                BigDecimal valorMulta,
                BigDecimal valorPago,
                LocalDate dataEmissao,
                LocalDate dataVencimento,
                LocalDate dataPagamento,
                Integer numeroParcela,
                Integer totalParcelas,
                Boolean deleted) {
            this.id = id;
            this.tipo = tipo;
            this.status = status;
            this.numeroDocumento = numeroDocumento;
            this.descricao = descricao;
            this.pessoaId = pessoaId;
            this.pessoaNome = pessoaNome;
            this.tituloCategoriaId = tituloCategoriaId;
            this.tituloCategoriaNome = tituloCategoriaNome;
            this.unidadeNegocioId = unidadeNegocioId;
            this.unidadeNegocioCodigo = unidadeNegocioCodigo;
            this.unidadeNegocioNome = unidadeNegocioNome;
            this.valorOriginal = valorOriginal;
            this.valorDesconto = valorDesconto;
            this.valorJuros = valorJuros;
            this.valorMulta = valorMulta;
            this.valorPago = valorPago != null ? valorPago : BigDecimal.ZERO;
            this.dataEmissao = dataEmissao;
            this.dataVencimento = dataVencimento;
            this.dataPagamento = dataPagamento;
            this.numeroParcela = numeroParcela;
            this.totalParcelas = totalParcelas;
            this.deleted = deleted;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo getTipo() {
            return tipo;
        }

        @Override
        public br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo getStatus() {
            return status;
        }

        @Override
        public String getNumeroDocumento() {
            return numeroDocumento;
        }

        @Override
        public String getDescricao() {
            return descricao;
        }

        @Override
        public UUID getPessoaId() {
            return pessoaId;
        }

        @Override
        public String getPessoaNome() {
            return pessoaNome;
        }

        @Override
        public UUID getTituloCategoriaId() {
            return tituloCategoriaId;
        }

        @Override
        public String getTituloCategoriaNome() {
            return tituloCategoriaNome;
        }

        @Override
        public UUID getUnidadeNegocioId() {
            return unidadeNegocioId;
        }

        @Override
        public String getUnidadeNegocioCodigo() {
            return unidadeNegocioCodigo;
        }

        @Override
        public String getUnidadeNegocioNome() {
            return unidadeNegocioNome;
        }

        @Override
        public BigDecimal getValorOriginal() {
            return valorOriginal;
        }

        @Override
        public BigDecimal getValorDesconto() {
            return valorDesconto;
        }

        @Override
        public BigDecimal getValorJuros() {
            return valorJuros;
        }

        @Override
        public BigDecimal getValorMulta() {
            return valorMulta;
        }

        @Override
        public BigDecimal getValorPago() {
            return valorPago;
        }

        @Override
        public LocalDate getDataEmissao() {
            return dataEmissao;
        }

        @Override
        public LocalDate getDataVencimento() {
            return dataVencimento;
        }

        @Override
        public LocalDate getDataPagamento() {
            return dataPagamento;
        }

        @Override
        public Integer getNumeroParcela() {
            return numeroParcela;
        }

        @Override
        public Integer getTotalParcelas() {
            return totalParcelas;
        }

        @Override
        public Boolean getDeleted() {
            return deleted;
        }
    }
}
