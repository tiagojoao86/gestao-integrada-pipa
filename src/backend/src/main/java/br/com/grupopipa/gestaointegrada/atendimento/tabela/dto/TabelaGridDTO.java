package br.com.grupopipa.gestaointegrada.atendimento.tabela.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.tabela.TipoTabela;
import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TabelaGridDTO implements GridDTO {

    private UUID id;
    private String nome;
    private TipoTabela tipo;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private Boolean deleted;
}
