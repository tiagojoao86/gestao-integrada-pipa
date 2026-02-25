package br.com.grupopipa.gestaointegrada.core.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import br.com.grupopipa.gestaointegrada.core.Session;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.dao.UnidadeNegocioSpecification;
import br.com.grupopipa.gestaointegrada.core.dto.AuditInfoDTO;
import br.com.grupopipa.gestaointegrada.core.dto.DTO;
import br.com.grupopipa.gestaointegrada.core.dto.FilterDTO;
import br.com.grupopipa.gestaointegrada.core.dto.GridDTO;
import br.com.grupopipa.gestaointegrada.core.dto.OrderDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.entity.UnidadeNegocioFiltravel;
import br.com.grupopipa.gestaointegrada.core.enums.FilterLogicOperator;
import br.com.grupopipa.gestaointegrada.core.exception.DeletedEntityException;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;
import jakarta.transaction.Transactional;

@Service
public abstract class CrudServiceImpl<D extends DTO, G extends GridDTO, T extends BaseEntity,
        R extends JpaRepository<T, UUID>> implements CrudService<D, G> {

    protected R repository;
    private Specifications<T> specifications;

    public CrudServiceImpl(R repository, Specifications<T> specifications) {
        this.repository = repository;
        this.specifications = specifications;
    }

    @Override
    @Transactional
    public D save(D dto) {
        if (Objects.nonNull(dto.getId())) {
            T entity = this.findEntityById(dto.getId());

            if (Objects.nonNull(entity)) {
                // Validar se a entidade não está excluída
                if (Boolean.TRUE.equals(entity.getDeleted())) {
                    throw new DeletedEntityException(getEntityClass().getSimpleName(), entity.getId());
                }

                T mergedEntity = this.mergeEntityAndDTO(entity, dto);
                T newEntity = repository.save(mergedEntity);
                D newDTO = this.buildDTOFromEntity(newEntity);
                return newDTO;
            }
        }
        return this.buildDTOFromEntity(repository.save(this.mergeEntityAndDTO(null, dto)));
    }

    @Transactional
    public UUID delete(UUID id) {
        T entity = this.findEntityById(id);

        // Realizar soft delete
        String username = Session.getUsuarioUsername();
        entity.markAsDeleted(username);

        // Salvar a entidade
        // O CustomAuditingEntityListener verifica se deleted=true e preserva
        // updatedBy/updatedAt
        repository.save(entity);

        return id;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public PageDTO<G> list(FilterDTO filter, Pageable pageable) {
        Specification<T> specification = this.buildSpecification(filter);

        // Adicionar filtro de soft delete
        specification = addSoftDeleteFilter(specification, filter);

        // Adicionar automaticamente filtro de UnidadeNegocio se aplicável
        specification = addUnidadeNegocioFilterIfApplicable(specification);

        if (this.repository instanceof JpaSpecificationExecutor) {
            Page<T> page = ((JpaSpecificationExecutor<T>) this.repository).findAll(specification, pageable);
            return new PageDTO<G>(
                    page.getContent().stream().map(this::buildGridDTOFromEntity).toList(),
                    page.getPageable(),
                    page.getTotalElements());
        }

        return null;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public List<G> list(FilterDTO filter, Sort sort) {
        Specification<T> specification = this.buildSpecification(filter);

        // Adicionar filtro de soft delete
        specification = addSoftDeleteFilter(specification, filter);

        // Adicionar automaticamente filtro de UnidadeNegocio se aplicável
        specification = addUnidadeNegocioFilterIfApplicable(specification);

        if (this.repository instanceof JpaSpecificationExecutor) {
            return ((JpaSpecificationExecutor<T>) this.repository)
                    .findAll(specification, sort).stream().map(this::buildGridDTOFromEntity).toList();
        }

        return null;
    }

    /**
     * Adiciona filtro de soft delete baseado no flag showDeleted. Se showDeleted
     * for false (padrão),
     * filtra registros não excluídos (deleted = false). Se showDeleted for true,
     * mostra todos os
     * registros (inclusive excluídos).
     *
     * @param specification Specification existente
     * @param filter        FilterDTO contendo o flag showDeleted
     * @return Specification com filtro de soft delete aplicado
     */
    protected Specification<T> addSoftDeleteFilter(Specification<T> specification, FilterDTO filter) {
        // Se showDeleted não estiver setado ou for false, filtrar apenas não excluídos
        if (filter == null || !filter.isShowDeleted()) {
            Specification<T> notDeletedSpec = (root, query, criteriaBuilder) -> criteriaBuilder
                    .isFalse(root.get("deleted"));

            specification = specification != null ? specification.and(notDeletedSpec) : notDeletedSpec;
        }
        // Se showDeleted for true, não adiciona filtro (mostra todos os registros)

        return specification;
    }

    @SuppressWarnings("unchecked")
    protected Specification<T> addUnidadeNegocioFilterIfApplicable(Specification<T> specification) {
        if (UnidadeNegocioFiltravel.class.isAssignableFrom(getEntityClass())) {
            Set<UUID> unidadesPermitidas = Session.getUnidadeNegocioIds();
            Specification<T> unidadeSpec = (Specification<T>) UnidadeNegocioSpecification.create(unidadesPermitidas);
            specification = specification != null ? specification.and(unidadeSpec) : unidadeSpec;
        }
        return specification;
    }

    @Override
    @Transactional
    public D findById(UUID id) {
        T entity = this.findEntityById(id);

        // Validar acesso à UnidadeNegocio
        if (entity instanceof UnidadeNegocioFiltravel) {
            UnidadeNegocioFiltravel filtravel = (UnidadeNegocioFiltravel) entity;
            UUID unidadeId = filtravel.getUnidadeNegocio() != null ? filtravel.getUnidadeNegocio().getId() : null;

            Set<UUID> unidadesPermitidas = Session.getUnidadeNegocioIds();

            if (unidadeId != null
                    && !unidadesPermitidas.isEmpty()
                    && !unidadesPermitidas.contains(unidadeId)) {
                throw new SecurityException(
                        "Acesso negado: usuário não tem permissão para esta unidade de negócio");
            }
        }

        return buildDTOFromEntity(entity);
    }

    public T findEntityById(UUID id) {
        return repository
                .findById(id)
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

    @Override
    @Transactional
    public byte[] exportToCsv(PageRequest request) {
        String[] headers = getCsvHeaders();
        if (headers.length == 0) {
            return "\uFEFF".getBytes(StandardCharsets.UTF_8);
        }
        Sort sort = Sort.by(
                request.getOrder() != null
                        ? request.getOrder().stream().map(OrderDTO::getOrder).toList()
                        : List.of());
        List<G> records = list(request.getFilter(), sort);

        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append(buildCsvLine(headers));
        for (G record : records) {
            sb.append(buildCsvLine(buildCsvRow(record)));
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    protected String[] getCsvHeaders() {
        return new String[0];
    }

    protected String[] buildCsvRow(G gridDto) {
        return new String[0];
    }

    private String buildCsvLine(String[] values) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(';');
            }
            line.append(escapeCsvValue(values[i]));
        }
        return line.append('\n').toString();
    }

    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    protected abstract T mergeEntityAndDTO(T entity, D dto);

    protected abstract D buildDTOFromEntity(T entity);

    protected abstract G buildGridDTOFromEntity(T entity);

    protected abstract List<String> getPropertiesToFilter();

    protected abstract Class<T> getEntityClass();

    /**
     * Método auxiliar para obter filtro de UnidadeNegocio. Usado em métodos como
     * listarParaVinculo()
     * que retornam List.
     *
     * @return Specification de UnidadeNegocio ou null se não aplicável
     */
    @SuppressWarnings("unchecked")
    protected Specification<T> getUnidadeNegocioFilter() {
        if (UnidadeNegocioFiltravel.class.isAssignableFrom(getEntityClass())) {
            Set<UUID> unidadesPermitidas = Session.getUnidadeNegocioIds();
            return (Specification<T>) UnidadeNegocioSpecification.create(unidadesPermitidas);
        }
        return null;
    }

    @Override
    public AuditInfoDTO getAuditInfo(UUID id) {
        T entity = this.findEntityById(id);

        return AuditInfoDTO.builder()
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .deletedBy(entity.getDeletedBy())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
