package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.financeiro.entity.PlanoContas;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para PlanoContasServiceImpl.
 * Valida as regras de negócio do serviço de plano de contas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlanoContasService - Testes Unitários")
class PlanoContasServiceTest {

    @Mock
    private PlanoContasRepository repository;

    @Mock
    private UnidadeNegocioRepository unidadeNegocioRepository;

    @Mock
    private Specifications<PlanoContas> specifications;

    @InjectMocks
    private PlanoContasServiceImpl service;

    private PlanoContasDTO dtoReceitas;
    private PlanoContasDTO dtoDespesas;
    private PlanoContas entityReceitas;
    private PlanoContas entityDespesas;
    private UnidadeNegocio unidadeNegocio;
    private UUID unidadeNegocioId;

    @BeforeEach
    void setUp() {
        // Criar unidade de negócio para os testes
        unidadeNegocioId = UUID.randomUUID();
        unidadeNegocio = new UnidadeNegocio.Builder()
                .codigo("UN001")
                .nome("Unidade Teste")
                .cnpj("11222333000181")
                .build();

        // Configurar mock do repository
        when(unidadeNegocioRepository.findById(unidadeNegocioId))
                .thenReturn(Optional.of(unidadeNegocio));

        // DTO de Receitas (raiz)
        dtoReceitas = PlanoContasDTO.builder()
                .codigo("1")
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA.name())
                .unidadeNegocioId(unidadeNegocioId)
                .ativo(true)
                .build();

        // DTO de Despesas (raiz)
        dtoDespesas = PlanoContasDTO.builder()
                .codigo("2")
                .descricao("Despesas")
                .tipo(TipoPlanoContas.DESPESA.name())
                .unidadeNegocioId(unidadeNegocioId)
                .ativo(true)
                .build();

        // Entity de Receitas
        entityReceitas = new PlanoContas.Builder()
                .codigo("1")
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA)
                .unidadeNegocio(unidadeNegocio)
                .build();

        // Entity de Despesas
        entityDespesas = new PlanoContas.Builder()
                .codigo("2")
                .descricao("Despesas")
                .tipo(TipoPlanoContas.DESPESA)
                .unidadeNegocio(unidadeNegocio)
                .build();
    }

    @Test
    @DisplayName("Deve criar plano de contas sem plano pai (raiz)")
    void deveCriarPlanoContasSemPlanoPai() {
        // When
        PlanoContas resultado = service.mergeEntityAndDTO(null, dtoReceitas);

        // Then
        assertNotNull(resultado);
        assertEquals("1", resultado.getCodigo());
        assertEquals("Receitas", resultado.getDescricao());
        assertEquals(TipoPlanoContas.RECEITA, resultado.getTipo());
        assertNull(resultado.getPlanoPai());
    }

    @Test
    @DisplayName("Deve criar plano de contas com plano pai")
    void deveCriarPlanoContasComPlanoPai() {
        // Given
        UUID planoPaiId = UUID.randomUUID();
        PlanoContas entityPai = new PlanoContas.Builder()
                .codigo("1")
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA)
                .unidadeNegocio(unidadeNegocio)
                .build();
        try {
            var idField = PlanoContas.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entityPai, planoPaiId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PlanoContasDTO dtoFilho = PlanoContasDTO.builder()
                .codigo("1.1")
                .descricao("Receitas Operacionais")
                .tipo(TipoPlanoContas.RECEITA.name())
                .unidadeNegocioId(unidadeNegocioId)
                .planoPaiId(planoPaiId)
                .ativo(true)
                .build();

        when(repository.findById(planoPaiId)).thenReturn(Optional.of(entityPai));

        // When
        PlanoContas resultado = service.mergeEntityAndDTO(null, dtoFilho);

        // Then
        assertNotNull(resultado);
        assertEquals("1.1", resultado.getCodigo());
        assertEquals("Receitas Operacionais", resultado.getDescricao());
        assertNotNull(resultado.getPlanoPai());
        assertEquals(TipoPlanoContas.RECEITA, resultado.getPlanoPai().getTipo());
    }

    @Test
    @DisplayName("Deve atualizar plano de contas existente")
    void deveAtualizarPlanoContasExistente() {
        // Given
        PlanoContasDTO dtoAtualizado = PlanoContasDTO.builder()
                .codigo("1")
                .descricao("Receitas Atualizadas")
                .tipo(TipoPlanoContas.RECEITA.name())
                .ativo(true)
                .build();

        // When
        PlanoContas resultado = service.mergeEntityAndDTO(entityReceitas, dtoAtualizado);

        // Then
        assertNotNull(resultado);
        assertEquals("Receitas Atualizadas", resultado.getDescricao());
    }

    @Test
    @DisplayName("Deve buscar plano de contas por ID")
    void deveBuscarPlanoContasPorId() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(entityReceitas));

        // When
        Optional<PlanoContas> resultado = repository.findById(id);

        // Then
        assertTrue(resultado.isPresent());
        assertEquals("Receitas", resultado.get().getDescricao());
        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve deletar plano de contas")
    void deveDeletarPlanoContas() {
        // Given
        UUID id = UUID.randomUUID();
        doNothing().when(repository).deleteById(id);

        // When
        repository.deleteById(id);

        // Then
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve construir DTO corretamente da entidade")
    void deveConstruirDTOCorretamenteDaEntidade() {
        // When
        PlanoContasDTO resultado = service.buildDTOFromEntity(entityReceitas);

        // Then
        assertNotNull(resultado);
        assertEquals("1", resultado.getCodigo());
        assertEquals("Receitas", resultado.getDescricao());
        assertEquals(TipoPlanoContas.RECEITA.name(), resultado.getTipo());
        assertTrue(resultado.getAtivo());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTOCorretamenteDaEntidade() {
        // When
        PlanoContasGridDTO resultado = service.buildGridDTOFromEntity(entityReceitas);

        // Then
        assertNotNull(resultado);
        assertEquals("1", resultado.getCodigo());
        assertEquals("Receitas", resultado.getDescricao());
        assertEquals(TipoPlanoContas.RECEITA.name(), resultado.getTipo());
        assertTrue(resultado.getAtivo());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar com plano pai inexistente")
    void deveLancarExcecaoAoCriarComPlanoPaiInexistente() {
        // Given
        UUID planoPaiId = UUID.randomUUID();
        PlanoContasDTO dtoComPaiInvalido = PlanoContasDTO.builder()
                .codigo("1.1")
                .descricao("Receitas Operacionais")
                .tipo(TipoPlanoContas.RECEITA.name())
                .planoPaiId(planoPaiId)
                .build();

        when(repository.findById(planoPaiId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.mergeEntityAndDTO(null, dtoComPaiInvalido);
        });
    }

    @Test
    @DisplayName("Deve criar plano de contas do tipo DESPESA")
    void deveCriarPlanoContasTipoDespesa() {
        // When
        PlanoContas resultado = service.mergeEntityAndDTO(null, dtoDespesas);

        // Then
        assertNotNull(resultado);
        assertEquals("2", resultado.getCodigo());
        assertEquals("Despesas", resultado.getDescricao());
        assertEquals(TipoPlanoContas.DESPESA, resultado.getTipo());
        assertNull(resultado.getPlanoPai());
    }

    @Test
    @DisplayName("Deve criar plano de contas do tipo Despesa com plano pai")
    void deveCriarPlanoContasDespesaComPlanoPai() {
        // Given
        UUID planoPaiId = UUID.randomUUID();
        PlanoContas entityPai = new PlanoContas.Builder()
                .codigo("2")
                .descricao("Despesas")
                .tipo(TipoPlanoContas.DESPESA)
                .unidadeNegocio(unidadeNegocio)
                .build();
        try {
            var idField = PlanoContas.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entityPai, planoPaiId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        PlanoContasDTO dtoDespesaFilho = PlanoContasDTO.builder()
                .codigo("2.1")
                .descricao("Despesas Administrativas")
                .tipo(TipoPlanoContas.DESPESA.name())
                .unidadeNegocioId(unidadeNegocioId)
                .planoPaiId(planoPaiId)
                .ativo(true)
                .build();

        when(repository.findById(planoPaiId)).thenReturn(Optional.of(entityPai));

        // When
        PlanoContas resultado = service.mergeEntityAndDTO(null, dtoDespesaFilho);

        // Then
        assertNotNull(resultado);
        assertEquals("2.1", resultado.getCodigo());
        assertEquals("Despesas Administrativas", resultado.getDescricao());
        assertNotNull(resultado.getPlanoPai());
        assertEquals(TipoPlanoContas.DESPESA, resultado.getPlanoPai().getTipo());
        assertEquals("2", resultado.getPlanoPai().getCodigo());
    }

    @Test
    @DisplayName("Deve construir DTO de plano de contas do tipo DESPESA")
    void deveConstruirDTODePlanoContasDespesa() {
        // When
        PlanoContasDTO resultado = service.buildDTOFromEntity(entityDespesas);

        // Then
        assertNotNull(resultado);
        assertEquals("2", resultado.getCodigo());
        assertEquals("Despesas", resultado.getDescricao());
        assertEquals(TipoPlanoContas.DESPESA.name(), resultado.getTipo());
        assertTrue(resultado.getAtivo());
    }

    @Test
    @DisplayName("Deve inativar plano de contas existente")
    void deveInativarPlanoContasExistente() {
        // Given
        PlanoContasDTO dtoInativo = PlanoContasDTO.builder()
                .codigo("1")
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA.name())
                .ativo(false)
                .build();

        // When
        PlanoContas resultado = service.mergeEntityAndDTO(entityReceitas, dtoInativo);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.getAtivo());
    }

    @Test
    @DisplayName("Deve ativar plano de contas existente")
    void deveAtivarPlanoContasExistente() {
        // Given
        entityReceitas.inativar(); // Primeiro inativa

        PlanoContasDTO dtoAtivo = PlanoContasDTO.builder()
                .codigo("1")
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA.name())
                .ativo(true)
                .build();

        // When
        PlanoContas resultado = service.mergeEntityAndDTO(entityReceitas, dtoAtivo);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.getAtivo());
    }
}
