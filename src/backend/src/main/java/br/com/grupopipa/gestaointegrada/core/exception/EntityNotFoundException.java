package br.com.grupopipa.gestaointegrada.core.exception;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String className, UUID id) {
        super(String.format("Não foi possível encontrar a entidade '%s' com o id '%s'", className, id));
    }

    public EntityNotFoundException(String className, String field, String info) {
        super(String.format("Não foi possível encontrar a entidade '%s' com o %s '%s'", className, field, info));
    }

}
