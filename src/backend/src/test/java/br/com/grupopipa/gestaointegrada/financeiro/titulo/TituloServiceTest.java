package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.planocontas.PlanoContasRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para TituloServiceImpl - CRUD básico.
 * Valida as regras de negócio do serviço de títulos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TituloService - Testes Unitários")
class TituloServiceTest {
    // unidadeNegocio é inicializada no setUp()

    @Mock
    private TituloRepository repository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private PlanoContasRepository planoContasRepository;

    @Mock
    private UnidadeNegocioRepository unidadeNegocioRepository;

    @Mock
    private Specifications<Titulo> specifications;

    @InjectMocks
    private TituloServiceImpl service;

    private Pessoa pessoa;
    private PlanoContas planoContas;
    private UnidadeNegocio unidadeNegocio;
    private TituloDTO dto;
    private Titulo entity;

    @BeforeEach
    void setUp() {
        // Setup unidade de negócio
        unidadeNegocio = new UnidadeNegocio.Builder()
                .codigo("UN001")
                .nome("Unidade Teste")
                .cnpj("11222333000181")
                .build();

        // Setup pessoa
        pessoa = new Pessoa.Builder()
                .tipoPessoa(br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa.JURIDICA)
                .nome("Fornecedor Teste")
                .email("fornecedor@test.com")
                .telefone("11999999999")
                .cnpj("11222333000181")
                .razaoSocial("Fornecedor LTDA")
                .build();

        // Setup plano de contas
        planoContas = new PlanoContas.Builder()
                .codigo("4.1.001")
                .descricao("Fornecedores")
                .tipo(TipoPlanoContas.DESPESA)
                .unidadeNegocio(unidadeNegocio)
                .build();

        // Setup DTO
        dto = TituloDTO.builder()
                .tipo(TipoTitulo.A_PAGAR.name())
                .descricao("Pagamento fornecedor")
                .pessoaId(UUID.randomUUID())
                .planoContasId(UUID.randomUUID())
                .valorOriginal(BigDecimal.valueOf(1000.00))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(30))
                .build();

        // Setup Entity
        entity = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Pagamento fornecedor")
                .pessoa(pessoa)
                .planoContas(planoContas)
                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(1000.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("Deve criar título a pagar")
    void deveCriarTituloAPagar() {
        // Given
        when(pessoaRepository.findById(dto.getPessoaId())).thenReturn(Optional.of(pessoa));
        when(planoContasRepository.findById(dto.getPlanoContasId())).thenReturn(Optional.of(planoContas));
        when(unidadeNegocioRepository.findById(any())).thenReturn(Optional.of(unidadeNegocio));

        // When
        Titulo resultado = service.mergeEntityAndDTO(null, dto);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoTitulo.A_PAGAR, resultado.getTipo());
        assertEquals(StatusTitulo.ABERTO, resultado.getStatus());
        assertEquals("Pagamento fornecedor", resultado.getDescricao());
        assertEquals(new Money(BigDecimal.valueOf(1000.00)), resultado.getValorOriginal());
        assertEquals(pessoa, resultado.getPessoa());
        assertEquals(planoContas, resultado.getPlanoContas());
    }

    @Test
    @DisplayName("Deve criar título a receber")
    void deveCriarTituloAReceber() {
        // Given
        dto.setTipo(TipoTitulo.A_RECEBER.name());
        PlanoContas planoReceita = new PlanoContas.Builder()
                .codigo("3.1.001")
                .descricao("Vendas")
                .tipo(TipoPlanoContas.RECEITA)
                .unidadeNegocio(unidadeNegocio)
                .build();

        when(pessoaRepository.findById(dto.getPessoaId())).thenReturn(Optional.of(pessoa));
        when(planoContasRepository.findById(dto.getPlanoContasId())).thenReturn(Optional.of(planoReceita));
        when(unidadeNegocioRepository.findById(any())).thenReturn(Optional.of(unidadeNegocio));

        // When
        Titulo resultado = service.mergeEntityAndDTO(null, dto);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoTitulo.A_RECEBER, resultado.getTipo());
        assertEquals(StatusTitulo.ABERTO, resultado.getStatus());
        assertEquals(planoReceita, resultado.getPlanoContas());
    }

    @Test
    @DisplayName("Deve atualizar título existente")
    void deveAtualizarTituloExistente() {
        // Given
        Titulo tituloExistente = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Descrição original")
                .pessoa(pessoa)
                .planoContas(planoContas)
                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(500.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(15))
                .build();

        dto.setDescricao("Descrição atualizada");
        dto.setDataVencimento(LocalDate.now().plusDays(45));

        // When
        Titulo resultado = service.mergeEntityAndDTO(tituloExistente, dto);

        // Then
        assertNotNull(resultado);
        assertEquals("Descrição atualizada", resultado.getDescricao());
        assertEquals(LocalDate.now().plusDays(45), resultado.getDataVencimento());
    }

    @Test
    @DisplayName("Deve buscar título por ID")
    void deveBuscarTituloPorId() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entity));

        // When
        TituloDTO resultado = service.findById(id);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoTitulo.A_PAGAR.name(), resultado.getTipo());
        assertEquals("Pagamento fornecedor", resultado.getDescricao());
        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve salvar título")
    void deveSalvarTitulo() {
        // Given
        when(pessoaRepository.findById(dto.getPessoaId())).thenReturn(Optional.of(pessoa));
        when(planoContasRepository.findById(dto.getPlanoContasId())).thenReturn(Optional.of(planoContas));
        when(unidadeNegocioRepository.findById(any())).thenReturn(Optional.of(unidadeNegocio));
        when(repository.save(any(Titulo.class))).thenReturn(entity);

        // When
        TituloDTO resultado = service.save(dto);

        // Then
        assertNotNull(resultado);
        verify(repository, times(1)).save(any(Titulo.class));
    }

    @Test
    @DisplayName("Deve deletar título")
    void deveDeletarTitulo() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        UUID resultado = service.delete(id);

        // Then
        assertEquals(id, resultado);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar título inexistente")
    void deveLancarExcecaoAoBuscarTituloInexistente() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> service.findById(id));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar título sem pessoa")
    void deveLancarExcecaoAoCriarTituloSemPessoa() {
        // Given
        when(pessoaRepository.findById(dto.getPessoaId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> service.mergeEntityAndDTO(null, dto));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar título sem plano de contas")
    void deveLancarExcecaoAoCriarTituloSemPlanoContas() {
        // Given
        when(pessoaRepository.findById(dto.getPessoaId())).thenReturn(Optional.of(pessoa));
        when(planoContasRepository.findById(dto.getPlanoContasId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> service.mergeEntityAndDTO(null, dto));
    }
}
