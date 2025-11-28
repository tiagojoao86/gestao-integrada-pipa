package br.com.grupopipa.gestaointegrada.config.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private JwtService jwtService;

    public AuthenticationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String authenticate(String username, Collection<? extends GrantedAuthority> authorities) {
        return jwtService.generateAuthToken(username, authorities);
    }

    public String generateRefreshToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return jwtService.generateRefreshToken(username, authorities);
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtService.getUsernameFromToken(token);
    }
}
