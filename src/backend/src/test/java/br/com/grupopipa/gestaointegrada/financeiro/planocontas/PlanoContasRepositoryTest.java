package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;
import jakarta.persistence.EntityManager;

/**
 * Testes de integração para PlanoContasRepository. Valida a persistência e
 * consultas do plano de
 * contas.
 */
@DisplayName("PlanoContasRepository - Testes de Integração")
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PlanoContasRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PlanoContasRepository repository;

    @Autowired
    private EntityManager entityManager;

    private UnidadeNegocio unidadeNegocio;

    @BeforeEach
    void setUp() {
        // Criar unidade de negócio para os testes
        unidadeNegocio = new UnidadeNegocio.Builder()
                .codigo("UN001")
                .nome("Unidade Teste")
                .cnpj("11222333000181")
                .build();
        entityManager.persist(unidadeNegocio);
        entityManager.flush();
    }

    @Test
    @DisplayName("Deve salvar e recuperar plano de contas raiz")
    void deveSalvarERecuperarPlanoContasRaiz() {
        // Given
        String codigo = (UUID.randomUUID().toString() + System.nanoTime()).replace("-", "").substring(0, 18);
        PlanoContas planoReceitas = new PlanoContas.Builder()
                .codigo(codigo)
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA)
                .unidadeNegocio(unidadeNegocio)
                .build();

        // When
        PlanoContas planoSalvo = repository.save(planoReceitas);

        // Then
        assertNotNull(planoSalvo.getId());
        assertEquals(codigo, planoSalvo.getCodigo());
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
        String codigoPai = (UUID.randomUUID().toString() + System.nanoTime()).replace("-", "").substring(0, 18);
        PlanoContas planoPai = new PlanoContas.Builder()
                .codigo(codigoPai)
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA)
                .unidadeNegocio(unidadeNegocio)
                .build();
        planoPai = repository.save(planoPai);

        String codigoFilho = (UUID.randomUUID().toString() + System.nanoTime()).replace("-", "").substring(0, 18);
        PlanoContas planoFilho = new PlanoContas.Builder()
                .codigo(codigoFilho)
                .descricao("Receitas Operacionais")
                .tipo(TipoPlanoContas.RECEITA)
                .unidadeNegocio(unidadeNegocio)
                .planoPai(planoPai)
                .build();

        // When
        PlanoContas planoSalvo = repository.save(planoFilho);

        // Then
        assertNotNull(planoSalvo.getId());
        assertEquals(codigoFilho, planoSalvo.getCodigo());
        assertEquals("Receitas Operacionais", planoSalvo.getDescricao());
        assertNotNull(planoSalvo.getPlanoPai());
        assertEquals(planoPai.getId(), planoSalvo.getPlanoPai().getId());
        assertEquals(TipoPlanoContas.RECEITA, planoSalvo.getTipo());
    }

    @Test
    @DisplayName("Deve buscar plano de contas por ID")
    void deveBuscarPlanoContasPorId() {
        // Given
        String codigo = (UUID.randomUUID().toString() + System.nanoTime()).replace("-", "").substring(0, 18);
        PlanoContas plano = new PlanoContas.Builder()
                .codigo(codigo)
                .descricao("Despesas")
                .tipo(TipoPlanoContas.DESPESA)
                .unidadeNegocio(unidadeNegocio)
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
        String uniqueCodigo = ("3" + UUID.randomUUID().toString() + System.nanoTime()).replace("-", "").substring(0,
                18);

        PlanoContas plano1 = new PlanoContas.Builder()
                .codigo(uniqueCodigo)
                .descricao("Ativos")
                .tipo(TipoPlanoContas.ATIVO)
                .unidadeNegocio(unidadeNegocio)
                .build();
        repository.save(plano1);

        PlanoContas plano2 = new PlanoContas.Builder()
                .codigo(uniqueCodigo)
                .descricao("Ativos Duplicado")
                .tipo(TipoPlanoContas.ATIVO)
                .unidadeNegocio(unidadeNegocio)
                .build();

        // When & Then
        assertThrows(
                DataIntegrityViolationException.class,
                () -> {
                    repository.save(plano2);
                    repository.flush();
                });
    }

    @Test
    @DisplayName("Deve deletar plano de contas")
    void deveDeletarPlanoContas() {
        // Given
        String codigo = (UUID.randomUUID().toString() + System.nanoTime()).replace("-", "").substring(0, 18);
        PlanoContas plano = new PlanoContas.Builder()
                .codigo(codigo)
                .descricao("Passivos")
                .tipo(TipoPlanoContas.PASSIVO)
                .unidadeNegocio(unidadeNegocio)
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
        String codigo = (UUID.randomUUID().toString() + System.nanoTime()).replace("-", "").substring(0, 18);
        PlanoContas plano = new PlanoContas.Builder()
                .codigo(codigo)
                .descricao("Ativo Circulante")
                .tipo(TipoPlanoContas.ATIVO)
                .unidadeNegocio(unidadeNegocio)
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
