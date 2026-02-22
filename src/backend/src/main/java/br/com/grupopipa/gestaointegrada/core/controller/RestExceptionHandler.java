package br.com.grupopipa.gestaointegrada.core.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import br.com.grupopipa.gestaointegrada.core.dao.DatabaseConstraintsEnum;
import br.com.grupopipa.gestaointegrada.core.exception.DeletedEntityException;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INVALID_DATA = "Invalid Data";
    private static final String RESOURCE_NOT_FOUND = "Resource not found";
    private static final String INTERNAL_SERVER_ERROR = "Internal server error";
    private static final String UNEXPECTED_ERROR_DETAIL = "An unexpected internal system error has occurred.";

    // Message codes (resolved via MessageSource)
    private static final String CODE_NOT_AUTHORIZED = "error.notAuthorized";
    private static final String CODE_RESOURCE_NOT_FOUND = "error.resourceNotFound";
    private static final String CODE_DELETED_ENTITY = "error.deletedEntity";
    private static final String CODE_BAD_CREDENTIAL = "error.badCredential";
    private static final String CODE_INTERNAL_SERVER_ERROR = "error.internalServerError";

    // Portuguese fallbacks (used when MessageSource is unavailable or key not found)
    private static final String FALLBACK_NOT_AUTHORIZED = "Você não tem permissão para realizar esta ação.";
    private static final String FALLBACK_RESOURCE_NOT_FOUND = "Recurso não encontrado.";
    private static final String FALLBACK_DELETED_ENTITY = "Não é possível alterar um registro que foi excluído.";
    private static final String FALLBACK_BAD_CREDENTIAL = "Credenciais inválidas.";
    private static final String FALLBACK_INTERNAL_SERVER_ERROR = "Erro interno do servidor.";

    @Autowired(required = false)
    private MessageSource messageSource;

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Object> handleAuthorizationDeniedException(
            AuthorizationDeniedException ex, WebRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(HttpStatus.FORBIDDEN.getReasonPhrase())
                .messages(List.of(resolve(CODE_NOT_AUTHORIZED, null, FALLBACK_NOT_AUTHORIZED)))
                .detail(List.of(ex.getMessage()))
                .build();
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntidadeNaoEncontrada(
            EntityNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.error(RESOURCE_NOT_FOUND + ": " + ex.getMessage());
        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(RESOURCE_NOT_FOUND)
                .messages(List.of(resolve(CODE_RESOURCE_NOT_FOUND, null, FALLBACK_RESOURCE_NOT_FOUND)))
                .detail(List.of(ex.getMessage()))
                .build();
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(DeletedEntityException.class)
    public ResponseEntity<Object> handleDeletedEntityException(
            DeletedEntityException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.error(INVALID_DATA + ": " + ex.getMessage());
        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(INVALID_DATA)
                .messages(List.of(resolve(CODE_DELETED_ENTITY, null, FALLBACK_DELETED_ENTITY)))
                .detail(List.of(ex.getMessage()))
                .build();
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(BeanValidationException.class)
    public ResponseEntity<Object> handleBeanValidationException(
            BeanValidationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String detail = ex.getViolations().stream()
                .map(v -> String.format("'%s': %s", v.getKey(), v.getMessage()))
                .collect(Collectors.joining(",\n"));

        List<String> messages = ex.getViolations().stream()
                .map(v -> resolve(v.getKey(), v.getArgs(), v.getMessage()))
                .toList();

        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(INVALID_DATA)
                .messages(messages)
                .build();

        log.error(INVALID_DATA + ": " + detail);

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = resolve(CODE_INTERNAL_SERVER_ERROR, null, FALLBACK_INTERNAL_SERVER_ERROR);

        if (ex.getCause() instanceof ConstraintViolationException constraintEx) {
            DatabaseConstraintsEnum constraint = DatabaseConstraintsEnum
                    .getByKey(constraintEx.getConstraintName());
            String code = "db.constraint." + constraint.name().toLowerCase();
            message = resolve(code, null, constraint.getMessage());
        }

        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(INVALID_DATA)
                .messages(List.of(message))
                .detail(List.of(ex.getMessage()))
                .build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .messages(List.of(resolve(CODE_BAD_CREDENTIAL, null, FALLBACK_BAD_CREDENTIAL)))
                .detail(List.of(ex.getMessage()))
                .build();
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Unexpected internal error: ", ex);
        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(INTERNAL_SERVER_ERROR)
                .messages(List.of(resolve(CODE_INTERNAL_SERVER_ERROR, null, FALLBACK_INTERNAL_SERVER_ERROR)))
                .detail(List.of(UNEXPECTED_ERROR_DETAIL))
                .build();
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    /**
     * Resolves a message code using MessageSource with the request locale.
     * Falls back to the provided Portuguese fallback if MessageSource is unavailable
     * or the code is not found (e.g., entity-specific messages not yet translated).
     */
    private String resolve(String code, Object[] args, String fallback) {
        if (messageSource == null) {
            return fallback;
        }
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(code, args, locale);
        } catch (NoSuchMessageException e) {
            return fallback;
        }
    }
}
