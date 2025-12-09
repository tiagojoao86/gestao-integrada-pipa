package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

import java.util.List;

public interface PlanoContasService extends CrudService<PlanoContasDTO, PlanoContasGridDTO> {
    List<UnidadeNegocioDTO> listarUnidadesDisponiveis();
}
