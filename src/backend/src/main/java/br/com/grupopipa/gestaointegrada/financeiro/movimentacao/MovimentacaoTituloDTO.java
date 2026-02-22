package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MovimentacaoTituloDTO {
    private UUID id;
    private String descricao;
    /** Valor aplicado a este título nesta movimentação (saldo quitado). */
    private BigDecimal valor;
    /** Tipo do título (A_PAGAR ou A_RECEBER). */
    private String tipo;
}
