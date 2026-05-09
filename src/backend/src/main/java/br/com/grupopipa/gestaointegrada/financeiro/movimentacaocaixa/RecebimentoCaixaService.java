package br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiro;

public interface RecebimentoCaixaService {

    LancamentoFinanceiro registrar(UUID lancamentoId, ReceberLancamentoRequest request);
}
