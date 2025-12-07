package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlanoContasRepository extends JpaRepository<PlanoContas, UUID>, JpaSpecificationExecutor<PlanoContas> {

    /**
     * Verifica se um plano de contas é analítico (não possui filhos)
     */
    @Query("SELECT CASE WHEN COUNT(f) = 0 THEN true ELSE false END FROM PlanoContas p LEFT JOIN p.planosFilhos f WHERE p.id = :planoId")
    boolean isAnalitico(@Param("planoId") UUID planoId);
}
