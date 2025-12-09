package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

import java.util.List;

public interface ContaBancariaService extends CrudService<ContaBancariaDTO, ContaBancariaGridDTO> {
    List<UnidadeNegocioDTO> listarUnidadesDisponiveis();
}
