package br.com.grupopipa.gestaointegrada.financeiro.centrocusto;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
public class CentroCustoGridDTO implements GridDTO {
    private UUID id;
    private String nome;
    private String unidadeNegocioCodigo;
    private Boolean centroResultado;
}
