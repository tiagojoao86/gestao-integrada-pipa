package br.com.grupopipa.gestaointegrada.financeiro.aberturacaixa;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbrirCaixaRequest {
    private UUID caixaId;
    private BigDecimal valorAbertura;
}
