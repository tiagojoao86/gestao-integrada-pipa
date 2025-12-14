package br.com.grupopipa.gestaointegrada.financeiro.categoria;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CategoriaTituloGridDTO implements GridDTO {
    private UUID id;
    private String nome;
    private String descricao;
}
