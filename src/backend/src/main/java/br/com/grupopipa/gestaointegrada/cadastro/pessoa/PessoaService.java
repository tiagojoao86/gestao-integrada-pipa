package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import br.com.grupopipa.gestaointegrada.core.service.CrudService;

import java.util.List;

public interface PessoaService extends CrudService<PessoaDTO, PessoaGridDTO> {
    List<PessoaDTO> listarParaVinculo();
}
