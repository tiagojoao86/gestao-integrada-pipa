package br.com.grupopipa.gestaointegrada.financeiro.condicaopagamento;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CondicaoPagamentoGridDTO implements GridDTO {
    private UUID id;
    private String condicao;
    private Boolean ativo;
    private Integer quantidadeParcelas;
    private Boolean deleted;
}
