package br.com.grupopipa.gestaointegrada.core.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Builder
public class ApiError {

    private Integer status;
    private OffsetDateTime timestamp;
    private String title;
    private List<String> detail;
    private List<String> userMessageKey;
}