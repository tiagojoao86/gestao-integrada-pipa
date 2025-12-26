package br.com.grupopipa.gestaointegrada.cadastro.setor;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SetorGridDTO implements GridDTO {
    private UUID id;
    private String nome;
    private String descricao;
    private String centroCustoNome;
    private Boolean deleted;
}
