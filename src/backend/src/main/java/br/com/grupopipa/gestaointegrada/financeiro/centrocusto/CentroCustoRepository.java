package br.com.grupopipa.gestaointegrada.financeiro.centrocusto;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.financeiro.entity.CentroCusto;

@Repository
public interface CentroCustoRepository
    extends JpaRepository<CentroCusto, UUID>, JpaSpecificationExecutor<CentroCusto> {}
