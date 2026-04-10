package br.com.grupopipa.gestaointegrada.atendimento.tabela.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.tabela.TipoTabela;
import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TabelaDTO implements DTO {

    private UUID id;
    private String nome;
    private TipoTabela tipo;
    private Boolean ativo;
    private List<TabelaItemDTO> itens;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
