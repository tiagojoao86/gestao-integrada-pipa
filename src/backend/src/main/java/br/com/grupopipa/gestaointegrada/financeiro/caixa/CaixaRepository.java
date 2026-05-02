package br.com.grupopipa.gestaointegrada.financeiro.caixa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.financeiro.caixa.entity.Caixa;

@Repository
public interface CaixaRepository
        extends JpaRepository<Caixa, UUID>, JpaSpecificationExecutor<Caixa> {
}
