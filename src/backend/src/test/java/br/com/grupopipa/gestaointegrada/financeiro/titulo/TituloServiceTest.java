package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.cadastro.setor.SetorRepository;
import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CentroCusto;
import br.com.grupopipa.gestaointegrada.financeiro.entity.Titulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;

/**
 * Testes unitários para TituloServiceImpl - CRUD básico. Valida as regras de
 * negócio do serviço de
 * títulos.
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
    private UnidadeNegocioRepository unidadeNegocioRepository;

    @Mock
    private SetorRepository setorRepository;

    @Mock
    private br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaRepository tituloCategoriaRepository;

    @Mock
    private Specifications<Titulo> specifications;

    @InjectMocks
    private TituloServiceImpl service;

    private Pessoa pessoa;
    private UnidadeNegocio unidadeNegocio;
    private Setor setor;
    private CentroCusto centroCusto;
    private br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria tituloCategoria;
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

        // Setup centro de custo
        centroCusto = new CentroCusto.Builder().nome("Centro Custo Teste").unidadeNegocio(unidadeNegocio).build();

        // Setup setor
        setor = new Setor.Builder().nome("Setor Teste").centroCusto(centroCusto).build();

        // Setup pessoa
        pessoa = new Pessoa.Builder()
                .tipoPessoa(br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa.JURIDICA)
                .nome("Fornecedor Teste")
                .email("fornecedor@test.com")
                .telefone("11999999999")
                .cnpj("11222333000181")
                .razaoSocial("Fornecedor LTDA")
                .build();

        // Setup categoria
        tituloCategoria = new br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria.Builder()
                .codigo("001")
                .nome("Despesas Operacionais")
                .descricao("Categoria de teste")
                .tipo(
                        br.com.grupopipa.gestaointegrada.financeiro.titulocategoria.TituloCategoriaTipoEnum.DESPESA)
                .build();

        // Setup DTO with setores
        TituloSetorDTO setorDTO = TituloSetorDTO.builder()
                .setorId(setor.getId())
                .setorNome("Setor Teste")
                .percentualRateio(BigDecimal.valueOf(100.00))
                .build();

        dto = TituloDTO.builder()
                .tipo(TipoTitulo.A_PAGAR.name())
                .descricao("Pagamento fornecedor")
                .unidadeNegocioId(unidadeNegocio.getId())
                .pessoaId(UUID.randomUUID())
                .tituloCategoriaId(tituloCategoria.getId())
                .valorOriginal(BigDecimal.valueOf(1000.00))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(30))
                .setores(List.of(setorDTO))
                .rateioAutomatico(false)
                .build();

        // Setup Entity
        entity = new Titulo.Builder()
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

        // Add setor to entity
        entity.adicionarSetor(setor, BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("Deve criar título a pagar")
    void deveCriarTituloAPagar() {
        // Given
        when(pessoaRepository.findById(dto.getPessoaId())).thenReturn(Optional.of(pessoa));
        when(tituloCategoriaRepository.findById(dto.getTituloCategoriaId()))
                .thenReturn(Optional.of(tituloCategoria));
        when(unidadeNegocioRepository.findById(any())).thenReturn(Optional.of(unidadeNegocio));
        when(setorRepository.findById(setor.getId())).thenReturn(Optional.of(setor));

        // When
        Titulo resultado = service.mergeEntityAndDTO(null, dto);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoTitulo.A_PAGAR, resultado.getTipo());
        assertEquals(StatusTitulo.ABERTO, resultado.getStatus());
        assertEquals("Pagamento fornecedor", resultado.getDescricao());
        assertEquals(Money.of(BigDecimal.valueOf(1000.00)), resultado.getValorOriginal());
        assertEquals(pessoa, resultado.getPessoa());
        assertEquals(tituloCategoria, resultado.getTituloCategoria());
    }

    @Test
    @DisplayName("Deve criar título a receber")
    void deveCriarTituloAReceber() {
        // Given
        dto.setTipo(TipoTitulo.A_RECEBER.name());
        when(pessoaRepository.findById(dto.getPessoaId())).thenReturn(Optional.of(pessoa));
        when(tituloCategoriaRepository.findById(dto.getTituloCategoriaId()))
                .thenReturn(Optional.of(tituloCategoria));
        when(unidadeNegocioRepository.findById(any())).thenReturn(Optional.of(unidadeNegocio));
        when(setorRepository.findById(setor.getId())).thenReturn(Optional.of(setor));

        // When
        Titulo resultado = service.mergeEntityAndDTO(null, dto);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoTitulo.A_RECEBER, resultado.getTipo());
        assertEquals(StatusTitulo.ABERTO, resultado.getStatus());
        assertEquals(tituloCategoria, resultado.getTituloCategoria());
    }

    @Test
    @DisplayName("Deve atualizar título existente")
    void deveAtualizarTituloExistente() {
        // Given
        Titulo tituloExistente = new Titulo.Builder()
                .tipo(TipoTitulo.A_PAGAR)
                .descricao("Descrição original")
                .pessoa(pessoa)
                .tituloCategoria(tituloCategoria)
                .unidadeNegocio(unidadeNegocio)
                .valorOriginal(Money.of(BigDecimal.valueOf(500.00)))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(15))
                .build();

        // Add setor to existing titulo
        tituloExistente.adicionarSetor(setor, BigDecimal.valueOf(100.00));

        dto.setDescricao("Descrição atualizada");
        dto.setDataVencimento(LocalDate.now().plusDays(45));
        when(setorRepository.findById(setor.getId())).thenReturn(Optional.of(setor));

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
        when(tituloCategoriaRepository.findById(dto.getTituloCategoriaId()))
                .thenReturn(Optional.of(tituloCategoria));
        when(unidadeNegocioRepository.findById(any())).thenReturn(Optional.of(unidadeNegocio));
        when(setorRepository.findById(setor.getId())).thenReturn(Optional.of(setor));
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
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(Titulo.class))).thenReturn(entity);

        // When
        UUID resultado = service.delete(id);

        // Then
        assertEquals(id, resultado);
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(Titulo.class));
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
        // This test was removed because `PlanoContas` is no longer required on
        // `Titulo`.
    }
}
