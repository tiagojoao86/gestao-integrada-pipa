package br.com.grupopipa.gestaointegrada.config.security.dto;

import java.util.List;

import br.com.grupopipa.gestaointegrada.cadastro.usuario.UsuarioUnidadeNegocioDTO;

public record AuthResponse(
    String accessToken,
    String username,
    String nome,
    List<AuthorityDTO> authorities,
    List<UsuarioUnidadeNegocioDTO> unidadesNegocio) {}
