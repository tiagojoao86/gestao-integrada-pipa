package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;

import java.util.List;
import java.util.UUID;

public interface PlanoContasService extends CrudService<PlanoContasDTO, PlanoContasGridDTO> {
    List<UnidadeNegocioDTO> listarUnidadesDisponiveis();

    List<PlanoContasDTO> listarPlanosParaVinculo(UUID unidadeNegocioId);
}
