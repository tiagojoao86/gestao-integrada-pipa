package br.com.grupopipa.gestaointegrada.atendimento.tabela;

import java.util.List;

import br.com.grupopipa.gestaointegrada.atendimento.tabela.dto.TabelaDTO;
import br.com.grupopipa.gestaointegrada.atendimento.tabela.dto.TabelaGridDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface TabelaService extends CrudService<TabelaDTO, TabelaGridDTO> {

    List<TabelaGridDTO> listarAtivas();
}
