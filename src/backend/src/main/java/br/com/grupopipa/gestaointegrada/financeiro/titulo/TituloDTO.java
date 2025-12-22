package br.com.grupopipa.gestaointegrada.financeiro.titulo;

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
public class TituloDTO implements DTO {

    private UUID id;
    private String tipo; // A_PAGAR, A_RECEBER
    private String status; // ABERTO, PARCIAL, PAGO, CANCELADO, VENCIDO
    private String numeroDocumento;
    private String descricao;

    // Relacionamentos
    private UUID pessoaId;
    private String pessoaNome;
    private UUID tituloCategoriaId;
    private String tituloCategoriaNome;
    private UUID unidadeNegocioId;
    private String unidadeNegocioNome;

    // Valores
    private BigDecimal valorOriginal;
    private BigDecimal valorPago;
    private BigDecimal valorDesconto;
    private BigDecimal valorJuros;
    private BigDecimal valorMulta;
    private BigDecimal saldo; // Calculado

    // Datas
    private LocalDate dataEmissao;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;

    private String observacoes;

    // Parcelamento
    private Integer numeroParcela;
    private Integer totalParcelas;
    private UUID tituloOrigemId;

    // Setores para rateio
    private List<TituloSetorDTO> setores;

    // Rateio automático
    private Boolean rateioAutomatico;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
