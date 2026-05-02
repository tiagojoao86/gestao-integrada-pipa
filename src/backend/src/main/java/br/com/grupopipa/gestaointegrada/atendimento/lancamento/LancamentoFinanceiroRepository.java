package br.com.grupopipa.gestaointegrada.atendimento.lancamento;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiro;

public interface LancamentoFinanceiroRepository
        extends JpaRepository<LancamentoFinanceiro, UUID>,
                JpaSpecificationExecutor<LancamentoFinanceiro> {

    Optional<LancamentoFinanceiro> findByAtendimentoId(UUID atendimentoId);
}
