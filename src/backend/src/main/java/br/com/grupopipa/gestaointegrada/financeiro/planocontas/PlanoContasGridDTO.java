package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
public class PlanoContasGridDTO implements GridDTO {
    private UUID id;
    private String codigo;
    private String descricao;
    private String tipo;
    private String planoPaiCodigo;
    private String planoPaiDescricao;
    private String unidadeNegocioCodigo;
    private Boolean ativo;
    private Boolean analitico;
    private Boolean deleted;
}
