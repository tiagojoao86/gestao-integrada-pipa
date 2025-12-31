package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;

@Repository
public interface UnidadeNegocioRepository
    extends JpaRepository<UnidadeNegocio, UUID>, JpaSpecificationExecutor<UnidadeNegocio> {
  List<UnidadeNegocio> findByAtivaTrue();
}
