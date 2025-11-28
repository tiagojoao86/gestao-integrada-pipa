package br.com.grupopipa.gestaointegrada.core.validation;

import java.util.Set;
import java.util.regex.Pattern;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;

public class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    private final Object value;
    private final String fieldName;
    private final Set<BeanValidationMessage> messages;

    private Validator(Object value, String fieldName, Set<BeanValidationMessage> messages) {
        this.value = value;
        this.fieldName = fieldName;
        this.messages = messages;
    }

    public static Validator of(Object value, String fieldName, Set<BeanValidationMessage> messages) {
        return new Validator(value, fieldName, messages);
    }

    public Validator notNull() {
        if (value == null) {
            addMessage("notNull", String.format("The field '%s' is required.", fieldName));
        }
        return this;
    }

    public Validator notBlank() {
        if (value == null || !(value instanceof String) || ((String) value).trim().isEmpty()) {
            addMessage("notBlank", String.format("The field '%s' is required and cannot be blank.", fieldName));
        }
        return this;
    }

    public Validator maxLength(int max) {
        if (value instanceof String) {
            if (((String) value).trim().length() > max) {
                addMessage("maxLength",
                        String.format("The field '%s' cannot be bigger than %d characters.", fieldName, max));
            }
        }
        return this;
    }

    public Validator minLength(int min) {
        if (value instanceof String) {
            if (((String) value).trim().length() < min) {
                addMessage("minLength",
                        String.format("The field '%s' must be at least %d characters long.", fieldName, min));
            }
        }
        return this;
    }

    public Validator emailFormat() {
        if (value instanceof String && !((String) value).trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher((String) value).matches()) {
                addMessage("email", String.format("The field '%s' has an invalid email format.", fieldName));
            }
        }
        return this;
    }

    public Validator greaterThan(Number number) {
        if (value instanceof Number) {
            if (((Number) value).doubleValue() <= number.doubleValue()) {
                addMessage("greaterThan", String.format("The field '%s' must be greater than %s.", fieldName, number));
            }
        }
        return this;
    }

    private void addMessage(String ruleKey, String defaultMessage) {
        this.messages.add(new BeanValidationMessage(
                String.format("%s.%s", fieldName, ruleKey),
                defaultMessage));
    }
}
