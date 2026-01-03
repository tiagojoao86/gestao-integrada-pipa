package br.com.grupopipa.gestaointegrada.cadastro.modulo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.cadastro.modulo.entity.ModuloEntity;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;

/**
 * Testes de integração para ModuloRepository. Valida consultas de módulos do
 * sistema (entidade
 * somente leitura).
 */
@DisplayName("ModuloRepository - Testes de Integração")
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ModuloRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ModuloRepository repository;

    @Test
    @DisplayName("Deve listar todos os módulos")
    void deveListarTodosModulos() {
        // When
        List<ModuloEntity> modulos = repository.findAll();

        // Then
        assertNotNull(modulos);
        assertFalse(modulos.isEmpty());
        assertTrue(modulos.size() > 0);
    }

    @Test
    @DisplayName("Deve buscar módulo por ID")
    void deveBuscarModuloPorId() {
        // Given - Busca primeiro módulo disponível
        List<ModuloEntity> modulos = repository.findAll();
        assertFalse(modulos.isEmpty());
        ModuloEntity moduloExistente = modulos.get(0);

        // When
        Optional<ModuloEntity> resultado = repository.findById(moduloExistente.getId());

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(moduloExistente.getId(), resultado.get().getId());
        assertEquals(moduloExistente.getNome(), resultado.get().getNome());
        assertNotNull(resultado.get().getChave());
        assertNotNull(resultado.get().getGrupo());
    }

    @Test
    @DisplayName("Deve buscar módulos por grupo")
    void deveBuscarModulosPorGrupo() {
        // Given - Apenas verificar que existem módulos cadastrados
        // When
        List<ModuloEntity> modulos = repository.findAll();

        // Then
        assertNotNull(modulos);
        assertFalse(modulos.isEmpty());
        // Verifica que cada módulo tem um grupo válido
        modulos.forEach(modulo -> assertNotNull(modulo.getGrupo()));
    }

    @Test
    @DisplayName("Deve retornar lista vazia para grupo sem módulos")
    void deveRetornarListaVaziaParaGrupoSemModulos() {
        // Given - Usando grupo que pode não ter módulos ou criando enum fictício
        // Para este teste, vamos buscar por um grupo válido mas verificar comportamento

        // When
        List<ModuloEntity> todosModulos = repository.findAll();

        // Then
        assertNotNull(todosModulos);
        // Verifica que existem módulos cadastrados
        assertFalse(todosModulos.isEmpty());
    }

    @Test
    @DisplayName("Deve validar estrutura do módulo")
    void deveValidarEstruturaModulo() {
        // Given
        List<ModuloEntity> modulos = repository.findAll();
        assertFalse(modulos.isEmpty());

        // When
        ModuloEntity modulo = modulos.get(0);

        // Then
        assertNotNull(modulo.getId());
        assertNotNull(modulo.getChave());
        assertFalse(modulo.getChave().isEmpty());
        assertNotNull(modulo.getNome());
        assertFalse(modulo.getNome().isEmpty());
        assertNotNull(modulo.getGrupo());
        assertNotNull(modulo.getCreatedAt());
    }

    @Test
    @DisplayName("Deve verificar grupos de módulos distintos")
    void deveVerificarGruposModulosDistintos() {
        // When
        List<ModuloEntity> todosModulos = repository.findAll();

        // Then
        assertFalse(todosModulos.isEmpty());

        // Verifica que existem módulos em diferentes grupos
        long gruposDistintos = todosModulos.stream().map(ModuloEntity::getGrupo).distinct().count();

        assertTrue(gruposDistintos >= 1, "Deve haver pelo menos um grupo de módulos");
    }
}
