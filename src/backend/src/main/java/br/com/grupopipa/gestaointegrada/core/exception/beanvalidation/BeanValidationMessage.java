package br.com.grupopipa.gestaointegrada.core.exception.beanvalidation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BeanValidationMessage {

    private final String key;
    private final String message;

}
