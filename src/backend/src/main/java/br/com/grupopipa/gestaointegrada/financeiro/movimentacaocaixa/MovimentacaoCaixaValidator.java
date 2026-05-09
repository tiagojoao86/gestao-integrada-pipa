package br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa.entity.AberturaCaixa;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;

public class MovimentacaoCaixaValidator {

    private MovimentacaoCaixaValidator() {}

    public static ValidatedData validate(
            AberturaCaixa aberturaCaixa,
            UUID lancamentoId,
            UUID tituloId,
            BigDecimal valor,
            FormaPagamento formaPagamento,
            String observacoes) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (aberturaCaixa == null) {
            violations.add(new BeanValidationMessage(
                "aberturaCaixaId", "Sessão de caixa é obrigatória."));
        }
        if (lancamentoId == null) {
            violations.add(new BeanValidationMessage(
                "lancamentoId", "Lançamento é obrigatório."));
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            violations.add(new BeanValidationMessage(
                "valorRecebido", "O valor do recebimento deve ser positivo."));
        }
        if (formaPagamento == null) {
            violations.add(new BeanValidationMessage(
                "formaPagamento", "Forma de pagamento é obrigatória."));
        }

        if (!violations.isEmpty()) {
            throw new BeanValidationException("movimentacaoCaixa", violations);
        }

        return new ValidatedData(aberturaCaixa, lancamentoId, tituloId,
            valor, formaPagamento, observacoes);
    }

    public static class ValidatedData {
        public final AberturaCaixa aberturaCaixa;
        public final UUID lancamentoId;
        public final UUID tituloId;
        public final BigDecimal valor;
        public final FormaPagamento formaPagamento;
        public final String observacoes;

        ValidatedData(
                AberturaCaixa aberturaCaixa,
                UUID lancamentoId,
                UUID tituloId,
                BigDecimal valor,
                FormaPagamento formaPagamento,
                String observacoes) {
            this.aberturaCaixa = aberturaCaixa;
            this.lancamentoId = lancamentoId;
            this.tituloId = tituloId;
            this.valor = valor;
            this.formaPagamento = formaPagamento;
            this.observacoes = observacoes;
        }
    }
}
