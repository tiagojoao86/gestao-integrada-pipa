package br.com.grupopipa.gestaointegrada.config.security;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;

import br.com.grupopipa.gestaointegrada.core.Session;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(Session.getUsuarioUsername());
    }
}
