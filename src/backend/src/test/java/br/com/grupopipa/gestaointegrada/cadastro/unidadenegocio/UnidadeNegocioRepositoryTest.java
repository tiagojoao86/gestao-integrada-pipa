package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UnidadeNegocioRepository - Testes de Integração")
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class UnidadeNegocioRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UnidadeNegocioRepository repository;

    @Test
    @DisplayName("Deve salvar e recuperar UnidadeNegocio com sucesso")
    void deveSalvarERecuperarUnidadeNegocio() {
        // Given - Preparação
        UnidadeNegocio unidade = new UnidadeNegocio("UN001", "Unidade Teste", "Descrição de teste");

        // When - Ação
        UnidadeNegocio salva = repository.save(unidade);
        UnidadeNegocio recuperada = repository.findById(salva.getId()).orElse(null);

        // Then - Verificação
        assertNotNull(recuperada);
        assertEquals("UN001", recuperada.getCodigo());
        assertEquals("Unidade Teste", recuperada.getNome());
        assertEquals("Descrição de teste", recuperada.getDescricao());
        assertTrue(recuperada.isAtiva());
    }

    @Test
    @DisplayName("Deve validar constraint de código único")
    void deveValidarCodigoUnico() {
        // Given - Este teste DEVE gerar SQL Error 23505 (constraint violation)
        UnidadeNegocio unidade1 = new UnidadeNegocio("UN002", "Unidade 1");
        repository.save(unidade1);

        UnidadeNegocio unidade2 = new UnidadeNegocio("UN002", "Unidade 2"); // Mesmo código

        // When/Then - Deve lançar exceção por violação de unique constraint
        assertThrows(Exception.class, () -> {
            repository.save(unidade2);
            repository.flush();
        });
    }

    @Test
    @DisplayName("Deve filtrar unidades ativas e inativas corretamente")
    void deveFiltrarUnidadesAtivasEInativas() {
        // Given
        UnidadeNegocio ativa = new UnidadeNegocio("UN003", "Unidade Ativa");
        repository.save(ativa);

        UnidadeNegocio inativa = new UnidadeNegocio("UN004", "Unidade Inativa");
        inativa.inativar();
        repository.save(inativa);

        // When
        var todasUnidades = repository.findAll();
        var apenasAtivas = todasUnidades.stream()
            .filter(UnidadeNegocio::isAtiva)
            .toList();

        // Then
        assertTrue(todasUnidades.size() >= 2);
        assertTrue(apenasAtivas.stream().anyMatch(u -> u.getCodigo().equals("UN003")));
        assertFalse(apenasAtivas.stream().anyMatch(u -> u.getCodigo().equals("UN004")));
    }

    @Test
    @DisplayName("Deve atualizar UnidadeNegocio preservando o ID")
    void deveAtualizarUnidadeNegocio() {
        // Given
        UnidadeNegocio unidade = new UnidadeNegocio("UN005", "Nome Original");
        UnidadeNegocio salva = repository.save(unidade);

        // When
        salva.atualizar("Nome Atualizado", "Nova descrição");
        UnidadeNegocio atualizada = repository.save(salva);

        // Then
        assertEquals(salva.getId(), atualizada.getId());
        assertEquals("Nome Atualizado", atualizada.getNome());
        assertEquals("Nova descrição", atualizada.getDescricao());
        assertEquals("UN005", atualizada.getCodigo()); // Código não muda
    }

    @Test
    @DisplayName("Deve deletar UnidadeNegocio corretamente")
    void deveDeletarUnidadeNegocio() {
        // Given
        UnidadeNegocio unidade = new UnidadeNegocio("UN006", "Unidade para Deletar");
        UnidadeNegocio salva = repository.save(unidade);

        // When
        repository.delete(salva);
        var encontrada = repository.findById(salva.getId());

        // Then
        assertTrue(encontrada.isEmpty());
    }
}
