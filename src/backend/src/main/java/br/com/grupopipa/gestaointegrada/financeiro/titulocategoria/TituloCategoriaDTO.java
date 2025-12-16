package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TituloCategoriaDTO implements DTO {

    private UUID id;
    private String nome;
    private String descricao;
    private TituloCategoriaTipoEnum tipo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
