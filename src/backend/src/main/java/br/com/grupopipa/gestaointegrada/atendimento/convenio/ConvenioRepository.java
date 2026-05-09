package br.com.grupopipa.gestaointegrada.atendimento.convenio;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;

public interface ConvenioRepository
        extends JpaRepository<Convenio, UUID>, JpaSpecificationExecutor<Convenio> {

    List<Convenio> findAllByAtivoTrueAndDeletedFalseOrderByNomeAsc();
}
