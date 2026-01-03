package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import java.util.List;

import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface PessoaService extends CrudService<PessoaDTO, PessoaGridDTO> {
    List<PessoaDTO> listarParaVinculo();
}
