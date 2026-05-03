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
public class CaixaComStatusDTO {
    private UUID caixaId;
    private String caixaNome;
    private BigDecimal valorPadraoAbertura;
    private StatusAberturaCaixa statusSessao;
    private UUID aberturaCaixaId;
    private LocalDateTime dataAbertura;
    private String usuarioNomeAbertura;
}
