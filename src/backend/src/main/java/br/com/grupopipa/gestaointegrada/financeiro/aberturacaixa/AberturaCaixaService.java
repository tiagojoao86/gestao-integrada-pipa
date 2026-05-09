package br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.dto.LancamentoFinanceiroGridDTO;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.MovimentacaoCaixaGridDTO;

public interface AberturaCaixaService {

    AberturaCaixaDTO abrir(AbrirCaixaRequest request);

    AberturaCaixaDTO fechar(UUID id, FecharCaixaRequest request);

    Optional<AberturaCaixaDTO> findAtivaByCaixaId(UUID caixaId);

    List<CaixaComStatusDTO> listarMeusCaixas();

    CaixaComStatusDTO statusPorCaixa(UUID caixaId);

    List<LancamentoFinanceiroGridDTO> listarLancamentosPendentes(UUID aberturaCaixaId);

    List<MovimentacaoCaixaGridDTO> listarMovimentacoes(UUID aberturaCaixaId);
}
