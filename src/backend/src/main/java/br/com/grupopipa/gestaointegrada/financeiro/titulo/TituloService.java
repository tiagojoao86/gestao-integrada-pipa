package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

import java.util.List;

public interface TituloService extends CrudService<TituloDTO, TituloGridDTO> {
    List<UnidadeNegocioDTO> listarUnidadesDisponiveis();
}
