package br.com.grupopipa.gestaointegrada.core.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PageRequest {
    private FilterDTO filter;
    private Integer page;
    private Integer size;
    private List<OrderDTO> order;
}
