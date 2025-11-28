package br.com.grupopipa.gestaointegrada.core.validation;

import java.util.Set;
import java.util.function.Supplier;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;

public final class ValidationUtils {

    private ValidationUtils() {        
    }

    /**
     * Executa um supplier que cria um Value Object, capturando BeanValidationExceptions.
     * Se uma exceção for capturada, as violações são adicionadas ao conjunto fornecido
     * e o método retorna null. Caso contrário, retorna o objeto criado.
     *
     * @param <T> O tipo do objeto a ser criado.
     * @param supplier A função que cria o objeto (ex: () -> Nome.of(valor)).
     * @param violations O conjunto onde as mensagens de erro serão adicionadas.
     * @return O objeto criado ou null em caso de falha na validação.
     */
    public static <T> T validateAndGet(Supplier<T> supplier, Set<BeanValidationMessage> violations) {
        try {
            return supplier.get();
        } catch (BeanValidationException e) {
            violations.addAll(e.getViolations());
            return null;
        }
    }
}