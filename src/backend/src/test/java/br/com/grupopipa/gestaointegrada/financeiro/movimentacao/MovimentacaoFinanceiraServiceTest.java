package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.contabancaria.ContaBancariaRepository;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.entity.MovimentacaoFinanceira;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloRepository;
import br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para MovimentacaoFinanceiraServiceImpl - CRUD básico.
 * Valida as regras de negócio do serviço de movimentações financeiras.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MovimentacaoFinanceiraService - Testes Unitários")
class MovimentacaoFinanceiraServiceTest {

    @Mock
    private MovimentacaoFinanceiraRepository repository;

    @Mock
    private TituloRepository tituloRepository;

    @Mock
    private ContaBancariaRepository contaBancariaRepository;

    @Mock
    private Specifications<MovimentacaoFinanceira> specifications;

    @InjectMocks
    private MovimentacaoFinanceiraServiceImpl service;

    private Titulo titulo;
    private ContaBancaria contaBancaria;
    private UnidadeNegocio unidadeNegocio;
    private MovimentacaoFinanceiraDTO dto;
    private MovimentacaoTituloDTO movTituloDTO;
    private List<MovimentacaoTituloDTO> movTitulosDTO;
    private MovimentacaoFinanceira entity;

    @BeforeEach
    void setUp() {
        // Setup unidade de negócio
        unidadeNegocio = new UnidadeNegocio.Builder()
                .codigo("UN001")
                .nome("Unidade Teste")
                .cnpj("11222333000181")
                .build();

        // Setup pessoa
        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa.JURIDICA)
                .nome("Fornecedor Teste")
                .email("fornecedor@test.com")
                .telefone("11999999999")
                .cnpj("11222333000181")
                .razaoSocial("Fornecedor LTDA")
                .build();

        // Setup titulo categoria
        br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria tituloCategoria =
            new br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria.Builder()
                .codigo("001")
                .nome("Despesas Gerais")
                .tipo(br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum.DESPESA)
                .build();

        // Setup título
        titulo = new Titulo.Builder()
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

        // Setup conta bancária
        contaBancaria = new ContaBancaria.Builder()
                .nome("Conta Corrente Principal")
                .tipo(TipoConta.CORRENTE)
                .banco("Banco do Brasil")
                .agencia("1234")
                .numeroConta("12345-6")
                .saldoInicial(Money.zero())
                .unidadeNegocio(unidadeNegocio)
                .build();

        // Setup DTO com múltiplos títulos
        movTituloDTO = MovimentacaoTituloDTO.builder()
                .id(titulo.getId())
                .descricao(titulo.getDescricao())
                .build();
        movTitulosDTO = List.of(movTituloDTO);
        dto = MovimentacaoFinanceiraDTO.builder()
                .titulos(movTitulosDTO)
                .contaBancariaId(UUID.randomUUID())
                .tipo(TipoMovimentacao.PAGAMENTO.name())
                .formaPagamento(FormaPagamento.PIX.name())
                .valor(BigDecimal.valueOf(500.00))
                .data(LocalDate.now())
                .build();

        // Setup Entity
        entity = new MovimentacaoFinanceira.Builder()
                .titulos(java.util.Set.of(titulo))
                .contaBancaria(contaBancaria)
                .tipo(TipoMovimentacao.PAGAMENTO)
                .formaPagamento(FormaPagamento.PIX)
                .valor(Money.of(BigDecimal.valueOf(500.00)))
                .data(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar movimentação de pagamento")
    void deveCriarMovimentacaoPagamento() {
        // Given
        for (MovimentacaoTituloDTO mt : dto.getTitulos()) {
            when(tituloRepository.findById(mt.getId())).thenReturn(Optional.of(titulo));
        }
        when(contaBancariaRepository.findById(dto.getContaBancariaId())).thenReturn(Optional.of(contaBancaria));

        // When
        MovimentacaoFinanceira resultado = service.mergeEntityAndDTO(null, dto);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoMovimentacao.PAGAMENTO, resultado.getTipo());
        assertEquals(FormaPagamento.PIX, resultado.getFormaPagamento());
        assertEquals(Money.of(BigDecimal.valueOf(500.00)), resultado.getValor());
        assertTrue(resultado.getTitulos().contains(titulo));
        assertEquals(contaBancaria, resultado.getContaBancaria());
        assertTrue(resultado.isPagamento());
    }

    @Test
    @DisplayName("Deve criar movimentação de recebimento")
    void deveCriarMovimentacaoRecebimento() {
        // Given
        dto.setTipo(TipoMovimentacao.RECEBIMENTO.name());

        for (MovimentacaoTituloDTO mt : dto.getTitulos()) {
            when(tituloRepository.findById(mt.getId())).thenReturn(Optional.of(titulo));
        }
        when(contaBancariaRepository.findById(dto.getContaBancariaId())).thenReturn(Optional.of(contaBancaria));

        // When
        MovimentacaoFinanceira resultado = service.mergeEntityAndDTO(null, dto);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoMovimentacao.RECEBIMENTO, resultado.getTipo());
        assertTrue(resultado.isRecebimento());
    }

    @Test
    @DisplayName("Deve buscar movimentação por ID")
    void deveBuscarMovimentacaoPorId() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        // When
        MovimentacaoFinanceiraDTO resultado = service.findById(id);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoMovimentacao.PAGAMENTO.name(), resultado.getTipo());
        assertEquals(FormaPagamento.PIX.name(), resultado.getFormaPagamento());
        assertEquals(0, resultado.getValor().compareTo(BigDecimal.valueOf(500.00)));
        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve salvar movimentação")
    void deveSalvarMovimentacao() {
        // Given
        for (MovimentacaoTituloDTO mt : dto.getTitulos()) {
            when(tituloRepository.findById(mt.getId())).thenReturn(Optional.of(titulo));
        }
        when(contaBancariaRepository.findById(dto.getContaBancariaId())).thenReturn(Optional.of(contaBancaria));
        when(repository.save(any(MovimentacaoFinanceira.class))).thenReturn(entity);

        // When
        MovimentacaoFinanceiraDTO resultado = service.save(dto);

        // Then
        assertNotNull(resultado);
        verify(repository, times(1)).save(any(MovimentacaoFinanceira.class));
    }

    @Test
    @DisplayName("Deve deletar movimentação")
    void deveDeletarMovimentacao() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(MovimentacaoFinanceira.class))).thenReturn(entity);

        // When
        UUID resultado = service.delete(id);

        // Then
        assertEquals(id, resultado);
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(MovimentacaoFinanceira.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar movimentação inexistente")
    void deveLancarExcecaoAoBuscarMovimentacaoInexistente() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> service.findById(id));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar movimentação sem título")
    void deveLancarExcecaoAoCriarMovimentacaoSemTitulo() {
        // Given
        for (MovimentacaoTituloDTO mt : dto.getTitulos()) {
            when(tituloRepository.findById(mt.getId())).thenReturn(Optional.empty());
        }

        // When & Then
        assertThrows(RuntimeException.class, () -> service.mergeEntityAndDTO(null, dto));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar movimentação sem conta bancária")
    void deveLancarExcecaoAoCriarMovimentacaoSemContaBancaria() {
        // Given
        for (MovimentacaoTituloDTO mt : dto.getTitulos()) {
            when(tituloRepository.findById(mt.getId())).thenReturn(Optional.of(titulo));
        }
        when(contaBancariaRepository.findById(dto.getContaBancariaId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> service.mergeEntityAndDTO(null, dto));
    }

    @Test
    @DisplayName("Deve criar movimentação com diferentes formas de pagamento")
    void deveCriarMovimentacaoComDiferentesFormasPagamento() {
        // Given
        dto.setFormaPagamento(FormaPagamento.BOLETO.name());
        for (MovimentacaoTituloDTO mt : dto.getTitulos()) {
            when(tituloRepository.findById(mt.getId())).thenReturn(Optional.of(titulo));
        }
        when(contaBancariaRepository.findById(dto.getContaBancariaId())).thenReturn(Optional.of(contaBancaria));

        // When
        MovimentacaoFinanceira resultado = service.mergeEntityAndDTO(null, dto);

        // Then
        assertEquals(FormaPagamento.BOLETO, resultado.getFormaPagamento());
    }

    @Test
    @DisplayName("Deve marcar título como PAGO quando saldo zerar após movimentação")
    void deveMarcaTituloComoPagoQuandoSaldoZerar() {
        // Given - criar um novo título sem movimentações anteriores
        br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria tituloCategoria =
            new br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria.Builder()
                .codigo("002")
                .nome("Categoria Teste 2")
                .tipo(br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum.DESPESA)
                .build();

        Titulo tituloNovo = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Título para quitar")
                .pessoa(titulo.getPessoa())
                .tituloCategoria(tituloCategoria)
                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(1000.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(30))
                .build();

        MovimentacaoTituloDTO movTituloNovoDTO = MovimentacaoTituloDTO.builder()
                .id(tituloNovo.getId())
                .descricao(tituloNovo.getDescricao())
                .build();

        dto.setTitulos(List.of(movTituloNovoDTO));
        dto.setValor(BigDecimal.valueOf(1000.00)); // Valor total do título
        when(tituloRepository.findById(movTituloNovoDTO.getId())).thenReturn(Optional.of(tituloNovo));
        when(contaBancariaRepository.findById(dto.getContaBancariaId())).thenReturn(Optional.of(contaBancaria));

        // When
        MovimentacaoFinanceira resultado = service.mergeEntityAndDTO(null, dto);

        // Then
        assertNotNull(resultado);
        // Verificar que o título foi atualizado para PAGO
        assertEquals(br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo.PAGO,
                     tituloNovo.getStatus());
        assertNotNull(tituloNovo.getDataPagamento());
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor pago ultrapassar valor total do título")
    void deveLancarExcecaoQuandoValorPagoUltrapassarValorTotal() {
        // Given - criar movimentação que ultrapassaria o valor total
        dto.setValor(BigDecimal.valueOf(1500.00)); // Mais que o valor original de 1000
        for (MovimentacaoTituloDTO mt : dto.getTitulos()) {
            when(tituloRepository.findById(mt.getId())).thenReturn(Optional.of(titulo));
        }
        when(contaBancariaRepository.findById(dto.getContaBancariaId())).thenReturn(Optional.of(contaBancaria));

        // When & Then
        BeanValidationException exception =
            assertThrows(BeanValidationException.class,
                        () -> service.mergeEntityAndDTO(null, dto));

        // Verificar que a mensagem de erro contém informação sobre ultrapassar o valor total
        assertTrue(exception.getViolations().stream()
            .anyMatch(v -> v.getKey().contains("valorPagoUltrapassaTotal")));
    }

    @Test
    @DisplayName("Deve validar atomicidade - nenhuma movimentação salva se um título falhar")
    void deveValidarAtomicidade() {
        // Given - criar segundo título com valor menor que falhará na validação
        TituloCategoria categoria =
            new TituloCategoria.Builder()
                .codigo("002")
                .nome("Categoria Teste")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .build();

        Titulo titulo2 = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Segundo titulo")
                .pessoa(titulo.getPessoa())
                .tituloCategoria(categoria)
                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(100.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(30))
                .build();

        // Criar DTOs para ambos os títulos
        MovimentacaoTituloDTO movTitulo2DTO = MovimentacaoTituloDTO.builder()
                .id(titulo2.getId())
                .descricao(titulo2.getDescricao())
                .build();

        dto.setTitulos(List.of(movTituloDTO, movTitulo2DTO));
        dto.setValor(BigDecimal.valueOf(200.00)); // Valor que ultrapassará o segundo título (que tem apenas 100)

        when(tituloRepository.findById(movTituloDTO.getId())).thenReturn(Optional.of(titulo));
        when(tituloRepository.findById(movTitulo2DTO.getId())).thenReturn(Optional.of(titulo2));
        when(contaBancariaRepository.findById(dto.getContaBancariaId())).thenReturn(Optional.of(contaBancaria));

        // When & Then - deve lançar exceção antes de persistir qualquer alteração
        // A validação ocorre no método validate() antes da criação da entidade
        // Portanto, nenhuma movimentação será criada e nenhuma alteração será persistida no banco
        BeanValidationException exception =
            assertThrows(BeanValidationException.class,
                        () -> service.mergeEntityAndDTO(null, dto));

        // Verificar que a exceção contém informação sobre o erro
        assertTrue(exception.getViolations().stream()
            .anyMatch(v -> v.getKey().contains("valorPagoUltrapassaTotal") ||
                          v.getKey().contains("valorMovimentoMaiorSaldo")));
    }

    @Test
    @DisplayName("Deve desconsiderar movimentações deletadas no cálculo do valor pago")
    void deveDesconsiderarMovimentacoesDeletedasNoCalculoDoValorPago() {
        // Given - criar um título novo
        TituloCategoria tituloCategoria =
            new TituloCategoria.Builder()
                .codigo("003")
                .nome("Categoria Teste 3")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .build();

        Titulo tituloNovo = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Título para teste soft delete")
                .pessoa(titulo.getPessoa())
                .tituloCategoria(tituloCategoria)
                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(1000.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(30))
                .build();

        // Criar primeira movimentação de R$ 300 (ativa)
        new MovimentacaoFinanceira.Builder()
                .titulos(java.util.Set.of(tituloNovo))
                .contaBancaria(contaBancaria)
                .tipo(TipoMovimentacao.PAGAMENTO)
                .formaPagamento(FormaPagamento.PIX)
                .valor(Money.of(BigDecimal.valueOf(300.00)))
                .data(LocalDate.now())
                .build();

        // Criar segunda movimentação de R$ 200 e marcar como deletada
        MovimentacaoFinanceira mov2 = new MovimentacaoFinanceira.Builder()
                .titulos(java.util.Set.of(tituloNovo))
                .contaBancaria(contaBancaria)
                .tipo(TipoMovimentacao.PAGAMENTO)
                .formaPagamento(FormaPagamento.PIX)
                .valor(Money.of(BigDecimal.valueOf(200.00)))
                .data(LocalDate.now())
                .build();

        // Simular soft delete na segunda movimentação usando reflexão
        try {
            java.lang.reflect.Field deletedField = BaseEntity.class
                .getDeclaredField("deleted");
            deletedField.setAccessible(true);
            deletedField.set(mov2, true);
        } catch (Exception e) {
            fail("Erro ao configurar soft delete: " + e.getMessage());
        }

        // When - calcular valor pago
        Money valorPago = tituloNovo.getValorPago();
        Money saldo = tituloNovo.calcularSaldo();

        // Then - deve considerar apenas a primeira movimentação (R$ 300)
        assertEquals(Money.of(BigDecimal.valueOf(300.00)), valorPago);
        // Saldo deve ser 1000 - 300 = 700
        assertEquals(Money.of(BigDecimal.valueOf(700.00)), saldo);
        // Status deve ser PARCIAL (pago algo mas não tudo)
        assertEquals(StatusTitulo.PARCIAL, tituloNovo.getStatus());
    }
}
