package br.com.grupopipa.gestaointegrada.financeiro.condicaopagamento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CondicaoPagamentoDTO implements DTO {

    private UUID id;
    private String condicao;
    private String descricao;
    private Boolean ativo;
    private Integer quantidadeParcelas;
    private List<Integer> diasVencimento;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
