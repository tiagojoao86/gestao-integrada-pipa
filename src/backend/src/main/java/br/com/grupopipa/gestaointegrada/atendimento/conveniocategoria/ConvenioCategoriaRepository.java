package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;

public interface ConvenioCategoriaRepository
        extends JpaRepository<ConvenioCategoria, UUID>, JpaSpecificationExecutor<ConvenioCategoria> {
}
