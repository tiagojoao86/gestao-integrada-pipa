package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;

import java.util.UUID;

public interface TituloCategoriaRepository
        extends JpaRepository<TituloCategoria, UUID>, JpaSpecificationExecutor<TituloCategoria> {
}
