package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.PessoaJuridica;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.valueobject.CNPJ;
import br.com.grupopipa.gestaointegrada.core.valueobject.Email;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.core.valueobject.PhoneNumber;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para TituloRepository - CRUD básico.
 * Valida a persistência e consultas de títulos a pagar/receber.
 */
@DisplayName("TituloRepository - Testes de Integração")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TituloRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TituloRepository repository;

    @Autowired
    private EntityManager entityManager;

    private Pessoa pessoa;
    private PlanoContas planoContas;

    @BeforeEach
    void setUp() {
        // Criar pessoa para os testes
        pessoa = new PessoaJuridica(
                "Fornecedor Teste",
                new Email("fornecedor@test.com"),
                new PhoneNumber("11999999999"),
                new CNPJ("11.222.333/0001-81"),
                "Fornecedor LTDA"
        );
        entityManager.persist(pessoa);

        // Criar plano de contas para os testes
        planoContas = new PlanoContas(
                "4.1.001",
                "Fornecedores",
                TipoPlanoContas.DESPESA
        );
        entityManager.persist(planoContas);
        entityManager.flush();
    }

    @Test
    @DisplayName("Deve salvar e recuperar título a pagar")
    void deveSalvarERecuperarTituloAPagar() {
        // Given
        Titulo titulo = new Titulo(
                TipoTitulo.A_PAGAR,
                "Pagamento fornecedor - NF 12345",
                pessoa,
                planoContas,
                new Money(BigDecimal.valueOf(1000.00)),
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );

        // When
        Titulo tituloSalvo = repository.save(titulo);
        entityManager.flush();
        entityManager.clear();

        // Then
        Titulo tituloRecuperado = repository.findById(tituloSalvo.getId()).orElseThrow();
        assertNotNull(tituloRecuperado.getId());
        assertEquals(TipoTitulo.A_PAGAR, tituloRecuperado.getTipo());
        assertEquals(StatusTitulo.ABERTO, tituloRecuperado.getStatus());
        assertEquals("Pagamento fornecedor - NF 12345", tituloRecuperado.getDescricao());
        assertEquals(new Money(BigDecimal.valueOf(1000.00)), tituloRecuperado.getValorOriginal());
        assertNotNull(tituloRecuperado.getCreatedAt());
    }

    @Test
    @DisplayName("Deve salvar e recuperar título a receber")
    void deveSalvarERecuperarTituloAReceber() {
        // Given
        Pessoa cliente = new PessoaJuridica(
                "Cliente Teste",
                new Email("cliente@test.com"),
                new PhoneNumber("11988888888"),
                new CNPJ("06158095000152"),
                "Cliente LTDA"
        );
        entityManager.persist(cliente);

        PlanoContas planoReceita = new PlanoContas(
                "3.1.001",
                "Vendas de Produtos",
                TipoPlanoContas.RECEITA
        );
        entityManager.persist(planoReceita);
        entityManager.flush();

        Titulo titulo = new Titulo(
                TipoTitulo.A_RECEBER,
                "Venda de produtos - NF 54321",
                cliente,
                planoReceita,
                new Money(BigDecimal.valueOf(2500.00)),
                LocalDate.now(),
                LocalDate.now().plusDays(15)
        );

        // When
        Titulo tituloSalvo = repository.save(titulo);
        entityManager.flush();

        // Then
        Optional<Titulo> resultado = repository.findById(tituloSalvo.getId());
        assertTrue(resultado.isPresent());
        assertEquals(TipoTitulo.A_RECEBER, resultado.get().getTipo());
        assertEquals(StatusTitulo.ABERTO, resultado.get().getStatus());
        assertEquals(new Money(BigDecimal.valueOf(2500.00)), resultado.get().getValorOriginal());
    }

    @Test
    @DisplayName("Deve atualizar título existente")
    void deveAtualizarTituloExistente() {
        // Given
        Titulo titulo = new Titulo(
                TipoTitulo.A_PAGAR,
                "Título original",
                pessoa,
                planoContas,
                new Money(BigDecimal.valueOf(500.00)),
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );
        Titulo tituloSalvo = repository.save(titulo);
        entityManager.flush();
        entityManager.clear();

        // When
        Titulo tituloParaAtualizar = repository.findById(tituloSalvo.getId()).orElseThrow();
        tituloParaAtualizar.adicionarObservacao("Observação adicionada no teste");
        repository.save(tituloParaAtualizar);
        entityManager.flush();
        entityManager.clear();

        // Then
        Titulo tituloAtualizado = repository.findById(tituloSalvo.getId()).orElseThrow();
        assertTrue(tituloAtualizado.getObservacoes().contains("Observação adicionada no teste"));
    }

    @Test
    @DisplayName("Deve deletar título")
    void deveDeletarTitulo() {
        // Given
        Titulo titulo = new Titulo(
                TipoTitulo.A_PAGAR,
                "Título para deletar",
                pessoa,
                planoContas,
                new Money(BigDecimal.valueOf(100.00)),
                LocalDate.now(),
                LocalDate.now().plusDays(5)
        );
        Titulo tituloSalvo = repository.save(titulo);
        entityManager.flush();

        // When
        repository.delete(tituloSalvo);
        entityManager.flush();

        // Then
        Optional<Titulo> resultado = repository.findById(tituloSalvo.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve buscar título por ID")
    void deveBuscarTituloPorId() {
        // Given
        Titulo titulo = new Titulo(
                TipoTitulo.A_PAGAR,
                "Busca por ID",
                pessoa,
                planoContas,
                new Money(BigDecimal.valueOf(750.00)),
                LocalDate.now(),
                LocalDate.now().plusDays(20)
        );
        Titulo tituloSalvo = repository.save(titulo);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<Titulo> resultado = repository.findById(tituloSalvo.getId());

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(tituloSalvo.getId(), resultado.get().getId());
        assertEquals("Busca por ID", resultado.get().getDescricao());
    }

    @Test
    @DisplayName("Não deve permitir título sem tipo")
    void naoDevePermitirTituloSemTipo() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new Titulo(
                    null, // tipo nulo
                    "Descrição",
                    pessoa,
                    planoContas,
                    new Money(BigDecimal.valueOf(100.00)),
                    LocalDate.now(),
                    LocalDate.now().plusDays(30)
            );
        });
    }

    @Test
    @DisplayName("Não deve permitir título sem descrição")
    void naoDevePermitirTituloSemDescricao() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new Titulo(
                    TipoTitulo.A_PAGAR,
                    null, // descrição nula
                    pessoa,
                    planoContas,
                    new Money(BigDecimal.valueOf(100.00)),
                    LocalDate.now(),
                    LocalDate.now().plusDays(30)
            );
        });
    }

    @Test
    @DisplayName("Não deve permitir título sem pessoa")
    void naoDevePermitirTituloSemPessoa() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new Titulo(
                    TipoTitulo.A_PAGAR,
                    "Descrição",
                    null, // pessoa nula
                    planoContas,
                    new Money(BigDecimal.valueOf(100.00)),
                    LocalDate.now(),
                    LocalDate.now().plusDays(30)
            );
        });
    }

    @Test
    @DisplayName("Não deve permitir título sem plano de contas")
    void naoDevePermitirTituloSemPlanoContas() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new Titulo(
                    TipoTitulo.A_PAGAR,
                    "Descrição",
                    pessoa,
                    null, // planoContas nulo
                    new Money(BigDecimal.valueOf(100.00)),
                    LocalDate.now(),
                    LocalDate.now().plusDays(30)
            );
        });
    }

    @Test
    @DisplayName("Não deve permitir título com valor zero ou negativo")
    void naoDevePermitirTituloComValorZeroOuNegativo() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new Titulo(
                    TipoTitulo.A_PAGAR,
                    "Descrição",
                    pessoa,
                    planoContas,
                    Money.zero(), // valor zero
                    LocalDate.now(),
                    LocalDate.now().plusDays(30)
            );
        });
    }

    @Test
    @DisplayName("Não deve permitir data de vencimento anterior à emissão")
    void naoDevePermitirDataVencimentoAnteriorEmissao() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new Titulo(
                    TipoTitulo.A_PAGAR,
                    "Descrição",
                    pessoa,
                    planoContas,
                    new Money(BigDecimal.valueOf(100.00)),
                    LocalDate.now(),
                    LocalDate.now().minusDays(1) // vencimento antes da emissão
            );
        });
    }
}
