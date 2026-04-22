package br.com.grupopipa.gestaointegrada.cadastro.setor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;

@Repository
public interface SetorRepository
        extends JpaRepository<Setor, UUID>, JpaSpecificationExecutor<Setor> {

    interface SetorLookupProjection {
        UUID getId();
        String getNome();
    }

    @Query(value = """
            SELECT s.id, s.nome FROM setor s
            JOIN centro_custo cc ON cc.id = s.centro_custo_id
            WHERE (s.deleted IS NULL OR s.deleted = false)
              AND cc.unidade_negocio_id IN :unidadeIds
            ORDER BY s.nome
            """, nativeQuery = true)
    List<SetorLookupProjection> findByUnidadeIds(@Param("unidadeIds") Set<UUID> unidadeIds);
}
