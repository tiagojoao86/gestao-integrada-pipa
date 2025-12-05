package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para PlanoContasRepository.
 * Valida a persistência e consultas do plano de contas.
 */
@DisplayName("PlanoContasRepository - Testes de Integração")
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PlanoContasRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PlanoContasRepository repository;

    @Test
    @DisplayName("Deve salvar e recuperar plano de contas raiz")
    void deveSalvarERecuperarPlanoContasRaiz() {
        // Given
        PlanoContas planoReceitas = new PlanoContas.Builder()
                .codigo("1")
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA)
                .build();

        // When
        PlanoContas planoSalvo = repository.save(planoReceitas);

        // Then
        assertNotNull(planoSalvo.getId());
        assertEquals("1", planoSalvo.getCodigo());
        assertEquals("Receitas", planoSalvo.getDescricao());
        assertEquals(TipoPlanoContas.RECEITA, planoSalvo.getTipo());
        assertNull(planoSalvo.getPlanoPai());
        assertTrue(planoSalvo.getAtivo());
        assertNotNull(planoSalvo.getCreatedAt());
    }

    @Test
    @DisplayName("Deve salvar e recuperar plano de contas com pai")
    void deveSalvarERecuperarPlanoContasComPai() {
        // Given
        PlanoContas planoPai = new PlanoContas.Builder()
                .codigo("1")
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA)
                .build();
        planoPai = repository.save(planoPai);

        PlanoContas planoFilho = new PlanoContas.Builder()
                .codigo("1.1")
                .descricao("Receitas Operacionais")
                .tipo(TipoPlanoContas.RECEITA)
                .planoPai(planoPai)
                .build();

        // When
        PlanoContas planoSalvo = repository.save(planoFilho);

        // Then
        assertNotNull(planoSalvo.getId());
        assertEquals("1.1", planoSalvo.getCodigo());
        assertEquals("Receitas Operacionais", planoSalvo.getDescricao());
        assertNotNull(planoSalvo.getPlanoPai());
        assertEquals(planoPai.getId(), planoSalvo.getPlanoPai().getId());
        assertEquals(TipoPlanoContas.RECEITA, planoSalvo.getTipo());
    }

    @Test
    @DisplayName("Deve buscar plano de contas por ID")
    void deveBuscarPlanoContasPorId() {
        // Given
        PlanoContas plano = new PlanoContas.Builder()
                .codigo("2")
                .descricao("Despesas")
                .tipo(TipoPlanoContas.DESPESA)
                .build();
        PlanoContas planoSalvo = repository.save(plano);

        // When
        Optional<PlanoContas> resultado = repository.findById(planoSalvo.getId());

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Despesas", resultado.get().getDescricao());
        assertEquals(TipoPlanoContas.DESPESA, resultado.get().getTipo());
    }

    @Test
    @DisplayName("Deve validar constraint de código único")
    void deveValidarConstraintCodigoUnico() {
        // Given
        PlanoContas plano1 = new PlanoContas.Builder()
                .codigo("3")
                .descricao("Ativos")
                .tipo(TipoPlanoContas.ATIVO)
                .build();
        repository.save(plano1);

        PlanoContas plano2 = new PlanoContas.Builder()
                .codigo("3")
                .descricao("Ativos Duplicado")
                .tipo(TipoPlanoContas.ATIVO)
                .build();

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.save(plano2);
            repository.flush();
        });
    }

    @Test
    @DisplayName("Deve deletar plano de contas")
    void deveDeletarPlanoContas() {
        // Given
        PlanoContas plano = new PlanoContas.Builder()
                .codigo("4")
                .descricao("Passivos")
                .tipo(TipoPlanoContas.PASSIVO)
                .build();
        PlanoContas planoSalvo = repository.save(plano);

        // When
        repository.delete(planoSalvo);
        Optional<PlanoContas> resultado = repository.findById(planoSalvo.getId());

        // Then
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios")
    void deveValidarCamposObrigatorios() {
        // Given
        PlanoContas plano = new PlanoContas.Builder()
                .codigo("5")
                .descricao("Ativo Circulante")
                .tipo(TipoPlanoContas.ATIVO)
                .build();

        // When
        PlanoContas planoSalvo = repository.save(plano);

        // Then
        assertNotNull(planoSalvo.getId());
        assertNotNull(planoSalvo.getCodigo());
        assertNotNull(planoSalvo.getDescricao());
        assertNotNull(planoSalvo.getTipo());
        assertNotNull(planoSalvo.getAtivo());
        assertNotNull(planoSalvo.getCreatedAt());
    }
}
