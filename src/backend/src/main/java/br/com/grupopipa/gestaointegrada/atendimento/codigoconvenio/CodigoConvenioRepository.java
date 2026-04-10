package br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.entity.CodigoConvenio;

public interface CodigoConvenioRepository extends JpaRepository<CodigoConvenio, UUID> {

    List<CodigoConvenio> findAllByConvenioId(UUID convenioId);

    void deleteAllByConvenioId(UUID convenioId);
}
