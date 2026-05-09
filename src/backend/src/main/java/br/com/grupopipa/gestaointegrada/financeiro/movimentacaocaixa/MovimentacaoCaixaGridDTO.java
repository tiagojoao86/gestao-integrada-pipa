package br.com.grupopipa.gestaointegrada.financeiro.movimentacaocaixa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MovimentacaoCaixaGridDTO {
    private UUID id;
    private BigDecimal valor;
    private String formaPagamento;
    private String formaPagamentoDescricao;
    private LocalDateTime dataHora;
    private String observacoes;
    private UUID lancamentoId;
}
