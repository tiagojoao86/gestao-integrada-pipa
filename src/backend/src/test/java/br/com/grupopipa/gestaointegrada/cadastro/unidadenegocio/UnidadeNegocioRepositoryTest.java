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
        UnidadeNegocio unidade = new UnidadeNegocio.Builder()
                .codigo("UN001")
                .nome("Unidade Teste")
                .descricao("Descrição de teste")
                .cnpj("11.222.333/0001-81")
                .build();

        // When - Ação
        UnidadeNegocio salva = repository.save(unidade);
        UnidadeNegocio recuperada = repository.findById(salva.getId()).orElse(null);

        // Then - Verificação
        assertNotNull(recuperada);
        assertEquals("UN001", recuperada.getCodigo());
        assertEquals("Unidade Teste", recuperada.getNome());
        assertEquals("Descrição de teste", recuperada.getDescricao());
        assertEquals("11222333000181", recuperada.getCnpj());
        assertTrue(recuperada.isAtiva());
    }

    @Test
    @DisplayName("Deve validar constraint de código único")
    void deveValidarCodigoUnico() {
        // Given - Este teste DEVE gerar SQL Error 23505 (constraint violation)
        UnidadeNegocio unidade1 = new UnidadeNegocio.Builder()
                .codigo("UN002")
                .nome("Unidade 1")
                .build();
        repository.save(unidade1);

        UnidadeNegocio unidade2 = new UnidadeNegocio.Builder()
                .codigo("UN002")
                .nome("Unidade 2")
                .build(); // Mesmo código

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
        UnidadeNegocio ativa = new UnidadeNegocio.Builder()
                .codigo("UN003")
                .nome("Unidade Ativa")
                .cnpj("09.373.731/0001-57")
                .build();
        repository.save(ativa);

        UnidadeNegocio inativa = new UnidadeNegocio.Builder()
                .codigo("UN004")
                .nome("Unidade Inativa")
                .cnpj("65.389.347/0001-13")
                .build();
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
        UnidadeNegocio unidade = new UnidadeNegocio.Builder()
                .codigo("UN005")
                .nome("Nome Original")
                .cnpj("11.222.333/0001-81")
                .build();
        UnidadeNegocio salva = repository.save(unidade);

        // When
        salva.atualizar("Nome Atualizado", "Nova descrição", null);
        UnidadeNegocio atualizada = repository.save(salva);

        // Then
        assertEquals(salva.getId(), atualizada.getId());
        assertEquals("Nome Atualizado", atualizada.getNome());
        assertEquals("Nova descrição", atualizada.getDescricao());
        assertEquals("UN005", atualizada.getCodigo()); // Código não muda
        assertNull(atualizada.getCnpj()); // CNPJ foi removido na atualização
    }

    @Test
    @DisplayName("Deve deletar UnidadeNegocio corretamente")
    void deveDeletarUnidadeNegocio() {
        // Given
        UnidadeNegocio unidade = new UnidadeNegocio.Builder()
                .codigo("UN006")
                .nome("Unidade para Deletar")
                .build();
        UnidadeNegocio salva = repository.save(unidade);

        // When
        repository.delete(salva);
        var encontrada = repository.findById(salva.getId());

        // Then
        assertTrue(encontrada.isEmpty());
    }

    @Test
    @DisplayName("Deve salvar UnidadeNegocio sem CNPJ (campo opcional)")
    void deveSalvarUnidadeNegocioSemCNPJ() {
        // Given
        UnidadeNegocio unidade = new UnidadeNegocio.Builder()
                .codigo("UN007")
                .nome("Unidade Sem CNPJ")
                .descricao("CNPJ é opcional")
                .build();

        // When
        UnidadeNegocio salva = repository.save(unidade);
        UnidadeNegocio recuperada = repository.findById(salva.getId()).orElse(null);

        // Then
        assertNotNull(recuperada);
        assertEquals("UN007", recuperada.getCodigo());
        assertNull(recuperada.getCnpj());
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ inválido ao criar UnidadeNegocio")
    void deveRejeitarCNPJInvalido() {
        // Given/When/Then
        assertThrows(Exception.class, () -> {
            new UnidadeNegocio.Builder()
                    .codigo("UN008")
                    .nome("Unidade com CNPJ Inválido")
                    .cnpj("00.000.000/0000-00") // CNPJ inválido
                    .build();
        });
    }

    @Test
    @DisplayName("Deve atualizar CNPJ de uma UnidadeNegocio existente")
    void deveAtualizarCNPJDeUnidadeNegocio() {
        // Given
        UnidadeNegocio unidade = new UnidadeNegocio.Builder()
                .codigo("UN009")
                .nome("Unidade Para Atualizar CNPJ")
                .cnpj("11.222.333/0001-81")
                .build();
        UnidadeNegocio salva = repository.save(unidade);

        // When
        salva.atualizar("Unidade Para Atualizar CNPJ", "Descrição",
                "34.028.316/0001-03");
        UnidadeNegocio atualizada = repository.save(salva);

        // Then
        assertEquals("34028316000103", atualizada.getCnpj());
    }
}
