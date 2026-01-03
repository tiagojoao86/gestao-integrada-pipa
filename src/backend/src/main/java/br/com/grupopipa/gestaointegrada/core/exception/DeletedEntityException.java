package br.com.grupopipa.gestaointegrada.core.exception;

import java.util.UUID;

public class DeletedEntityException extends RuntimeException {

    public DeletedEntityException(String className, UUID id) {
        super(
                String.format(
                        "Não é possível alterar a entidade '%s' com o id '%s' pois ela foi excluída",
                        className, id));
    }
}
