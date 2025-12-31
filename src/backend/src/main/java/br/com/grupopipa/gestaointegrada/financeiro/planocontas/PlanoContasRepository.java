package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;

@Repository
public interface PlanoContasRepository
    extends JpaRepository<PlanoContas, UUID>, JpaSpecificationExecutor<PlanoContas> {

  /** Verifica se um plano de contas é analítico (não possui filhos) */
  @Query(
      "SELECT CASE WHEN COUNT(f) = 0 THEN true ELSE false END "
          + "FROM PlanoContas p LEFT JOIN p.planosFilhos f WHERE p.id = :planoId")
  boolean isAnalitico(@Param("planoId") UUID planoId);

  /** Lista planos de contas ativos de uma unidade de negócio específica */
  List<PlanoContas> findByAtivoTrueAndUnidadeNegocioId(UUID unidadeNegocioId);
}
