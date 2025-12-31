package br.com.grupopipa.gestaointegrada.financeiro.centrocusto;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class CentroCustoGridDTO implements GridDTO {
  private UUID id;
  private String nome;
  private String unidadeNegocioCodigo;
  private Boolean centroResultado;
  private Boolean deleted;
}
