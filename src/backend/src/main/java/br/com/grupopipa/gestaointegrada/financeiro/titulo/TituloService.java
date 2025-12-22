package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaDTO;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioDTO;
import br.com.grupopipa.gestaointegrada.core.service.CrudService;
import br.com.grupopipa.gestaointegrada.financeiro.planocontas.PlanoContasDTO;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaDTO;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public interface TituloService extends CrudService<TituloDTO, TituloGridDTO> {
    List<UnidadeNegocioDTO> listarUnidadesDisponiveis();

    List<PessoaDTO> listarPessoasDisponiveis();

    List<PlanoContasDTO> listarPlanosDisponiveis(UUID unidadeNegocioId);

    List<TituloCategoriaDTO> listarCategoriasDisponiveis();

    List<TituloDTO> searchByQuery(String q, int size);
}
