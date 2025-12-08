package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import br.com.grupopipa.gestaointegrada.core.service.CrudService;

import java.util.List;

public interface UnidadeNegocioService extends CrudService<UnidadeNegocioDTO, UnidadeNegocioGridDTO> {
    List<UnidadeNegocioDTO> listarAtivas();
}
