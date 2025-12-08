package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UsuarioUnidadeNegocioDTO {
    private UUID unidadeNegocioId;
    private String unidadeNegocioNome;
    private Boolean isDefault;
}
