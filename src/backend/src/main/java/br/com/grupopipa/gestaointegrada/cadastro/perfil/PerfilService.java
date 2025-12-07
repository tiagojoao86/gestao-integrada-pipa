package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import java.util.List;

import br.com.grupopipa.gestaointegrada.core.service.CrudService;

public interface PerfilService extends CrudService<PerfilDTO, PerfilGridDTO> {

    /**
     * Retorna lista simplificada de perfis para vinculação (autocomplete).
     * Sem paginação, apenas perfis ativos.
     */
    List<PerfilParaVinculoDTO> listarParaVinculo();
}
