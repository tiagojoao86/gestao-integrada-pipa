package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.financeiro.entity.MovimentacaoFinanceira;

@Repository
public interface MovimentacaoFinanceiraRepository
    extends JpaRepository<MovimentacaoFinanceira, UUID>,
        JpaSpecificationExecutor<MovimentacaoFinanceira> {}
