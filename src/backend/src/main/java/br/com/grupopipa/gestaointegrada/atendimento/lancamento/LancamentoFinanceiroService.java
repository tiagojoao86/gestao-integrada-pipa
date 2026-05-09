package br.com.grupopipa.gestaointegrada.atendimento.lancamento;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroDTO;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroGridDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.dto.ResolverProcedimentoResponse;
import br.com.grupopipa.gestaointegrada.core.controller.Response;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.ReceberLancamentoRequest;

public interface LancamentoFinanceiroService
        extends CrudService<LancamentoFinanceiroDTO, LancamentoFinanceiroGridDTO> {

    Response fecharParaPagamento(UUID id);

    Response fecharParaFaturamento(UUID id);

    Response cancelar(UUID id);

    Response receber(UUID id, ReceberLancamentoRequest request);

    ResolverProcedimentoResponse resolverProcedimento(UUID lancamentoId, UUID procedimentoId);
}
