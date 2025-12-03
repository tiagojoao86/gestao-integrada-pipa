package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.PessoaJuridica;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.valueobject.CNPJ;
import br.com.grupopipa.gestaointegrada.core.valueobject.Email;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.core.valueobject.PhoneNumber;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.entity.MovimentacaoFinanceira;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
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
 * Testes de integração para MovimentacaoFinanceiraRepository - CRUD básico.
 * Valida a persistência e consultas de movimentações financeiras.
 */
@DisplayName("MovimentacaoFinanceiraRepository - Testes de Integração")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MovimentacaoFinanceiraRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MovimentacaoFinanceiraRepository repository;

    @Autowired
    private EntityManager entityManager;

    private Titulo titulo;
    private ContaBancaria contaBancaria;

    @BeforeEach
    void setUp() {
        // Criar pessoa
        Pessoa pessoa = new PessoaJuridica(
                "Fornecedor Teste",
                new Email("fornecedor@test.com"),
                new PhoneNumber("11999999999"),
                new CNPJ("11.222.333/0001-81"),
                "Fornecedor LTDA"
        );
        entityManager.persist(pessoa);

        // Criar plano de contas
        PlanoContas planoContas = new PlanoContas(
                "4.1.001",
                "Fornecedores",
                TipoPlanoContas.DESPESA
        );
        entityManager.persist(planoContas);

        // Criar título
        titulo = new Titulo(
                TipoTitulo.A_PAGAR,
                "Pagamento fornecedor",
                pessoa,
                planoContas,
                new Money(BigDecimal.valueOf(1000.00)),
                LocalDate.now(),
                LocalDate.now().plusDays(30)
        );
        entityManager.persist(titulo);

        // Criar conta bancária
        contaBancaria = new ContaBancaria(
                "Conta Corrente Principal",
                TipoConta.CORRENTE,
                "Banco do Brasil",
                "1234",
                "12345-6"
        );
        contaBancaria.definirSaldoInicial(new Money(BigDecimal.valueOf(5000.00)));
        entityManager.persist(contaBancaria);

        entityManager.flush();
    }

    @Test
    @DisplayName("Deve salvar e recuperar movimentação de pagamento")
    void deveSalvarERecuperarMovimentacaoPagamento() {
        // Given
        MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira(
                titulo,
                contaBancaria,
                TipoMovimentacao.PAGAMENTO,
                FormaPagamento.PIX,
                new Money(BigDecimal.valueOf(500.00)),
                LocalDate.now()
        );

        // When
        MovimentacaoFinanceira movimentacaoSalva = repository.save(movimentacao);
        entityManager.flush();
        entityManager.clear();

        // Then
        MovimentacaoFinanceira recuperada = repository.findById(movimentacaoSalva.getId()).orElseThrow();
        assertNotNull(recuperada.getId());
        assertEquals(TipoMovimentacao.PAGAMENTO, recuperada.getTipo());
        assertEquals(FormaPagamento.PIX, recuperada.getFormaPagamento());
        assertEquals(new Money(BigDecimal.valueOf(500.00)), recuperada.getValor());
        assertNotNull(recuperada.getCreatedAt());
        assertTrue(recuperada.isPagamento());
    }

    @Test
    @DisplayName("Deve salvar e recuperar movimentação de recebimento")
    void deveSalvarERecuperarMovimentacaoRecebimento() {
        // Given
        // Criar título a receber
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
                "Vendas",
                TipoPlanoContas.RECEITA
        );
        entityManager.persist(planoReceita);

        Titulo tituloReceber = new Titulo(
                TipoTitulo.A_RECEBER,
                "Venda de produtos",
                cliente,
                planoReceita,
                new Money(BigDecimal.valueOf(2000.00)),
                LocalDate.now(),
                LocalDate.now().plusDays(15)
        );
        entityManager.persist(tituloReceber);
        entityManager.flush();

        MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira(
                tituloReceber,
                contaBancaria,
                TipoMovimentacao.RECEBIMENTO,
                FormaPagamento.BOLETO,
                new Money(BigDecimal.valueOf(2000.00)),
                LocalDate.now()
        );

        // When
        MovimentacaoFinanceira movimentacaoSalva = repository.save(movimentacao);
        entityManager.flush();

        // Then
        Optional<MovimentacaoFinanceira> resultado = repository.findById(movimentacaoSalva.getId());
        assertTrue(resultado.isPresent());
        assertEquals(TipoMovimentacao.RECEBIMENTO, resultado.get().getTipo());
        assertTrue(resultado.get().isRecebimento());
    }

    @Test
    @DisplayName("Deve adicionar observações à movimentação")
    void deveAdicionarObservacoesMovimentacao() {
        // Given
        MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira(
                titulo,
                contaBancaria,
                TipoMovimentacao.PAGAMENTO,
                FormaPagamento.TED,
                new Money(BigDecimal.valueOf(300.00)),
                LocalDate.now()
        );
        repository.save(movimentacao);
        entityManager.flush();

        // When
        movimentacao.adicionarObservacao("Pagamento efetuado com sucesso");
        repository.save(movimentacao);
        entityManager.flush();
        entityManager.clear();

        // Then
        MovimentacaoFinanceira recuperada = repository.findById(movimentacao.getId()).orElseThrow();
        assertNotNull(recuperada.getObservacoes());
        assertTrue(recuperada.getObservacoes().contains("Pagamento efetuado com sucesso"));
    }

    @Test
    @DisplayName("Deve registrar pagamento parcial no título")
    void deveRegistrarPagamentoParcialNoTitulo() {
        // Given
        MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira(
                titulo,
                contaBancaria,
                TipoMovimentacao.PAGAMENTO,
                FormaPagamento.DINHEIRO,
                new Money(BigDecimal.valueOf(400.00)), // Pagamento parcial
                LocalDate.now()
        );

        // When
        repository.save(movimentacao);
        entityManager.flush();
        entityManager.refresh(titulo);

        // Then
        assertEquals(StatusTitulo.PARCIAL, titulo.getStatus());
        assertEquals(new Money(BigDecimal.valueOf(400.00)), titulo.getValorPago());
        assertEquals(new Money(BigDecimal.valueOf(600.00)), titulo.calcularSaldo());
    }

    @Test
    @DisplayName("Deve registrar pagamento total e marcar título como pago")
    void deveRegistrarPagamentoTotalETituloPago() {
        // Given
        MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira(
                titulo,
                contaBancaria,
                TipoMovimentacao.PAGAMENTO,
                FormaPagamento.CARTAO_CREDITO,
                new Money(BigDecimal.valueOf(1000.00)), // Pagamento total
                LocalDate.now()
        );

        // When
        repository.save(movimentacao);
        entityManager.flush();
        entityManager.refresh(titulo);

        // Then
        assertEquals(StatusTitulo.PAGO, titulo.getStatus());
        assertEquals(new Money(BigDecimal.valueOf(1000.00)), titulo.getValorPago());
        assertTrue(titulo.calcularSaldo().isZero());
        assertNotNull(titulo.getDataPagamento());
    }

    @Test
    @DisplayName("Deve deletar movimentação")
    void deveDeletarMovimentacao() {
        // Given
        MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira(
                titulo,
                contaBancaria,
                TipoMovimentacao.PAGAMENTO,
                FormaPagamento.DOC,
                new Money(BigDecimal.valueOf(200.00)),
                LocalDate.now()
        );
        MovimentacaoFinanceira movimentacaoSalva = repository.save(movimentacao);
        entityManager.flush();

        // When
        repository.delete(movimentacaoSalva);
        entityManager.flush();

        // Then
        Optional<MovimentacaoFinanceira> resultado = repository.findById(movimentacaoSalva.getId());
        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve buscar movimentação por ID")
    void deveBuscarMovimentacaoPorId() {
        // Given
        MovimentacaoFinanceira movimentacao = new MovimentacaoFinanceira(
                titulo,
                contaBancaria,
                TipoMovimentacao.PAGAMENTO,
                FormaPagamento.CHEQUE,
                new Money(BigDecimal.valueOf(150.00)),
                LocalDate.now()
        );
        MovimentacaoFinanceira movimentacaoSalva = repository.save(movimentacao);
        entityManager.flush();
        entityManager.clear();

        // When
        Optional<MovimentacaoFinanceira> resultado = repository.findById(movimentacaoSalva.getId());

        // Then
        assertTrue(resultado.isPresent());
        assertEquals(movimentacaoSalva.getId(), resultado.get().getId());
        assertEquals(new Money(BigDecimal.valueOf(150.00)), resultado.get().getValor());
    }

    @Test
    @DisplayName("Não deve permitir movimentação com valor zero")
    void naoDevePermitirMovimentacaoComValorZero() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new MovimentacaoFinanceira(
                    titulo,
                    contaBancaria,
                    TipoMovimentacao.PAGAMENTO,
                    FormaPagamento.PIX,
                    Money.zero(), // Valor zero
                    LocalDate.now()
            );
        });
    }

    @Test
    @DisplayName("Não deve permitir movimentação sem título")
    void naoDevePermitirMovimentacaoSemTitulo() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new MovimentacaoFinanceira(
                    null, // Título nulo
                    contaBancaria,
                    TipoMovimentacao.PAGAMENTO,
                    FormaPagamento.PIX,
                    new Money(BigDecimal.valueOf(100.00)),
                    LocalDate.now()
            );
        });
    }

    @Test
    @DisplayName("Não deve permitir movimentação sem conta bancária")
    void naoDevePermitirMovimentacaoSemContaBancaria() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new MovimentacaoFinanceira(
                    titulo,
                    null, // Conta bancária nula
                    TipoMovimentacao.PAGAMENTO,
                    FormaPagamento.PIX,
                    new Money(BigDecimal.valueOf(100.00)),
                    LocalDate.now()
            );
        });
    }

    @Test
    @DisplayName("Não deve permitir movimentação com valor maior que o saldo do título")
    void naoDevePermitirMovimentacaoComValorMaiorQueSaldo() {
        // When & Then
        assertThrows(BeanValidationException.class, () -> {
            new MovimentacaoFinanceira(
                    titulo,
                    contaBancaria,
                    TipoMovimentacao.PAGAMENTO,
                    FormaPagamento.PIX,
                    new Money(BigDecimal.valueOf(1500.00)), // Maior que valor do título (1000)
                    LocalDate.now()
            );
        });
    }
}
