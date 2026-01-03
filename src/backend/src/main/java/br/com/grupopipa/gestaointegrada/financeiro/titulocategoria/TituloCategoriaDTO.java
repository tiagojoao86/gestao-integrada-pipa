package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TituloCategoriaDTO implements DTO {

    private UUID id;
    private String codigo;
    private String nome;
    private String descricao;
    private TituloCategoriaTipoEnum tipo;
    private UUID agrupadorId;
    private String agrupadorNome;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
