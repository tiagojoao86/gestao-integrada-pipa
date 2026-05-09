package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;

public interface TituloCategoriaRepository
        extends JpaRepository<TituloCategoria, UUID>, JpaSpecificationExecutor<TituloCategoria> {

    Optional<TituloCategoria> findByCodigo(String codigo);

    Optional<TituloCategoria> findByPadraoTrue();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(
        "UPDATE TituloCategoria t SET t.padrao = false WHERE t.padrao = true AND t.id <> :excludeId")
    void removerPadraoExceto(@org.springframework.data.repository.query.Param("excludeId") UUID excludeId);
}
