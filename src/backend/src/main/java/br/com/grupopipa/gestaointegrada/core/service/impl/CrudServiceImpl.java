package br.com.grupopipa.gestaointegrada.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import br.com.grupopipa.gestaointegrada.core.dto.FilterDTO;
import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.enums.FilterLogicOperator;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;
import jakarta.transaction.Transactional;

@Service
public abstract class CrudServiceImpl<D extends DTO, G extends GridDTO, T extends BaseEntity, R extends JpaRepository<T, UUID>>
        implements CrudService<D, G> {
    
    protected R repository;    
    private Specifications<T> specifications;

    public CrudServiceImpl(R repository, Specifications<T> specifications) {
        this.repository = repository;
        this.specifications = specifications;
    }

    public D save(D dto) {
        if (Objects.nonNull(dto.getId())) {
            T entity = this.findEntityById(dto.getId());

            if (Objects.nonNull(entity)) {
                T mergedEntity = this.mergeEntityAndDTO(entity, dto);
                T newEntity = repository.save(mergedEntity);
                D newDTO = this.buildDTOFromEntity(newEntity);
                return newDTO;
            }
        }
        return this.buildDTOFromEntity(repository.save(this.mergeEntityAndDTO(null, dto)));
    }

    public UUID delete(UUID id) {
        repository.deleteById(id);

        return id;
    }

    @SuppressWarnings("unchecked")
    public PageDTO<G> list(FilterDTO filter, Pageable pageable) {
        Specification<T> specification = this.buildSpecification(filter);

        if (this.repository instanceof JpaSpecificationExecutor) {
            Page<T> page = ((JpaSpecificationExecutor<T>) this.repository).findAll(specification, pageable);
            return new PageDTO<G>(
                    page.getContent().stream().map(this::buildGridDTOFromEntity).toList(),
                    page.getPageable(),
                    page.getTotalElements());
        }

        return null;

    }

    @Transactional
    public D findById(UUID id) {
        return buildDTOFromEntity(this.findEntityById(id));
    }

    public T findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityClass().getSimpleName(), id));
    }

    protected Specification<T> buildSpecification(FilterDTO filter) {
        if (!ObjectUtils.isEmpty(filter)) {
            if (FilterLogicOperator.AND.equals(filter.getFilterLogicOperator())) {
                return Specification.allOf(listSpecifications(filter));
            }

            return Specification.anyOf(listSpecifications(filter));
        }

        return null;
    }

    private List<Specification<T>> listSpecifications(FilterDTO filter) {
        List<Specification<T>> list = new ArrayList<>();
        for (String property : getPropertiesToFilter()) {
            Specification<T> newSpecification = specifications.withItem(filter.getItemByPropertyName(property),
                    getEntityClass());

            if (Objects.nonNull(newSpecification)) {
                list.add(newSpecification);
            }
        }

        return list;
    }

    protected abstract T mergeEntityAndDTO(T entity, D dto);

    protected abstract D buildDTOFromEntity(T entity);

    protected abstract G buildGridDTOFromEntity(T entity);

    protected abstract List<String> getPropertiesToFilter();

    protected abstract Class<T> getEntityClass();

}
