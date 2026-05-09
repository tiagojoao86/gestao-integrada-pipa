package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TituloCategoriaGridDTO implements GridDTO {
    private UUID id;
    private String codigo;
    private String nome;
    private String descricao;
    private TituloCategoriaTipoEnum tipo;
    private String agrupadorNome;
    private Boolean padrao;
    private Boolean deleted;
}
