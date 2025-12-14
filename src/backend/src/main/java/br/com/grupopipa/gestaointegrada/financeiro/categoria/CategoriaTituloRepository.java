package br.com.grupopipa.gestaointegrada.financeiro.categoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.financeiro.entity.CategoriaTitulo;

import java.util.UUID;

public interface CategoriaTituloRepository
        extends JpaRepository<CategoriaTitulo, UUID>, JpaSpecificationExecutor<CategoriaTitulo> {
}
