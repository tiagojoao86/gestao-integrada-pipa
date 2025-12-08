package br.com.grupopipa.gestaointegrada.config.security;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private JwtService jwtService;

    public AuthenticationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String authenticate(String username, Collection<? extends GrantedAuthority> authorities,
            Set<UUID> unidadeNegocioIds) {
        return jwtService.generateAuthToken(username, authorities, unidadeNegocioIds);
    }

    public String generateRefreshToken(String username, Collection<? extends GrantedAuthority> authorities,
            Set<UUID> unidadeNegocioIds) {
        return jwtService.generateRefreshToken(username, authorities, unidadeNegocioIds);
    }

    public Set<UUID> getUnidadeNegocioIdsFromToken(String token) {
        return jwtService.getUnidadeNegocioIdsFromToken(token);
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtService.getUsernameFromToken(token);
    }
}
