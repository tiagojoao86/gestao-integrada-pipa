package br.com.grupopipa.gestaointegrada.core.dao;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.entity.UnidadeNegocioFiltravel;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.Set;
import java.util.UUID;

/**
 * Specification para filtrar entidades por UnidadeNegocio.
 * Aplica filtro IN nas unidades de negócio permitidas ao usuário.
 */
public class UnidadeNegocioSpecification<T extends BaseEntity & UnidadeNegocioFiltravel>
        implements Specification<T> {

    private final Set<UUID> unidadesPermitidas;

    public UnidadeNegocioSpecification(Set<UUID> unidadesPermitidas) {
        this.unidadesPermitidas = unidadesPermitidas;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<T> root,
            @NonNull CriteriaQuery<?> query,
            @NonNull CriteriaBuilder cb) {

        // Se não há unidades no contexto, significa que é admin ou não há restrição
        if (unidadesPermitidas == null || unidadesPermitidas.isEmpty()) {
            return cb.conjunction(); // retorna true (não filtra)
        }

        // Aplica filtro: unidadeNegocio.id IN (unidadesPermitidas)
        return root.get("unidadeNegocio").get("id").in(unidadesPermitidas);
    }

    /**
     * Factory method para criar a specification.
     * 
     * @param unidadesPermitidas IDs das unidades permitidas
     * @return Specification configurada
     */
    public static <T extends BaseEntity & UnidadeNegocioFiltravel> Specification<T> create(
            Set<UUID> unidadesPermitidas) {
        return new UnidadeNegocioSpecification<>(unidadesPermitidas);
    }
}
