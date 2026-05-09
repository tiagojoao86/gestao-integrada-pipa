package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ContaBancariaGridDTO implements GridDTO {
    private UUID id;
    private String nome;
    private String banco;
    private String tipo;
    private BigDecimal saldoInicial;
    private String unidadeNegocioCodigo;
    private Boolean ativa;
    private List<String> formasPagamento;
    private Boolean deleted;
}
