package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TituloRepository extends JpaRepository<Titulo, UUID>, JpaSpecificationExecutor<Titulo>, TituloRepositoryCustom {
}
