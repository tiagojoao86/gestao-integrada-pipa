package br.com.grupopipa.gestaointegrada.financeiro.centrocusto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.core.exception.EntityNotFoundException;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CentroCusto;

@DisplayName("CentroCustoService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class CentroCustoServiceTest {

    @Mock
    private CentroCustoRepository repository;

    @Mock
    private UnidadeNegocioRepository unidadeNegocioRepository;

    @Mock
    private Specifications<CentroCusto> specifications;

    @InjectMocks
    private CentroCustoServiceImpl service;

    private CentroCustoDTO dtoValido;
    private CentroCusto entidadeValida;
    private UnidadeNegocio unidadeNegocio;
    private UUID centroId;
    private UUID unidadeId;

    @BeforeEach
    void setup() {
        centroId = UUID.randomUUID();
        unidadeId = UUID.randomUUID();

        // Criar UnidadeNegocio mock
        unidadeNegocio = new UnidadeNegocio.Builder()
                .codigo("UN_TEST")
                .nome("Unidade Teste")
                .cnpj("11222333000181")
                .build();
        // Usar reflexão para setar o ID na entidade
        try {
            java.lang.reflect.Field idField = UnidadeNegocio.class.getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(unidadeNegocio, unidadeId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // DTO válido
        dtoValido = CentroCustoDTO.builder()
                .nome("Centro Teste")
                .centroResultado(Boolean.FALSE)
                .unidadeNegocioId(unidadeId)
                .build();

        // Entidade válida
        entidadeValida = new CentroCusto.Builder()
                .nome("Centro Teste")
                .centroResultado(Boolean.FALSE)
                .unidadeNegocio(unidadeNegocio)
                .build();
    }

    @Test
    @DisplayName("Deve criar novo centro de custo")
    void deveCriarNovoCentroCusto() {
        when(unidadeNegocioRepository.findById(unidadeId)).thenReturn(Optional.of(unidadeNegocio));
        when(repository.save(any(CentroCusto.class))).thenReturn(entidadeValida);

        CentroCustoDTO resultado = service.save(dtoValido);

        assertNotNull(resultado);
        assertEquals("Centro Teste", resultado.getNome());
        verify(repository, times(1)).save(any(CentroCusto.class));
        verify(unidadeNegocioRepository, times(1)).findById(unidadeId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando UnidadeNegocio não existe")
    void deveLancarExcecaoQuandoUnidadeNaoExiste() {
        UUID unidadeInexistente = UUID.randomUUID();
        when(unidadeNegocioRepository.findById(unidadeInexistente)).thenReturn(Optional.empty());

        CentroCustoDTO dtoInvalido = CentroCustoDTO.builder()
                .nome("Centro Teste")
                .centroResultado(Boolean.FALSE)
                .unidadeNegocioId(unidadeInexistente)
                .build();

        assertThrows(EntityNotFoundException.class, () -> service.save(dtoInvalido));
        verify(unidadeNegocioRepository, times(1)).findById(unidadeInexistente);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar centro de custo por id")
    void deveBuscarCentroPorId() {
        when(repository.findById(centroId)).thenReturn(Optional.of(entidadeValida));

        CentroCustoDTO resultado = service.findById(centroId);

        assertNotNull(resultado);
        assertEquals("Centro Teste", resultado.getNome());
        assertEquals(unidadeId, resultado.getUnidadeNegocioId());
        assertEquals("Unidade Teste", resultado.getUnidadeNegocioNome());
        assertEquals("UN_TEST", resultado.getUnidadeNegocioCodigo());
        verify(repository, times(1)).findById(centroId);
    }

    @Test
    @DisplayName("Deve deletar centro de custo")
    void deveDeletarCentro() {
        doNothing().when(repository).deleteById(centroId);

        UUID resultado = service.delete(centroId);

        assertEquals(centroId, resultado);
        verify(repository, times(1)).deleteById(centroId);
    }

    @Test
    @DisplayName("Deve construir DTO corretamente da entidade")
    void deveConstruirDTOCorretamenteDaEntidade() {
        CentroCustoDTO dto = service.buildDTOFromEntity(entidadeValida);

        assertNotNull(dto);
        assertEquals("Centro Teste", dto.getNome());
        assertFalse(dto.getCentroResultado());
        assertEquals("Unidade Teste", dto.getUnidadeNegocioNome());
        assertEquals("UN_TEST", dto.getUnidadeNegocioCodigo());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTOCorretamenteDaEntidade() {
        CentroCustoGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);

        assertNotNull(gridDTO);
        assertEquals("Centro Teste", gridDTO.getNome());
        assertFalse(gridDTO.getCentroResultado());
        assertEquals("UN_TEST", gridDTO.getUnidadeNegocioCodigo());
    }

    @Test
    @DisplayName("Deve atualizar centro de custo existente")
    void deveAtualizarCentroExistente() {
        CentroCusto entidadeExistente = new CentroCusto.Builder()
                .nome("Nome Antigo")
                .centroResultado(Boolean.TRUE)
                .unidadeNegocio(unidadeNegocio)
                .build();

        when(unidadeNegocioRepository.findById(unidadeId)).thenReturn(Optional.of(unidadeNegocio));
        when(repository.findById(centroId)).thenReturn(Optional.of(entidadeExistente));
        when(repository.save(any(CentroCusto.class))).thenReturn(entidadeExistente);

        CentroCustoDTO dtoAtualizado = CentroCustoDTO.builder()
                .id(centroId)
                .nome("Nome Novo")
                .centroResultado(Boolean.FALSE)
                .unidadeNegocioId(unidadeId)
                .build();

        CentroCustoDTO resultado = service.save(dtoAtualizado);

        assertNotNull(resultado);
        verify(repository, times(1)).findById(centroId);
        verify(repository, times(1)).save(any(CentroCusto.class));
        verify(unidadeNegocioRepository, times(1)).findById(unidadeId);
    }
}
