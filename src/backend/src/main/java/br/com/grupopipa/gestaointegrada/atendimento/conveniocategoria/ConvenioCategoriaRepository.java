package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;

public interface ConvenioCategoriaRepository
        extends JpaRepository<ConvenioCategoria, UUID>, JpaSpecificationExecutor<ConvenioCategoria> {

    @Query("SELECT c FROM ConvenioCategoria c JOIN FETCH c.convenio "
            + "WHERE c.convenio.id = :convenioId AND c.deleted = false ORDER BY c.nome ASC")
    List<ConvenioCategoria> findAllByConvenioIdAndDeletedFalseOrderByNomeAsc(
            @Param("convenioId") UUID convenioId);
}
