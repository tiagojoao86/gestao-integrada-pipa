package br.com.grupopipa.gestaointegrada.core.valueobject;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Value Object para Email com validação de formato */
@Embeddable
public class Email implements Serializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Column(name = "email", length = 255)
    private String value;

    protected Email() {
    }

    public Email(String value) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (value == null || value.isBlank()) {
            violations.add(new BeanValidationMessage("validation.email.required", "O campo 'E-mail' é obrigatório."));
            throw new BeanValidationException(violations);
        }

        String emailTrimmed = value.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(emailTrimmed).matches()) {
            violations.add(new BeanValidationMessage("validation.email.invalid", "E-mail inválido."));
            throw new BeanValidationException(violations);
        }

        this.value = emailTrimmed;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Email)) {
            return false;
        }
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
