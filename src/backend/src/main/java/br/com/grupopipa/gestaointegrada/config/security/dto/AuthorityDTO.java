package br.com.grupopipa.gestaointegrada.config.security.dto;

import java.util.List;

public record AuthorityDTO(
    String chave,
    String nome,
    String grupo,
    List<String> permissoes
) {
    
}