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
public class Nome {

    @Column(name = "nome", nullable = false)
    private final String value;

    private Nome(String value) {
        Set<BeanValidationMessage> messages = new HashSet<>();

        Validator.of(value, "nome", messages)
                .notBlank()
                .maxLength(255);

        if (!messages.isEmpty()) {
            throw new BeanValidationException(messages);
        }

        this.value = value.trim();
    }

    private Nome() {
        this.value = null;
    }

    public static Nome of(String value) {
        return new Nome(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nome nome = (Nome) o;
        return Objects.equals(value, nome.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
}
