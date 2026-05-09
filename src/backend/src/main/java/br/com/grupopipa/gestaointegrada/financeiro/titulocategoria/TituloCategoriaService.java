package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import java.util.UUID;

import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface TituloCategoriaService
        extends CrudService<TituloCategoriaDTO, TituloCategoriaGridDTO> {

    TituloCategoriaDTO definirPadrao(UUID id);

    TituloCategoriaDTO findPadrao();
}
