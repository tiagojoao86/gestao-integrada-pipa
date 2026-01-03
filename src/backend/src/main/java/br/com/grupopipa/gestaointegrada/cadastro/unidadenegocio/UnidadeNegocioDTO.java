package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UnidadeNegocioDTO implements DTO {

    private UUID id;
    private String codigo;
    private String nome;
    private String descricao;
    private String cnpj;
    private Boolean ativa;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
