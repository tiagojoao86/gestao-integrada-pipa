package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import br.com.grupopipa.gestaointegrada.core.service.CrudService;

import java.util.List;

public interface UnidadeNegocioService extends CrudService<UnidadeNegocioDTO, UnidadeNegocioGridDTO> {
    List<UnidadeNegocioDTO> listarAtivas();

    /**
     * Lista unidades de negócio ativas filtradas pelas unidades permitidas ao
     * usuário.
     * 
     * @return Lista de unidades que o usuário tem acesso
     */
    List<UnidadeNegocioDTO> listarDisponiveisParaUsuario();

    /**
     * Lista todas unidades de negócio ativas sem filtro (para uso administrativo).
     * 
     * @return Lista completa de unidades ativas
     */
    List<UnidadeNegocioDTO> listarTodasDisponiveis();
}
