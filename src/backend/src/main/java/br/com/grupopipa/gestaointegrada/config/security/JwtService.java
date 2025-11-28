package br.com.grupopipa.gestaointegrada.config.security;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public JwtService(JwtEncoder encoder, JwtDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public String getUsernameFromToken(String token) {
        try {
            return this.decoder.decode(token).getSubject();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean validateToken(String token) {
        try {
            this.decoder.decode(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generateAuthToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return generateToken(username, authorities, 60);
    }

    public String generateRefreshToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return generateToken(username, authorities, 60 * 24);
    }

    private String generateToken(String username, Collection<? extends GrantedAuthority> authorities, long minutes) {
        Instant now = Instant.now();

        String scope = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors
                        .joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gestao-integrada")
                .issuedAt(now)
                .expiresAt(now.plusMillis(minutes * 60000))
                .subject(username)
                .claim("scope", scope)
                .build();

        return encoder.encode(
                JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

}
