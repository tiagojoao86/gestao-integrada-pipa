package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
public class UnidadeNegocioGridDTO implements GridDTO {
    private UUID id;
    private String codigo;
    private String nome;
    private String cnpj;
    private Boolean ativa;
    private Boolean deleted;
}
