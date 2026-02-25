package br.com.grupopipa.gestaointegrada.core.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import br.com.grupopipa.gestaointegrada.core.dto.AuditInfoDTO;
import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import br.com.grupopipa.gestaointegrada.core.dto.FilterDTO;
import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

public interface CrudService<D extends DTO, G extends GridDTO> {

    D save(D dto);

    UUID delete(UUID id);

    PageDTO<G> list(FilterDTO filter, Pageable pageable);

    List<G> list(FilterDTO filter, Sort sort);

    D findById(UUID id);

    AuditInfoDTO getAuditInfo(UUID id);

    byte[] exportToCsv(PageRequest request);
}
