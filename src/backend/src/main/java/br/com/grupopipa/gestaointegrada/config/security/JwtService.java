package br.com.grupopipa.gestaointegrada.config.security;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@SuppressWarnings("checkstyle:MagicNumber")
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

    public String generateAuthToken(
            String username,
            Collection<? extends GrantedAuthority> authorities,
            Set<UUID> unidadeNegocioIds) {
        return generateToken(username, authorities, unidadeNegocioIds, 60);
    }

    public String generateRefreshToken(
            String username,
            Collection<? extends GrantedAuthority> authorities,
            Set<UUID> unidadeNegocioIds) {
        return generateToken(username, authorities, unidadeNegocioIds, 60 * 24);
    }

    private String generateToken(
            String username,
            Collection<? extends GrantedAuthority> authorities,
            Set<UUID> unidadeNegocioIds,
            long minutes) {
        Instant now = Instant.now();

        Map<String, Integer> perms = buildPermsBitmask(authorities);

        List<String> unidadeNegocioIdStrings = unidadeNegocioIds != null
                ? unidadeNegocioIds.stream().map(UUID::toString).collect(Collectors.toList())
                : List.of();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gestao-integrada")
                .issuedAt(now)
                .expiresAt(now.plusMillis(minutes * 60000))
                .subject(username)
                .claim("perms", perms)
                .claim("unidade_negocio_ids", unidadeNegocioIdStrings)
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Converte authorities no formato "MODULO_PERMISSAO" para um mapa de bitmask.
     * Bits: LISTAR=1, VISUALIZAR=2, EDITAR=4, DELETAR=8, AUDITAR=16
     */
    private Map<String, Integer> buildPermsBitmask(Collection<? extends GrantedAuthority> authorities) {
        Map<String, Integer> perms = new HashMap<>();
        String[] sufixos = { "_LISTAR", "_VISUALIZAR", "_EDITAR", "_DELETAR", "_AUDITAR" };
        int[] bits = { 1, 2, 4, 8, 16 };

        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            for (int i = 0; i < sufixos.length; i++) {
                if (auth.endsWith(sufixos[i])) {
                    String modulo = auth.substring(0, auth.length() - sufixos[i].length());
                    int bit = bits[i];
                    perms.merge(modulo, bit, Integer::sum);
                    break;
                }
            }
        }
        return perms;
    }

    public Set<UUID> getUnidadeNegocioIdsFromToken(String token) {
        try {
            Jwt jwt = this.decoder.decode(token);
            List<String> unidadeNegocioIdStrings = jwt.getClaim("unidade_negocio_ids");
            if (unidadeNegocioIdStrings == null || unidadeNegocioIdStrings.isEmpty()) {
                return Set.of();
            }
            return unidadeNegocioIdStrings.stream().map(UUID::fromString).collect(Collectors.toSet());
        } catch (Exception e) {
            return Set.of();
        }
    }
}
