package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
public class ContaBancariaGridDTO implements GridDTO {
    private UUID id;
    private String nome;
    private String banco;
    private String tipo;
    private BigDecimal saldoInicial;
    private Boolean ativa;
}
