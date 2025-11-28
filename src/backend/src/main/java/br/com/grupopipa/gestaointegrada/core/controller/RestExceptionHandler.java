package br.com.grupopipa.gestaointegrada.core.controller;

import br.com.grupopipa.gestaointegrada.core.dao.DatabaseConstraintsEnum;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String INVALID_DATA = "Invalid Data";
    private static final String RESOURCE_NOT_FOUND = "Resource not found";
    private static final String INTERNAL_SERVER_ERROR = "Internal server error";
    private static final String UNEXPECTED_ERROR_DETAIL = "An unexpected internal system error has occurred.";

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Object> handleAuthorizationDeniedException(AuthorizationDeniedException ex, WebRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String title = HttpStatus.FORBIDDEN.getReasonPhrase();
        String detail = ex.getMessage();
        List<String> userMessageKeys = List.of(ErrorKeys.NOT_AUTHORIZED);
        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(title)
                .userMessageKey(userMessageKeys)
                .detail(List.of(detail))
                .build();
        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntidadeNaoEncontrada(EntityNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        String title = RESOURCE_NOT_FOUND;
        String detail = ex.getMessage();

        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(title)
                .userMessageKey(List.of(ErrorKeys.RESOURCE_NOT_FOUND))
                .detail(List.of(detail))
                .build();

        log.error(title + ": " + detail);

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(BeanValidationException.class)
    public ResponseEntity<Object> handleBeanValidationException(BeanValidationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String title = INVALID_DATA;
        String detail = ex.getViolations().stream()
                .map(v -> String.format("'%s': %s", v.getKey(), v.getMessage()))
                .collect(Collectors.joining(",\n"));

        List<String> userMessageKey = ex.getViolations().stream()
                .map(v -> {
                    String key = v.getKey();                    
                    return StringUtils.hasText(ex.getEntityName())
                            ? ex.getEntityName() + "." + key
                            : key;
                })
                .toList();

        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(title)
                .userMessageKey(userMessageKey)
                .build();

        log.error(title + ": " + detail);

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String title = INVALID_DATA;
        String detail = ex.getMessage();
        List<String> userMessageKeys = List.of(ErrorKeys.INTERNAL_SERVER_ERROR);

        if (ex.getCause().getClass().getName().equals("org.hibernate.exception.ConstraintViolationException")) {
            userMessageKeys = List.of(
                    DatabaseConstraintsEnum.getByKey(
                        ((ConstraintViolationException) ex.getCause()).getConstraintName())
                        .getUserMessageKey()
                    );
        }

        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(title)
                .userMessageKey(userMessageKeys)
                .detail(List.of(detail))
                .build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String title = HttpStatus.UNAUTHORIZED.getReasonPhrase();
        String detail = ex.getMessage();
        List<String> userMessageKeys = List.of(ErrorKeys.BAD_CREDENTIAL);

        ApiError apiError = ApiError.builder()
                .status(status.value())
                .timestamp(OffsetDateTime.now())
                .title(title)
                .userMessageKey(userMessageKeys)
                .detail(List.of(detail))
                .build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String title = INTERNAL_SERVER_ERROR;
        String detail = UNEXPECTED_ERROR_DETAIL;

        log.error("Unexpected internal error: ", ex);

        ApiError apiError = ApiError.builder()
                .status(status.value()).timestamp(OffsetDateTime.now()).title(title)
                .userMessageKey(List.of(ErrorKeys.INTERNAL_SERVER_ERROR)).detail(List.of(detail)).build();

        return handleExceptionInternal(ex, apiError, new HttpHeaders(), status, request);
    }
}
