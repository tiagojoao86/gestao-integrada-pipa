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
public class Chave {

    @Column(name = "chave", nullable = false)
    private final String value;

    private Chave(String value) {
        Set<BeanValidationMessage> messages = new HashSet<>();

        Validator.of(value, "nome", messages)
                .notBlank()
                .maxLength(255);

        if (!messages.isEmpty()) {
            throw new BeanValidationException(messages);
        }

        this.value = value.trim();
    }

    private Chave() {
        this.value = null;
    }

    public static Chave of(String value) {
        return new Chave(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chave chave = (Chave) o;
        return Objects.equals(value, chave.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
}
