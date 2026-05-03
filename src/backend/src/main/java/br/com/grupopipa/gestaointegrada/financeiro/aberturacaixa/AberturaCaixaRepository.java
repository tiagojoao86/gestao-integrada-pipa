package br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.entity.AberturaCaixa;

@Repository
public interface AberturaCaixaRepository extends JpaRepository<AberturaCaixa, UUID> {

    Optional<AberturaCaixa> findByCaixaIdAndStatus(UUID caixaId, StatusAberturaCaixa status);
}
