package br.com.grupopipa.gestaointegrada.cadastro.setor;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;

@Repository
public interface SetorRepository
        extends JpaRepository<Setor, UUID>, JpaSpecificationExecutor<Setor> {
}
