package br.com.grupopipa.gestaointegrada.atendimento.lancamento;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiro;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;

public interface LancamentoTituloService {

    Titulo gerarTitulo(LancamentoFinanceiro lancamento);
}
