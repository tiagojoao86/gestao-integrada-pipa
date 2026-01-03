package br.com.grupopipa.gestaointegrada.cadastro.modulo;

import java.util.List;

import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface ModuloService extends CrudService<ModuloDTO, ModuloGridDTO> {
    List<ModuloDTO> findAllSimple();
}
