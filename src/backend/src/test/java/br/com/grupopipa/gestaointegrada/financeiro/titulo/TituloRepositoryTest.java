package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
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
    private br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria tituloCategoria;
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

        // Criar categoria para os testes
        tituloCategoria = new br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria.Builder()
                .codigo("001")
                .nome("Despesas Operacionais")
                .descricao("Categoria de teste")
                .tipo(br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum.DESPESA)
                .build();
        entityManager.persist(tituloCategoria);

        // Criar pessoa para os testes
        pessoa = new Pessoa.Builder()
                .tipoPessoa(br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa.JURIDICA)
                .nome("Fornecedor Teste")
                .email("fornecedor@test.com")
                .telefone("11999999999")
                .cnpj("11222333000181")
                .razaoSocial("Fornecedor LTDA")
                .build();
        entityManager.persist(pessoa);
        entityManager.flush();
    }

    @Test
    @DisplayName("Deve salvar e recuperar título a pagar")
    void deveSalvarERecuperarTituloAPagar() {
        // Given
        Titulo titulo = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Pagamento fornecedor - NF 12345")
                .pessoa(pessoa)
                .tituloCategoria(tituloCategoria)

                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(1000.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(30))
                .rateioAutomatico(false)
                .build();

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
        assertEquals(Money.of(BigDecimal.valueOf(1000.00)), tituloRecuperado.getValorOriginal());
        assertNotNull(tituloRecuperado.getCreatedAt());
    }

    @Test
    @DisplayName("Deve salvar e recuperar título a receber")
    void deveSalvarERecuperarTituloAReceber() {
        // Given
        Pessoa cliente = new Pessoa.Builder()
                .tipoPessoa(br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa.JURIDICA)
                .nome("Cliente Teste")
                .email("cliente@test.com")
                .telefone("11988888888")
                .cnpj("06158095000152")
                .razaoSocial("Cliente LTDA")
                .build();
        entityManager.persist(cliente);

        TituloCategoria categoriaReceita = new TituloCategoria.Builder()
                .codigo("002")
                .nome("Vendas")
                .descricao("Receitas de vendas")
                .tipo(br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum.RECEITA)
                .build();
        entityManager.persist(categoriaReceita);

        Titulo titulo = new Titulo.Builder()
                .tipo(TipoTitulo.A_RECEBER)
                .descricao("Venda de produtos - NF 54321")
                .pessoa(cliente)
                .tituloCategoria(categoriaReceita)
                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(2500.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(15))
                .rateioAutomatico(false)
                .build();

        // When
        Titulo tituloSalvo = repository.save(titulo);
        entityManager.flush();

        // Then
        Optional<Titulo> resultado = repository.findById(tituloSalvo.getId());
        assertTrue(resultado.isPresent());
        assertEquals(TipoTitulo.A_RECEBER, resultado.get().getTipo());
        assertEquals(StatusTitulo.ABERTO, resultado.get().getStatus());
        assertEquals(Money.of(BigDecimal.valueOf(2500.00)), resultado.get().getValorOriginal());
    }

    @Test
    @DisplayName("Deve atualizar título existente")
    void deveAtualizarTituloExistente() {
        // Given
        Titulo titulo = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Título original")
                .pessoa(pessoa)
                .tituloCategoria(tituloCategoria)

                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(500.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(10))
                .rateioAutomatico(false)
                .build();
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
        Titulo titulo = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Título para deletar")
                .pessoa(pessoa)
                .tituloCategoria(tituloCategoria)

                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(100.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(5))
                .rateioAutomatico(false)
                .build();
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
        Titulo titulo = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Busca por ID")
                .pessoa(pessoa)
                .tituloCategoria(tituloCategoria)

                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(750.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(20))
                .rateioAutomatico(false)
                .build();
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
            new Titulo.Builder()
                    .tipo(null) // tipo nulo
                    .descricao("Descrição")
                    .pessoa(pessoa)
                .tituloCategoria(tituloCategoria)

                    .valorOriginal(Money.of(BigDecimal.valueOf(100.00)))
                    .dataEmissao(LocalDate.now())
                    .dataVencimento(LocalDate.now().plusDays(30))
                    .build();
        });
    }

    @Test
    @DisplayName("Não deve permitir título sem descrição")
    void naoDevePermitirTituloSemDescricao() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new Titulo.Builder()
                    .tipo(TipoTitulo.A_PAGAR)
                    .descricao(null) // descrição nula
                    .pessoa(pessoa)
                .tituloCategoria(tituloCategoria)

                    .valorOriginal(Money.of(BigDecimal.valueOf(100.00)))
                    .dataEmissao(LocalDate.now())
                    .dataVencimento(LocalDate.now().plusDays(30))
                    .build();
        });
    }

    @Test
    @DisplayName("Não deve permitir título sem pessoa")
    void naoDevePermitirTituloSemPessoa() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new Titulo.Builder()
                    .tipo(TipoTitulo.A_PAGAR)
                    .descricao("Descrição")
                    .pessoa(null) // pessoa nula
                    .valorOriginal(Money.of(BigDecimal.valueOf(100.00)))
                    .dataEmissao(LocalDate.now())
                    .dataVencimento(LocalDate.now().plusDays(30))
                    .build();
        });
    }

    @Test
    @DisplayName("Não deve permitir título com valor zero ou negativo")
    void naoDevePermitirTituloComValorZeroOuNegativo() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new Titulo.Builder()
                    .tipo(TipoTitulo.A_PAGAR)
                    .descricao("Descrição")
                    .pessoa(pessoa)
                .tituloCategoria(tituloCategoria)
                    .valorOriginal(Money.zero()) // valor zero
                    .dataEmissao(LocalDate.now())
                    .dataVencimento(LocalDate.now().plusDays(30))
                    .build();
        });
    }

    @Test
    @DisplayName("Não deve permitir data de vencimento anterior à emissão")
    void naoDevePermitirDataVencimentoAnteriorEmissao() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new Titulo.Builder()
                    .tipo(TipoTitulo.A_PAGAR)
                    .descricao("Descrição")
                    .pessoa(pessoa)
                .tituloCategoria(tituloCategoria)
                    .valorOriginal(Money.of(BigDecimal.valueOf(100.00)))
                    .dataEmissao(LocalDate.now())
                    .dataVencimento(LocalDate.now().minusDays(1)) // vencimento antes da emissão
                    .build();
        });
    }
}
