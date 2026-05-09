package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;

@Repository
public interface ContaBancariaRepository
        extends JpaRepository<ContaBancaria, UUID>, JpaSpecificationExecutor<ContaBancaria> {

    @Query("SELECT c FROM ContaBancaria c WHERE :fp MEMBER OF c.formasPagamento"
            + " AND (c.deleted IS NULL OR c.deleted = false)")
    List<ContaBancaria> findByFormaPagamento(@Param("fp") FormaPagamento fp);

    @Query("SELECT c FROM ContaBancaria c WHERE :fp MEMBER OF c.formasPagamento"
            + " AND c.id != :excludeId AND (c.deleted IS NULL OR c.deleted = false)")
    List<ContaBancaria> findByFormaPagamentoExcluindo(
            @Param("fp") FormaPagamento fp, @Param("excludeId") UUID excludeId);

    @Query("SELECT c FROM ContaBancaria c WHERE :fp MEMBER OF c.formasPagamento"
            + " AND (c.deleted IS NULL OR c.deleted = false)")
    Optional<ContaBancaria> findFirstByFormaPagamento(@Param("fp") FormaPagamento fp);
}
