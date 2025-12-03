package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
public class MovimentacaoFinanceiraGridDTO implements GridDTO {
    private UUID id;
    private String tituloDescricao;
    private String contaBancariaNome;
    private String tipo;
    private String formaPagamento;
    private BigDecimal valor;
    private LocalDate data;
}
