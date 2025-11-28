package br.com.grupopipa.gestaointegrada.core.valueobject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.Validator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Login {

    @Column(name = "login", nullable = false, unique = true)
    private final String value;

    private Login(String value) {
        Set<BeanValidationMessage> messages = new HashSet<>();

        Validator.of(value, "login", messages)
                .notBlank()
                .maxLength(100);

        if (!messages.isEmpty()) {
            throw new BeanValidationException(messages);
        }

        this.value = value.trim();
    }

    private Login() {
        this.value = null;
    }

    public static Login of(String value) {
        return new Login(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Login login = (Login) o;
        return Objects.equals(value, login.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
}
