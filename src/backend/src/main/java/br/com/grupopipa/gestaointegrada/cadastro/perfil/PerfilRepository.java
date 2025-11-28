package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilEntity;

@Repository
public interface PerfilRepository extends JpaRepository<PerfilEntity, UUID>, JpaSpecificationExecutor<PerfilEntity> {
}
