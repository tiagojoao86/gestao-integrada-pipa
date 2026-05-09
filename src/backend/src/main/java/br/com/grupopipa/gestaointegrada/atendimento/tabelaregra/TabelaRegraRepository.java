package br.com.grupopipa.gestaointegrada.atendimento.tabelaregra;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.grupopipa.gestaointegrada.atendimento.tabelaregra.entity.TabelaRegra;

public interface TabelaRegraRepository
        extends JpaRepository<TabelaRegra, UUID>, JpaSpecificationExecutor<TabelaRegra> {

    @Query("SELECT r FROM TabelaRegra r "
        + "WHERE r.convenio.id = :convenioId "
        + "AND r.convenioCategoria.id = :categoriaId "
        + "AND (r.deleted IS NULL OR r.deleted = false)")
    Optional<TabelaRegra> findByConvenioAndCategoria(
        @Param("convenioId") UUID convenioId,
        @Param("categoriaId") UUID categoriaId);

    @Query("SELECT r FROM TabelaRegra r "
        + "WHERE r.convenio.id = :convenioId "
        + "AND r.convenioCategoria IS NULL "
        + "AND (r.deleted IS NULL OR r.deleted = false)")
    Optional<TabelaRegra> findByConvenioSemCategoria(@Param("convenioId") UUID convenioId);
}
