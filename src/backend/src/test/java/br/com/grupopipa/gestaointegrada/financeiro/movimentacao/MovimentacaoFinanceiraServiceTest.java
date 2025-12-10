package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.contabancaria.ContaBancariaRepository;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.entity.MovimentacaoFinanceira;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloRepository;
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

        // Setup plano de contas
        PlanoContas planoContas = new PlanoContas.Builder()
                .codigo("4.1.001")
                .descricao("Fornecedores")
                .tipo(TipoPlanoContas.DESPESA)
                .unidadeNegocio(unidadeNegocio)
                .build();

        // Setup título
        titulo = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Pagamento fornecedor")
                .pessoa(pessoa)
                .planoContas(planoContas)
                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(1000.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(30))
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
        assertEquals(new Money(BigDecimal.valueOf(500.00)), resultado.getValor());
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

        // When
        UUID resultado = service.delete(id);

        // Then
        assertEquals(id, resultado);
        verify(repository, times(1)).deleteById(id);
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
}
