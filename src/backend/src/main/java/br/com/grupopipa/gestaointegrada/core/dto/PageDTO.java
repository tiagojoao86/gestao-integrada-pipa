package br.com.grupopipa.gestaointegrada.core.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;

    public PageDTO(List<T> content, org.springframework.data.domain.Pageable pageable, long totalElements) {
        this.content = content;
        this.pageNumber = pageable.getPageNumber();
        this.pageSize = pageable.getPageSize();
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
    }
}
