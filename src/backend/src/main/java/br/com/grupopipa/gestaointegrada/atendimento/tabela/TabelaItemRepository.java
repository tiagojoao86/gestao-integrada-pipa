package br.com.grupopipa.gestaointegrada.atendimento.tabela;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.grupopipa.gestaointegrada.atendimento.tabela.entity.TabelaItem;

public interface TabelaItemRepository extends JpaRepository<TabelaItem, UUID> {

    List<TabelaItem> findAllByTabelaId(UUID tabelaId);

    void deleteAllByTabelaId(UUID tabelaId);

    /**
     * Verifica se já existe um item ativo (sem vigenciaFim ou com vigenciaFim no futuro)
     * para o mesmo procedimento na mesma tabela, com início diferente (para edição).
     */
    @Query("""
        SELECT ti FROM TabelaItem ti
        WHERE ti.tabela.id = :tabelaId
          AND ti.procedimento.id = :procedimentoId
          AND (:excluirId IS NULL OR ti.id <> :excluirId)
          AND (ti.vigenciaFim IS NULL OR ti.vigenciaFim >= :referencia)
          AND ti.deleted = false
        """)
    List<TabelaItem> findItensAtivosConflitantes(
        @Param("tabelaId") UUID tabelaId,
        @Param("procedimentoId") UUID procedimentoId,
        @Param("referencia") LocalDate referencia,
        @Param("excluirId") UUID excluirId
    );

    /**
     * Busca o item vigente para um procedimento em uma tabela numa data específica.
     */
    @Query("""
        SELECT ti FROM TabelaItem ti
        WHERE ti.tabela.id = :tabelaId
          AND ti.procedimento.id = :procedimentoId
          AND ti.vigenciaInicio <= :data
          AND (ti.vigenciaFim IS NULL OR ti.vigenciaFim >= :data)
          AND ti.deleted = false
        ORDER BY ti.vigenciaInicio DESC
        """)
    Optional<TabelaItem> findItemVigente(
        @Param("tabelaId") UUID tabelaId,
        @Param("procedimentoId") UUID procedimentoId,
        @Param("data") LocalDate data
    );

    /**
     * Busca qualquer item vigente para um procedimento na data informada,
     * preferindo tabelas do tipo CONVENIO se hasConvenio=true, caso contrário PARTICULAR.
     */
    @Query("""
        SELECT ti FROM TabelaItem ti
        WHERE ti.procedimento.id = :procedimentoId
          AND ti.vigenciaInicio <= :data
          AND (ti.vigenciaFim IS NULL OR ti.vigenciaFim >= :data)
          AND ti.deleted = false
          AND ti.tabela.tipo = CASE WHEN :hasConvenio = true THEN 'CONVENIO' ELSE 'PARTICULAR' END
        ORDER BY ti.vigenciaInicio DESC
        """)
    Optional<TabelaItem> findItemVigenteParaProcedimento(
        @Param("procedimentoId") UUID procedimentoId,
        @Param("data") LocalDate data,
        @Param("hasConvenio") boolean hasConvenio
    );
}
