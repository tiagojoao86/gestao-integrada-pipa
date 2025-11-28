package br.com.grupopipa.gestaointegrada.cadastro.modulo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.cadastro.modulo.entity.ModuloEntity;

public interface ModuloRepository extends JpaRepository<ModuloEntity, UUID>, JpaSpecificationExecutor<ModuloEntity> {
}