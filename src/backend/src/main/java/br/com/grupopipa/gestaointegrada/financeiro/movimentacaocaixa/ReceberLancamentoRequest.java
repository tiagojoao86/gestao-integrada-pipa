package br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import lombok.Data;

@Data
public class ReceberLancamentoRequest {
    private UUID aberturaCaixaId;
    private BigDecimal valorRecebido;
    private FormaPagamento formaPagamento;
    private String observacoes;
}
