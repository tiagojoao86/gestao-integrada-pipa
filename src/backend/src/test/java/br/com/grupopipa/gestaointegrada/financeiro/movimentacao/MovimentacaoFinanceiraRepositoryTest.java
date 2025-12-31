package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.entity.MovimentacaoFinanceira;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum;

/**
 * Testes de integração para MovimentacaoFinanceiraRepository - CRUD básico. Valida a persistência e
 * consultas de movimentações financeiras.
 */
@DisplayName("MovimentacaoFinanceiraRepository - Testes de Integração")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MovimentacaoFinanceiraRepositoryTest extends AbstractIntegrationTest {

  @Autowired private MovimentacaoFinanceiraRepository repository;

  @Autowired private EntityManager entityManager;

  private Titulo titulo;
  private ContaBancaria contaBancaria;
  private UnidadeNegocio unidadeNegocio;

  @BeforeEach
  void setUp() {
    // Criar unidade de negócio
    unidadeNegocio =
        new UnidadeNegocio.Builder()
            .codigo("UN001")
            .nome("Unidade Teste")
            .cnpj("11222333000181")
            .build();
    entityManager.persist(unidadeNegocio);

    // Criar pessoa
    Pessoa pessoa =
        new Pessoa.Builder()
            .tipoPessoa(br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa.JURIDICA)
            .nome("Fornecedor Teste")
            .email("fornecedor@test.com")
            .telefone("11999999999")
            .cnpj("11222333000181")
            .razaoSocial("Fornecedor LTDA")
            .build();
    entityManager.persist(pessoa);

    // Criar categoria de título
    TituloCategoria tituloCategoria =
        new TituloCategoria.Builder()
            .codigo("001")
            .nome("Fornecedores")
            .descricao("Despesas com fornecedores")
            .tipo(TituloCategoriaTipoEnum.DESPESA)
            .build();
    entityManager.persist(tituloCategoria);

    // Criar título
    titulo =
        new Titulo.Builder()
            .tipo(TipoTitulo.A_PAGAR)
            .descricao("Pagamento fornecedor")
            .pessoa(pessoa)
            .tituloCategoria(tituloCategoria)
            .unidadeNegocio(unidadeNegocio)
            .valorOriginal(Money.of(BigDecimal.valueOf(1000.00)))
            .dataEmissao(LocalDate.now())
            .dataVencimento(LocalDate.now().plusDays(30))
            .rateioAutomatico(false)
            .build();
    entityManager.persist(titulo);

    // Criar conta bancária
    contaBancaria =
        new ContaBancaria.Builder()
            .nome("Conta Corrente Principal")
            .tipo(TipoConta.CORRENTE)
            .banco("Banco do Brasil")
            .agencia("1234")
            .numeroConta("12345-6")
            .saldoInicial(Money.of(BigDecimal.valueOf(5000.00)))
            .unidadeNegocio(unidadeNegocio)
            .build();
    entityManager.persist(contaBancaria);

    entityManager.flush();
  }

  @Test
  @DisplayName("Deve salvar e recuperar movimentação de pagamento")
  void deveSalvarERecuperarMovimentacaoPagamento() {
    // Given
    MovimentacaoFinanceira movimentacao =
        new MovimentacaoFinanceira.Builder()
            .titulos(new java.util.HashSet<>(java.util.List.of(titulo)))
            .contaBancaria(contaBancaria)
            .tipo(TipoMovimentacao.PAGAMENTO)
            .formaPagamento(FormaPagamento.PIX)
            .valor(Money.of(BigDecimal.valueOf(500.00)))
            .data(LocalDate.now())
            .build();

    // When
    MovimentacaoFinanceira movimentacaoSalva = repository.save(movimentacao);
    entityManager.flush();
    entityManager.clear();

    // Then
    MovimentacaoFinanceira recuperada =
        repository.findById(movimentacaoSalva.getId()).orElseThrow();
    assertNotNull(recuperada.getId());
    assertEquals(TipoMovimentacao.PAGAMENTO, recuperada.getTipo());
    assertEquals(FormaPagamento.PIX, recuperada.getFormaPagamento());
    assertEquals(Money.of(BigDecimal.valueOf(500.00)), recuperada.getValor());
    assertNotNull(recuperada.getCreatedAt());
    assertTrue(recuperada.isPagamento());
  }

  @Test
  @DisplayName("Deve salvar e recuperar movimentação de recebimento")
  void deveSalvarERecuperarMovimentacaoRecebimento() {
    // Given
    // Criar título a receber
    Pessoa cliente =
        new Pessoa.Builder()
            .tipoPessoa(br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa.JURIDICA)
            .nome("Cliente Teste")
            .email("cliente@test.com")
            .telefone("11988888888")
            .cnpj("06158095000152")
            .razaoSocial("Cliente LTDA")
            .build();
    entityManager.persist(cliente);

    TituloCategoria categoriaReceita =
        new TituloCategoria.Builder()
            .codigo("002")
            .nome("Vendas")
            .descricao("Receitas de vendas")
            .tipo(TituloCategoriaTipoEnum.RECEITA)
            .build();
    entityManager.persist(categoriaReceita);

    Titulo tituloReceber =
        new Titulo.Builder()
            .tipo(TipoTitulo.A_RECEBER)
            .descricao("Venda de produtos")
            .pessoa(cliente)
            .tituloCategoria(categoriaReceita)
            .unidadeNegocio(unidadeNegocio)
            .valorOriginal(Money.of(BigDecimal.valueOf(2000.00)))
            .dataEmissao(LocalDate.now())
            .dataVencimento(LocalDate.now().plusDays(15))
            .rateioAutomatico(false)
            .build();
    entityManager.persist(tituloReceber);
    entityManager.flush();

    MovimentacaoFinanceira movimentacao =
        new MovimentacaoFinanceira.Builder()
            .titulos(new java.util.HashSet<>(java.util.List.of(tituloReceber)))
            .contaBancaria(contaBancaria)
            .tipo(TipoMovimentacao.RECEBIMENTO)
            .formaPagamento(FormaPagamento.BOLETO)
            .valor(Money.of(BigDecimal.valueOf(2000.00)))
            .data(LocalDate.now())
            .build();

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
    MovimentacaoFinanceira movimentacao =
        new MovimentacaoFinanceira.Builder()
            .titulos(new java.util.HashSet<>(java.util.List.of(titulo)))
            .contaBancaria(contaBancaria)
            .tipo(TipoMovimentacao.PAGAMENTO)
            .formaPagamento(FormaPagamento.TED)
            .valor(Money.of(BigDecimal.valueOf(300.00)))
            .data(LocalDate.now())
            .build();
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
    MovimentacaoFinanceira movimentacao =
        new MovimentacaoFinanceira.Builder()
            .titulos(new java.util.HashSet<>(java.util.List.of(titulo)))
            .contaBancaria(contaBancaria)
            .tipo(TipoMovimentacao.PAGAMENTO)
            .formaPagamento(FormaPagamento.DINHEIRO)
            .valor(Money.of(BigDecimal.valueOf(400.00))) // Pagamento parcial
            .data(LocalDate.now())
            .build();

    // When
    repository.save(movimentacao);
    entityManager.flush();
    entityManager.refresh(titulo);

    // Then
    assertEquals(StatusTitulo.PARCIAL, titulo.getStatus());
    assertEquals(Money.of(BigDecimal.valueOf(400.00)), titulo.getValorPago());
    assertEquals(Money.of(BigDecimal.valueOf(600.00)), titulo.calcularSaldo());
  }

  @Test
  @DisplayName("Deve registrar pagamento total e marcar título como pago")
  void deveRegistrarPagamentoTotalETituloPago() {
    // Given
    MovimentacaoFinanceira movimentacao =
        new MovimentacaoFinanceira.Builder()
            .titulos(new java.util.HashSet<>(java.util.List.of(titulo)))
            .contaBancaria(contaBancaria)
            .tipo(TipoMovimentacao.PAGAMENTO)
            .formaPagamento(FormaPagamento.CARTAO_CREDITO)
            .valor(Money.of(BigDecimal.valueOf(1000.00))) // Pagamento total
            .data(LocalDate.now())
            .build();

    // When
    repository.save(movimentacao);
    entityManager.flush();
    entityManager.refresh(titulo);

    // Then
    assertEquals(StatusTitulo.PAGO, titulo.getStatus());
    assertEquals(Money.of(BigDecimal.valueOf(1000.00)), titulo.getValorPago());
    assertTrue(titulo.calcularSaldo().isZero());
    assertNotNull(titulo.getDataPagamento());
  }

  @Test
  @DisplayName("Deve deletar movimentação")
  void deveDeletarMovimentacao() {
    // Given
    MovimentacaoFinanceira movimentacao =
        new MovimentacaoFinanceira.Builder()
            .titulos(new java.util.HashSet<>(java.util.List.of(titulo)))
            .contaBancaria(contaBancaria)
            .tipo(TipoMovimentacao.PAGAMENTO)
            .formaPagamento(FormaPagamento.DOC)
            .valor(Money.of(BigDecimal.valueOf(200.00)))
            .data(LocalDate.now())
            .build();
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
    MovimentacaoFinanceira movimentacao =
        new MovimentacaoFinanceira.Builder()
            .titulos(new java.util.HashSet<>(java.util.List.of(titulo)))
            .contaBancaria(contaBancaria)
            .tipo(TipoMovimentacao.PAGAMENTO)
            .formaPagamento(FormaPagamento.CHEQUE)
            .valor(Money.of(BigDecimal.valueOf(150.00)))
            .data(LocalDate.now())
            .build();
    MovimentacaoFinanceira movimentacaoSalva = repository.save(movimentacao);
    entityManager.flush();
    entityManager.clear();

    // When
    Optional<MovimentacaoFinanceira> resultado = repository.findById(movimentacaoSalva.getId());

    // Then
    assertTrue(resultado.isPresent());
    assertEquals(movimentacaoSalva.getId(), resultado.get().getId());
    assertEquals(Money.of(BigDecimal.valueOf(150.00)), resultado.get().getValor());
  }

  @Test
  @DisplayName("Não deve permitir movimentação com valor zero")
  void naoDevePermitirMovimentacaoComValorZero() {
    // When & Then
    assertThrows(
        BeanValidationException.class,
        () -> {
          new MovimentacaoFinanceira.Builder()
              .titulos(new java.util.HashSet<>(java.util.List.of(titulo)))
              .contaBancaria(contaBancaria)
              .tipo(TipoMovimentacao.PAGAMENTO)
              .formaPagamento(FormaPagamento.PIX)
              .valor(Money.zero()) // Valor zero
              .data(LocalDate.now())
              .build();
        });
  }

  @Test
  @DisplayName("Não deve permitir movimentação sem título")
  void naoDevePermitirMovimentacaoSemTitulo() {
    // When & Then
    assertThrows(
        BeanValidationException.class,
        () -> {
          new MovimentacaoFinanceira.Builder()
              .titulos(new java.util.HashSet<>()) // Nenhum título
              .contaBancaria(contaBancaria)
              .tipo(TipoMovimentacao.PAGAMENTO)
              .formaPagamento(FormaPagamento.PIX)
              .valor(Money.of(BigDecimal.valueOf(100.00)))
              .data(LocalDate.now())
              .build();
        });
  }

  @Test
  @DisplayName("Não deve permitir movimentação sem conta bancária")
  void naoDevePermitirMovimentacaoSemContaBancaria() {
    // When & Then
    assertThrows(
        BeanValidationException.class,
        () -> {
          new MovimentacaoFinanceira.Builder()
              .titulos(new java.util.HashSet<>(java.util.List.of(titulo)))
              .contaBancaria(null) // Conta bancária nula
              .tipo(TipoMovimentacao.PAGAMENTO)
              .formaPagamento(FormaPagamento.PIX)
              .valor(Money.of(BigDecimal.valueOf(100.00)))
              .data(LocalDate.now())
              .build();
        });
  }

  @Test
  @DisplayName("Não deve permitir movimentação com valor maior que o saldo do título")
  void naoDevePermitirMovimentacaoComValorMaiorQueSaldo() {
    // When & Then
    assertThrows(
        BeanValidationException.class,
        () -> {
          new MovimentacaoFinanceira.Builder()
              .titulos(new java.util.HashSet<>(java.util.List.of(titulo)))
              .contaBancaria(contaBancaria)
              .tipo(TipoMovimentacao.PAGAMENTO)
              .formaPagamento(FormaPagamento.PIX)
              .valor(Money.of(BigDecimal.valueOf(1500.00))) // Maior que valor do título
              // (1000)
              .data(LocalDate.now())
              .build();
        });
  }
}
