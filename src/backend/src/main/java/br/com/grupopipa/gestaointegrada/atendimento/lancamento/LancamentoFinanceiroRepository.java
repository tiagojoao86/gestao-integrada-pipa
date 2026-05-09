package br.com.grupopipa.gestaointegrada.atendimento.lancamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiro;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroSituacaoEnum;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroStatusFinanceiroEnum;

public interface LancamentoFinanceiroRepository
        extends JpaRepository<LancamentoFinanceiro, UUID>,
                JpaSpecificationExecutor<LancamentoFinanceiro> {

    Optional<LancamentoFinanceiro> findByAtendimentoId(UUID atendimentoId);

    Optional<LancamentoFinanceiro> findByTituloId(UUID tituloId);

    @Query("SELECT l FROM LancamentoFinanceiro l "
            + "WHERE l.situacao = :situacao "
            + "AND l.statusFinanceiro IN :statuses "
            + "AND l.unidadeNegocioId = :unidadeNegocioId "
            + "AND (l.deleted IS NULL OR l.deleted = false) "
            + "ORDER BY l.dataAtendimento DESC")
    List<LancamentoFinanceiro> findPendentesParaRecebimento(
            @Param("situacao") LancamentoFinanceiroSituacaoEnum situacao,
            @Param("statuses") List<LancamentoFinanceiroStatusFinanceiroEnum> statuses,
            @Param("unidadeNegocioId") UUID unidadeNegocioId);
}
