package br.com.grupopipa.gestaointegrada.core.controller;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class ApiError {

    private Integer status;
    private OffsetDateTime timestamp;
    private String title;
    private List<String> detail;
    private List<String> messages;
    /** @deprecated Use {@link #messages} instead. Will be removed in a future version. */
    @Deprecated
    private List<String> userMessageKey;
}
