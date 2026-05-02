package br.com.grupopipa.gestaointegrada.financeiro.caixa;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CaixaGridDTO implements GridDTO {
    private UUID id;
    private String nome;
    private BigDecimal valorPadraoAbertura;
    private Boolean percentualParcialConfigurado;
    private Boolean ativo;
    private Boolean deleted;
}
