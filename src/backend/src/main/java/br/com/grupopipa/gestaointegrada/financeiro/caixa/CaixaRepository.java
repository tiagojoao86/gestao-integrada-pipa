package br.com.grupopipa.gestaointegrada.financeiro.caixa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.financeiro.caixa.entity.Caixa;

@Repository
public interface CaixaRepository
        extends JpaRepository<Caixa, UUID>, JpaSpecificationExecutor<Caixa> {

    @Query("SELECT c FROM Caixa c WHERE c.ativo = true AND (c.deleted IS NULL OR c.deleted = false)"
            + " ORDER BY c.nome ASC")
    List<Caixa> findAllAtivos();

    @Query("SELECT c FROM Caixa c JOIN c.usuarioIds u WHERE u = :usuarioId"
            + " AND (c.deleted IS NULL OR c.deleted = false)")
    List<Caixa> findByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
