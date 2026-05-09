package br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.entity.MovimentacaoCaixa;

public interface MovimentacaoCaixaRepository extends JpaRepository<MovimentacaoCaixa, UUID> {

    List<MovimentacaoCaixa> findByAberturaCaixaId(UUID aberturaCaixaId);

    List<MovimentacaoCaixa> findByLancamentoId(UUID lancamentoId);
}
