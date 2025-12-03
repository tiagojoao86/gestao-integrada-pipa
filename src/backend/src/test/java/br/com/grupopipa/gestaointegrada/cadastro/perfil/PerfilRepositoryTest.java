package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import br.com.grupopipa.gestaointegrada.cadastro.perfil.entity.PerfilEntity;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para PerfilRepository.
 * Valida a persistência e consultas de perfis de usuário.
 */
@DisplayName("PerfilRepository - Testes de Integração")
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PerfilRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PerfilRepository repository;

    @Test
    @DisplayName("Deve salvar e recuperar perfil")
    void deveSalvarERecuperarPerfil() {
        // Given
        PerfilEntity perfil = new PerfilEntity.Builder()
            .nome("Administrador")
            .build();

        // When
        PerfilEntity perfilSalvo = repository.save(perfil);

        // Then
        assertNotNull(perfilSalvo.getId());
        assertEquals("Administrador", perfilSalvo.getNome());
        assertNotNull(perfilSalvo.getCreatedAt());
    }

    @Test
    @DisplayName("Deve buscar perfil por ID")
    void deveBuscarPerfilPorId() {
        // Given
        PerfilEntity perfil = new PerfilEntity.Builder()
            .nome("Operador")
            .build();
        PerfilEntity perfilSalvo = repository.save(perfil);

        // When
        Optional<PerfilEntity> resultado = repository.findById(perfilSalvo.getId());

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Operador", resultado.get().getNome());
        assertEquals(perfilSalvo.getId(), resultado.get().getId());
    }

    @Test
    @DisplayName("Deve deletar perfil")
    void deveDeletarPerfil() {
        // Given
        PerfilEntity perfil = new PerfilEntity.Builder()
            .nome("Temporário")
            .build();
        PerfilEntity perfilSalvo = repository.save(perfil);

        // When
        repository.delete(perfilSalvo);
        Optional<PerfilEntity> resultado = repository.findById(perfilSalvo.getId());

        // Then
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios")
    void deveValidarCamposObrigatorios() {
        // Given
        PerfilEntity perfil = new PerfilEntity.Builder()
            .nome("Analista")
            .build();

        // When
        PerfilEntity perfilSalvo = repository.save(perfil);

        // Then
        assertNotNull(perfilSalvo.getId());
        assertNotNull(perfilSalvo.getNome());
        assertNotNull(perfilSalvo.getCreatedAt());
    }
}
