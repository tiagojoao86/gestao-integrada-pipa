package br.com.grupopipa.gestaointegrada.core.entity;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;

/**
 * Interface marker para entidades que devem ser filtradas por UnidadeNegocio.
 * Entidades que implementam esta interface terão automaticamente aplicado
 * o filtro de unidades de negócio permitidas ao usuário nos métodos:
 * - list()
 * - findById()
 * - listarParaVinculo()
 */
public interface UnidadeNegocioFiltravel {
    /**
     * Retorna a unidade de negócio associada à entidade.
     * 
     * @return UnidadeNegocio da entidade
     */
    UnidadeNegocio getUnidadeNegocio();
}
