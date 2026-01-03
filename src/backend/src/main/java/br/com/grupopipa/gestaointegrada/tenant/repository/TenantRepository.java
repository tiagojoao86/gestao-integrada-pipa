package br.com.grupopipa.gestaointegrada.tenant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.tenant.entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByTenantId(String tenantId);

    Optional<Tenant> findBySchemaName(String schemaName);

    boolean existsByTenantId(String tenantId);

    boolean existsBySchemaName(String schemaName);
}
