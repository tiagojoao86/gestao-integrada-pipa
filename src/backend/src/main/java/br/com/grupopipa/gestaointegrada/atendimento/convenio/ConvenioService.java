package br.com.grupopipa.gestaointegrada.atendimento.convenio;

import java.util.List;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.dto.ConvenioDTO;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.dto.ConvenioGridDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface ConvenioService extends CrudService<ConvenioDTO, ConvenioGridDTO> {

    List<ConvenioGridDTO> listarAtivos();
}
