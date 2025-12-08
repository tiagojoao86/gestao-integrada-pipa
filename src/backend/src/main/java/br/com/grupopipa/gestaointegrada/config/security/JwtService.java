package br.com.grupopipa.gestaointegrada.config.security;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
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

    public String generateAuthToken(String username, Collection<? extends GrantedAuthority> authorities,
            Set<UUID> unidadeNegocioIds) {
        return generateToken(username, authorities, unidadeNegocioIds, 60);
    }

    public String generateRefreshToken(String username, Collection<? extends GrantedAuthority> authorities,
            Set<UUID> unidadeNegocioIds) {
        return generateToken(username, authorities, unidadeNegocioIds, 60 * 24);
    }

    private String generateToken(String username, Collection<? extends GrantedAuthority> authorities,
            Set<UUID> unidadeNegocioIds, long minutes) {
        Instant now = Instant.now();

        String scope = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors
                        .joining(" "));

        List<String> unidadeNegocioIdStrings = unidadeNegocioIds != null
                ? unidadeNegocioIds.stream().map(UUID::toString).collect(Collectors.toList())
                : List.of();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gestao-integrada")
                .issuedAt(now)
                .expiresAt(now.plusMillis(minutes * 60000))
                .subject(username)
                .claim("scope", scope)
                .claim("unidade_negocio_ids", unidadeNegocioIdStrings)
                .build();

        return encoder.encode(
                JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    public Set<UUID> getUnidadeNegocioIdsFromToken(String token) {
        try {
            Jwt jwt = this.decoder.decode(token);
            List<String> unidadeNegocioIdStrings = jwt.getClaim("unidade_negocio_ids");
            if (unidadeNegocioIdStrings == null || unidadeNegocioIdStrings.isEmpty()) {
                return Set.of();
            }
            return unidadeNegocioIdStrings.stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            return Set.of();
        }
    }

}
