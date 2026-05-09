package br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.lancamento.LancamentoFinanceiroRepository;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiro;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroSituacaoEnum;
import br.com.grupopipa.gestaointegrada.atendimento.lancamento.entity.LancamentoFinanceiroStatusFinanceiroEnum;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.AberturaCaixaRepository;
import br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.StatusAberturaCaixa;
import br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.entity.AberturaCaixa;
import br.com.grupopipa.gestaointegrada.financeiro.contabancaria.ContaBancariaRepository;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa.entity.MovimentacaoCaixa;

@Service
@Transactional(readOnly = true)
public class RecebimentoCaixaServiceImpl implements RecebimentoCaixaService {

    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    private final AberturaCaixaRepository aberturaCaixaRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final ContaBancariaRepository contaBancariaRepository;

    public RecebimentoCaixaServiceImpl(
            MovimentacaoCaixaRepository movimentacaoCaixaRepository,
            AberturaCaixaRepository aberturaCaixaRepository,
            LancamentoFinanceiroRepository lancamentoRepository,
            ContaBancariaRepository contaBancariaRepository) {
        this.movimentacaoCaixaRepository = movimentacaoCaixaRepository;
        this.aberturaCaixaRepository = aberturaCaixaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.contaBancariaRepository = contaBancariaRepository;
    }

    @Override
    @Transactional
    public LancamentoFinanceiro registrar(UUID lancamentoId, ReceberLancamentoRequest request) {
        AberturaCaixa abertura = buscarAbertura(request.getAberturaCaixaId());
        LancamentoFinanceiro lancamento = buscarLancamento(lancamentoId);
        validarUnidadeCaixa(abertura, lancamento);
        validarContaParaFormaPagamento(request.getFormaPagamento());

        MovimentacaoCaixa mov = new MovimentacaoCaixa.Builder()
                .aberturaCaixa(abertura)
                .lancamentoId(lancamentoId)
                .tituloId(lancamento.getTituloId())
                .valor(request.getValorRecebido())
                .formaPagamento(request.getFormaPagamento())
                .observacoes(request.getObservacoes())
                .build();
        movimentacaoCaixaRepository.save(mov);

        return lancamento;
    }

    private void validarContaParaFormaPagamento(FormaPagamento formaPagamento) {
        if (formaPagamento == null) {
            return;
        }
        boolean existeConta = !contaBancariaRepository.findByFormaPagamento(formaPagamento).isEmpty();
        if (!existeConta) {
            throw new BeanValidationException("recebimentoCaixa",
                    Set.of(new BeanValidationMessage("formaPagamento",
                            "Nenhuma conta financeira configurada para a forma de pagamento '"
                                    + formaPagamento.getDescricao() + "'.")));
        }
    }

    private void validarUnidadeCaixa(AberturaCaixa abertura, LancamentoFinanceiro lancamento) {
        var unidadeCaixa = abertura.getCaixa().getUnidadeNegocio();
        if (unidadeCaixa == null) {
            return;
        }
        if (lancamento.getUnidadeNegocioId() == null
                || !unidadeCaixa.getId().equals(lancamento.getUnidadeNegocioId())) {
            throw new BeanValidationException("recebimentoCaixa",
                    Set.of(new BeanValidationMessage("aberturaCaixaId",
                            "Este lançamento pertence a outra unidade de negócio.")));
        }
    }

    private AberturaCaixa buscarAbertura(UUID id) {
        AberturaCaixa abertura = aberturaCaixaRepository.findById(id)
                .orElseThrow(() -> new BeanValidationException("recebimentoCaixa",
                        Set.of(new BeanValidationMessage(
                            "aberturaCaixaId", "Sessão de caixa não encontrada."))));
        if (abertura.getStatus() != StatusAberturaCaixa.ABERTO) {
            throw new BeanValidationException("recebimentoCaixa",
                    Set.of(new BeanValidationMessage(
                        "aberturaCaixaId", "A sessão de caixa não está aberta.")));
        }
        return abertura;
    }

    private LancamentoFinanceiro buscarLancamento(UUID id) {
        LancamentoFinanceiro lancamento = lancamentoRepository.findById(id)
                .orElseThrow(() -> new BeanValidationException("recebimentoCaixa",
                        Set.of(new BeanValidationMessage(
                            "lancamentoId", "Lançamento financeiro não encontrado."))));
        if (lancamento.getSituacao() != LancamentoFinanceiroSituacaoEnum.FECHADO) {
            throw new BeanValidationException("recebimentoCaixa",
                    Set.of(new BeanValidationMessage(
                        "lancamentoId",
                        "Apenas lançamentos fechados podem ser recebidos no caixa.")));
        }
        LancamentoFinanceiroStatusFinanceiroEnum status = lancamento.getStatusFinanceiro();
        boolean permiteReceber = status == LancamentoFinanceiroStatusFinanceiroEnum.PENDENTE
                || status == LancamentoFinanceiroStatusFinanceiroEnum.PAGO_PARCIAL;
        if (!permiteReceber) {
            throw new BeanValidationException("recebimentoCaixa",
                    Set.of(new BeanValidationMessage(
                        "lancamentoId", "Este lançamento não está disponível para recebimento.")));
        }
        return lancamento;
    }
}
