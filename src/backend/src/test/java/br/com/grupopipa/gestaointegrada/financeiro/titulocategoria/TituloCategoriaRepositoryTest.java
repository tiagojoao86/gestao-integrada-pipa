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
    @DisplayName("Deve salvar e recuperar categoria")
    void deveSalvarERecuperarCategoria() {
        TituloCategoria categoria = new TituloCategoria.Builder()
                .nome("Categoria Repo")
                .descricao("Desc")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .build();

        TituloCategoria salvo = repository.save(categoria);
        repository.flush();

        Optional<TituloCategoria> encontrado = repository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("Categoria Repo", encontrado.get().getNome().getValue());
    }

    @Test
    @DisplayName("Deve deletar categoria")
    void deveDeletarCategoria() {
        TituloCategoria categoria = new TituloCategoria.Builder()
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
