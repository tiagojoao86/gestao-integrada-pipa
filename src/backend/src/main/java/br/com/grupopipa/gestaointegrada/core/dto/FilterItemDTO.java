package br.com.grupopipa.gestaointegrada.core.dto;

import java.util.List;

import br.com.grupopipa.gestaointegrada.core.enums.FilterOperator;
import lombok.Getter;

@Getter
public class FilterItemDTO {
    private String property;
    private List<Object> values;
    private FilterOperator operator;
}
