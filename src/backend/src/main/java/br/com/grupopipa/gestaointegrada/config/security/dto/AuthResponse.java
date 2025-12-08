package br.com.grupopipa.gestaointegrada.config.security.dto;

import br.com.grupopipa.gestaointegrada.cadastro.usuario.UsuarioUnidadeNegocioDTO;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String username,
        String nome,
        List<AuthorityDTO> authorities,
        List<UsuarioUnidadeNegocioDTO> unidadesNegocio) {
}
