package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;

/** Interface customizada para consultas otimizadas de Titulo */
public interface TituloRepositoryCustom {

    /**
     * Busca títulos com projeção otimizada, calculando valorPago via SUM de
     * movimentações Suporta
     * filtros dinâmicos via Specifications
     *
     * @param spec     Specification para filtros dinâmicos (pode ser null)
     * @param pageable Paginação
     * @return Page de TituloProjection com valorPago calculado
     */
    Page<TituloProjection> findAllProjected(Specification<Titulo> spec, Pageable pageable);
}
