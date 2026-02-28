package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.dashboard.DFCDetalheProjection;
import br.com.grupopipa.gestaointegrada.dashboard.DFCProjection;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;

@Repository
public interface TituloRepository
        extends JpaRepository<Titulo, UUID>, JpaSpecificationExecutor<Titulo>, TituloRepositoryCustom {

    List<Titulo> findByTituloOrigem(Titulo tituloOrigem);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('month', t.data_vencimento), 'YYYY-MM') AS mes,
                   COALESCE(SUM(CASE WHEN t.tipo = 'A_RECEBER' THEN t.valor_original ELSE 0 END), 0) AS entradas,
                   COALESCE(SUM(CASE WHEN t.tipo = 'A_PAGAR'    THEN t.valor_original ELSE 0 END), 0) AS saidas
            FROM titulo t
            WHERE (t.deleted IS NULL OR t.deleted = false)
              AND t.status = 'PAGO'
              AND t.data_vencimento BETWEEN :dataInicio AND :dataFim
              AND t.unidade_negocio_id IN :unidadeIds
            GROUP BY DATE_TRUNC('month', t.data_vencimento)
            ORDER BY DATE_TRUNC('month', t.data_vencimento) ASC
            """, nativeQuery = true)
    List<DFCProjection> findFluxoCaixaCompetencia(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("unidadeIds") Collection<UUID> unidadeIds);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('month', t.data_pagamento), 'YYYY-MM') AS mes,
                   COALESCE(SUM(CASE WHEN t.tipo = 'A_RECEBER' THEN t.valor_original ELSE 0 END), 0) AS entradas,
                   COALESCE(SUM(CASE WHEN t.tipo = 'A_PAGAR'    THEN t.valor_original ELSE 0 END), 0) AS saidas
            FROM titulo t
            WHERE (t.deleted IS NULL OR t.deleted = false)
              AND t.status = 'PAGO'
              AND t.data_pagamento BETWEEN :dataInicio AND :dataFim
              AND t.unidade_negocio_id IN :unidadeIds
            GROUP BY DATE_TRUNC('month', t.data_pagamento)
            ORDER BY DATE_TRUNC('month', t.data_pagamento) ASC
            """, nativeQuery = true)
    List<DFCProjection> findFluxoCaixaCaixa(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("unidadeIds") Collection<UUID> unidadeIds);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('month', t.data_vencimento), 'YYYY-MM') AS mes,
                   CAST(tc.tipo AS VARCHAR) AS tipo,
                   CAST(COALESCE(ag.id, tc.id) AS VARCHAR) AS agrupador_id,
                   COALESCE(ag.nome, tc.nome) AS agrupador_nome,
                   COALESCE(ag.codigo, tc.codigo) AS agrupador_codigo,
                   CAST(tc.id AS VARCHAR) AS categoria_id,
                   tc.nome AS categoria_nome,
                   tc.codigo AS categoria_codigo,
                   CASE WHEN tc.agrupador_id IS NOT NULL THEN TRUE ELSE FALSE END AS tem_agrupador,
                   COALESCE(SUM(t.valor_original), 0) AS total
            FROM titulo t
            JOIN titulo_categoria tc ON tc.id = t.titulo_categoria_id
            LEFT JOIN titulo_categoria ag ON ag.id = tc.agrupador_id
            WHERE (t.deleted IS NULL OR t.deleted = false)
              AND t.status = 'PAGO'
              AND t.data_vencimento BETWEEN :dataInicio AND :dataFim
              AND t.unidade_negocio_id IN :unidadeIds
            GROUP BY DATE_TRUNC('month', t.data_vencimento), tc.tipo,
                     COALESCE(ag.id, tc.id), COALESCE(ag.nome, tc.nome),
                     COALESCE(ag.codigo, tc.codigo), tc.id, tc.nome, tc.codigo, tc.agrupador_id
            ORDER BY COALESCE(ag.codigo, tc.codigo), tc.codigo,
                     DATE_TRUNC('month', t.data_vencimento) ASC
            """, nativeQuery = true)
    List<DFCDetalheProjection> findFluxoCaixaDetalheCompetencia(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("unidadeIds") Collection<UUID> unidadeIds);

    @Query(value = """
            SELECT TO_CHAR(DATE_TRUNC('month', t.data_pagamento), 'YYYY-MM') AS mes,
                   CAST(tc.tipo AS VARCHAR) AS tipo,
                   CAST(COALESCE(ag.id, tc.id) AS VARCHAR) AS agrupador_id,
                   COALESCE(ag.nome, tc.nome) AS agrupador_nome,
                   COALESCE(ag.codigo, tc.codigo) AS agrupador_codigo,
                   CAST(tc.id AS VARCHAR) AS categoria_id,
                   tc.nome AS categoria_nome,
                   tc.codigo AS categoria_codigo,
                   CASE WHEN tc.agrupador_id IS NOT NULL THEN TRUE ELSE FALSE END AS tem_agrupador,
                   COALESCE(SUM(t.valor_original), 0) AS total
            FROM titulo t
            JOIN titulo_categoria tc ON tc.id = t.titulo_categoria_id
            LEFT JOIN titulo_categoria ag ON ag.id = tc.agrupador_id
            WHERE (t.deleted IS NULL OR t.deleted = false)
              AND t.status = 'PAGO'
              AND t.data_pagamento BETWEEN :dataInicio AND :dataFim
              AND t.unidade_negocio_id IN :unidadeIds
            GROUP BY DATE_TRUNC('month', t.data_pagamento), tc.tipo,
                     COALESCE(ag.id, tc.id), COALESCE(ag.nome, tc.nome),
                     COALESCE(ag.codigo, tc.codigo), tc.id, tc.nome, tc.codigo, tc.agrupador_id
            ORDER BY COALESCE(ag.codigo, tc.codigo), tc.codigo,
                     DATE_TRUNC('month', t.data_pagamento) ASC
            """, nativeQuery = true)
    List<DFCDetalheProjection> findFluxoCaixaDetalheCaixa(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("unidadeIds") Collection<UUID> unidadeIds);
}
