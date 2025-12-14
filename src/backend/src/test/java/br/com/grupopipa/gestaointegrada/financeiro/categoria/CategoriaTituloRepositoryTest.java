package br.com.grupopipa.gestaointegrada.financeiro.categoria;

import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CategoriaTitulo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CategoriaTituloRepository - Testes de Integração")
@Transactional
class CategoriaTituloRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CategoriaTituloRepository repository;

    @Test
    @DisplayName("Deve salvar e recuperar categoria")
    void deveSalvarERecuperarCategoria() {
        CategoriaTitulo categoria = new CategoriaTitulo.Builder()
                .nome("Categoria Repo")
                .descricao("Desc")
                .build();

        CategoriaTitulo salvo = repository.save(categoria);
        repository.flush();

        Optional<CategoriaTitulo> encontrado = repository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("Categoria Repo", encontrado.get().getNome());
    }

    @Test
    @DisplayName("Deve deletar categoria")
    void deveDeletarCategoria() {
        CategoriaTitulo categoria = new CategoriaTitulo.Builder()
                .nome("Categoria To Delete")
                .descricao("Desc")
                .build();

        CategoriaTitulo salvo = repository.save(categoria);
        repository.flush();

        repository.deleteById(salvo.getId());
        repository.flush();

        Optional<CategoriaTitulo> encontrado = repository.findById(salvo.getId());
        assertFalse(encontrado.isPresent());
    }
}
