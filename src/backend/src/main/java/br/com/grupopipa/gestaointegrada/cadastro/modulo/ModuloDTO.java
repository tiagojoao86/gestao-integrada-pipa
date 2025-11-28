package br.com.grupopipa.gestaointegrada.cadastro.modulo;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ModuloDTO implements DTO {
    private UUID id;
    private String chave;
    private String nome;
    private GrupoModuloEnum grupoEnum;
}
