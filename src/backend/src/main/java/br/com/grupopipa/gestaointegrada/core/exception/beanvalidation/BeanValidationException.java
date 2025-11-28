package br.com.grupopipa.gestaointegrada.core.exception.beanvalidation;

import java.util.Set;
import java.util.stream.Collectors;

public class BeanValidationException extends RuntimeException {

    private final String entityName;
    private final Set<BeanValidationMessage> violations;

    public BeanValidationException(Set<BeanValidationMessage> violations) {
        this(null, violations);
    }

    public BeanValidationException(String entityName, Set<BeanValidationMessage> violations) {
        super(buildMessage(violations));
        this.entityName = entityName;
        this.violations = violations;
    }

    private static String buildMessage(Set<BeanValidationMessage> violations) {
        return violations.stream()
                .map(v -> String.format("'%s': %s", v.getKey(),
                        v.getMessage()))
                .collect(Collectors.joining(", "));
    }

    public String getEntityName() {
        return entityName;
    }

    public Set<BeanValidationMessage> getViolations() {
        return violations;
    }
}