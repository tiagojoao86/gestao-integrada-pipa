package br.com.grupopipa.gestaointegrada.config.security.dto;

import java.util.List;

public record AuthResponse(
    String accessToken, 
    String username, 
    String nome,
    List<AuthorityDTO> authorities
) {
}
