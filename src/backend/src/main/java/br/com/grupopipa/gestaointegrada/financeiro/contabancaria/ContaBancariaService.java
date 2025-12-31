package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import java.util.List;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface ContaBancariaService extends CrudService<ContaBancariaDTO, ContaBancariaGridDTO> {
  List<UnidadeNegocioDTO> listarUnidadesDisponiveis();
}
