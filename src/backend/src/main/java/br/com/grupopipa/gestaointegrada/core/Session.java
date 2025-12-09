package br.com.grupopipa.gestaointegrada.core;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class Session {

    public static String getUsuarioUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        if (principal instanceof Jwt) {
            return ((Jwt) principal).getSubject();
        }

        return principal.toString();
    }

    /**
     * Retorna os IDs das Unidades de Negócio do usuário a partir do token JWT.
     * Se não houver token ou claim, retorna conjunto vazio (sem filtro - admin).
     * 
     * @return Set de UUIDs das unidades de negócio
     */
    public static Set<UUID> getUnidadeNegocioIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptySet();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            List<String> unidades = jwt.getClaim("unidade_negocio_ids");
            if (unidades != null && !unidades.isEmpty()) {
                return unidades.stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toSet());
            }
        }

        return Collections.emptySet();
    }

}
