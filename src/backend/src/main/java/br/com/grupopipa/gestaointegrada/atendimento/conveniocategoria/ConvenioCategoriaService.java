package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria;

import java.util.List;
import java.util.UUID;

import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto.ConvenioCategoriaDTO;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto.ConvenioCategoriaGridDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface ConvenioCategoriaService
        extends CrudService<ConvenioCategoriaDTO, ConvenioCategoriaGridDTO> {

    List<ConvenioCategoriaGridDTO> listarPorConvenio(UUID convenioId);
}
