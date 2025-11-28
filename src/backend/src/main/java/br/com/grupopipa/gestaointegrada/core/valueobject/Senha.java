package br.com.grupopipa.gestaointegrada.core.valueobject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;

@Embeddable
public class Senha {

    @Column(name = "senha", nullable = false)
    private final String value;

    @Transient
    private final PasswordEncoder passwordEncoder;

    private Senha(String rawPassword, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        Set<BeanValidationMessage> messages = new HashSet<>();

        Validator.of(rawPassword, "senha", messages)
                .notBlank()
                .minLength(8);

        if (!messages.isEmpty()) {
            throw new BeanValidationException(messages);
        }

        this.value = this.passwordEncoder.encode(rawPassword.trim());
    }

    private Senha() {
        this.passwordEncoder = null;
        this.value = null;
    }

    public static Senha of(String rawPassword, PasswordEncoder passwordEncoder) {
        Objects.requireNonNull(passwordEncoder, "PasswordEncoder não pode ser nulo");
        return new Senha(rawPassword, passwordEncoder);
    }

    public String getValue() {
        return value;
    }

    public boolean matches(String rawPassword) {
        return this.passwordEncoder.matches(rawPassword, this.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Senha senha = (Senha) o;
        return Objects.equals(value, senha.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
}
