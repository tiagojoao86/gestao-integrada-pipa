package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class MovimentacaoTituloDTO {
    private UUID id;
    private String descricao;
}
