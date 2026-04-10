package br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio;

import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.dto.CodigoConvenioDTO;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;

public interface CodigoConvenioService {

    List<CodigoConvenioDTO> findAllByConvenioId(UUID convenioId);

    void syncForConvenio(Convenio convenio, List<CodigoConvenioDTO> codigos);
}
