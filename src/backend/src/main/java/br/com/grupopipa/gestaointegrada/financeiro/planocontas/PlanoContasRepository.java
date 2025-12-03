package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlanoContasRepository extends JpaRepository<PlanoContas, UUID>, JpaSpecificationExecutor<PlanoContas> {
}
