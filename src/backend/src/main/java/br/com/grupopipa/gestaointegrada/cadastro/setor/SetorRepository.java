package br.com.grupopipa.gestaointegrada.cadastro.setor;

import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SetorRepository extends JpaRepository<Setor, UUID>, JpaSpecificationExecutor<Setor> {
}
