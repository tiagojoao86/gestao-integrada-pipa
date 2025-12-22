package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CategoriaTituloRepository - Testes de Integração")
@Transactional
class TituloCategoriaRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TituloCategoriaRepository repository;

    @Test
    @DisplayName("Deve salvar e recuperar categoria sem agrupador")
    void deveSalvarERecuperarCategoria() {
        TituloCategoria categoria = new TituloCategoria.Builder()
                .codigo("001")
                .nome("Categoria Repo")
                .descricao("Desc")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .build();

        TituloCategoria salvo = repository.save(categoria);
        repository.flush();

        Optional<TituloCategoria> encontrado = repository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("001", encontrado.get().getCodigo());
        assertEquals("Categoria Repo", encontrado.get().getNome().getValue());
        assertNull(encontrado.get().getAgrupador());
    }

    @Test
    @DisplayName("Deve salvar categoria com agrupador")
    void deveSalvarCategoriaComAgrupador() {
        // Criar agrupador (categoria pai)
        TituloCategoria agrupador = new TituloCategoria.Builder()
                .codigo("001")
                .nome("Despesas Operacionais")
                .descricao("Agrupador de despesas operacionais")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .build();
        TituloCategoria agrupadorSalvo = repository.save(agrupador);
        repository.flush();

        // Criar categoria filha
        TituloCategoria categoria = new TituloCategoria.Builder()
                .codigo("001.001")
                .nome("Material de Escritório")
                .descricao("Materiais para escritório")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .agrupador(agrupadorSalvo)
                .build();

        TituloCategoria salvo = repository.save(categoria);
        repository.flush();

        Optional<TituloCategoria> encontrado = repository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("001.001", encontrado.get().getCodigo());
        assertEquals("Material de Escritório", encontrado.get().getNome().getValue());
        assertNotNull(encontrado.get().getAgrupador());
        assertEquals("Despesas Operacionais", encontrado.get().getAgrupador().getNome().getValue());
        assertTrue(encontrado.get().temAgrupador());
        assertFalse(encontrado.get().isAgrupador());
    }

    @Test
    @DisplayName("Deve verificar se categoria é agrupador")
    void deveVerificarSeEhAgrupador() {
        TituloCategoria agrupador = new TituloCategoria.Builder()
                .codigo("002")
                .nome("Receitas de Serviços")
                .descricao("Agrupador de receitas")
                .tipo(TituloCategoriaTipoEnum.RECEITA)
                .build();

        TituloCategoria salvo = repository.save(agrupador);
        repository.flush();

        Optional<TituloCategoria> encontrado = repository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertTrue(encontrado.get().isAgrupador());
        assertFalse(encontrado.get().temAgrupador());
    }

    @Test
    @DisplayName("Deve deletar categoria")
    void deveDeletarCategoria() {
        TituloCategoria categoria = new TituloCategoria.Builder()
                .codigo("003")
                .nome("Categoria To Delete")
                .descricao("Desc")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .build();

        TituloCategoria salvo = repository.save(categoria);
        repository.flush();

        repository.deleteById(salvo.getId());
        repository.flush();

        Optional<TituloCategoria> encontrado = repository.findById(salvo.getId());
        assertFalse(encontrado.isPresent());
    }
}
