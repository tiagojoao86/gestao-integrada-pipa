package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MovimentacaoTituloDTO {
  private UUID id;
  private String descricao;
}
