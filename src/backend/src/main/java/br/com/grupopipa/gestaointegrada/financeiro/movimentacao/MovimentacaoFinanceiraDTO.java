package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class MovimentacaoFinanceiraDTO implements DTO {

    private UUID id;
    private List<MovimentacaoTituloDTO> titulos;
    private UUID contaBancariaId;
    private String contaBancariaNome;
    private String tipo; // PAGAMENTO, RECEBIMENTO, ESTORNO, TRANSFERENCIA
    private String formaPagamento; // PIX, DINHEIRO, BOLETO, etc
    private BigDecimal valor;
    private LocalDate data;
    private String observacoes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
