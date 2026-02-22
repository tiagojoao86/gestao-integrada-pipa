package br.com.grupopipa.gestaointegrada.core.exception.beanvalidation;

import lombok.Getter;

@Getter
public class BeanValidationMessage {

    private final String key;
    private final Object[] args;
    private final String message;

    /** Constructor for messages without arguments (e.g. CPF, CNPJ, Email). */
    public BeanValidationMessage(String key, String message) {
        this.key = key;
        this.args = new Object[]{};
        this.message = message;
    }

    /** Constructor for parameterized messages (e.g. field name, max length). */
    public BeanValidationMessage(String key, Object[] args, String message) {
        this.key = key;
        this.args = args;
        this.message = message;
    }
}
