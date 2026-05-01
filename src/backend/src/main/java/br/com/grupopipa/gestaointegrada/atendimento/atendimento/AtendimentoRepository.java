package br.com.grupopipa.gestaointegrada.atendimento.atendimento;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.grupopipa.gestaointegrada.atendimento.atendimento.entity.Atendimento;
import br.com.grupopipa.gestaointegrada.dashboard.AtendimentoMesProjection;

public interface AtendimentoRepository
        extends JpaRepository<Atendimento, UUID>, JpaSpecificationExecutor<Atendimento> {

    @Query(value = """
            SELECT TO_CHAR(a.data_inicio, 'YYYY-MM') AS mes,
                   COUNT(*) AS total
            FROM atendimento a
            WHERE a.deleted = false
              AND DATE(a.data_inicio) BETWEEN :dataInicio AND :dataFim
              AND a.setor_id IN :setorIds
            GROUP BY mes
            ORDER BY mes
            """, nativeQuery = true)
    List<AtendimentoMesProjection> findAtendimentosPorMes(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("setorIds") Set<UUID> setorIds);

    @Query("SELECT a.id, a.numero FROM Atendimento a WHERE a.id IN :ids")
    List<Object[]> findNumerosByIds(@Param("ids") Collection<UUID> ids);

    default Map<UUID, Long> findNumerosMapByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return findNumerosByIds(ids).stream()
            .collect(Collectors.toMap(r -> (UUID) r[0], r -> (Long) r[1]));
    }
}
