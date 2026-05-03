package br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AberturaCaixaDTO {
    private UUID id;
    private UUID caixaId;
    private String caixaNome;
    private UUID usuarioId;
    private String usuarioNome;
    private StatusAberturaCaixa status;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private BigDecimal valorAbertura;
    private BigDecimal valorConferencia;
    private String observacoes;
}
