package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;

/**
 * Testes unitários para UnidadeNegocioService. Usa Mockito para simular dependências (repository).
 */
@DisplayName("UnidadeNegocioService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class UnidadeNegocioServiceTest {

  @Mock private UnidadeNegocioRepository repository;

  @Mock private Specifications<UnidadeNegocio> specifications;

  @InjectMocks private UnidadeNegocioServiceImpl service;

  private UnidadeNegocioDTO dtoValido;
  private UnidadeNegocio entidadeValida;

  @BeforeEach
  void setup() {
    dtoValido =
        UnidadeNegocioDTO.builder()
            .codigo("UN001")
            .nome("Unidade Teste")
            .descricao("Descrição da unidade")
            .ativa(true)
            .build();

    entidadeValida =
        new UnidadeNegocio.Builder()
            .codigo("UN001")
            .nome("Unidade Teste")
            .descricao("Descrição da unidade")
            .build();
  }

  @Test
  @DisplayName("Deve criar nova unidade de negócio")
  void deveCriarNovaUnidadeNegocio() {
    // Given
    when(repository.save(any(UnidadeNegocio.class)))
        .thenAnswer(
            invocation -> {
              UnidadeNegocio entity = invocation.getArgument(0);
              // Simula o ID gerado pelo banco
              return entity;
            });

    // When
    UnidadeNegocioDTO resultado = service.save(dtoValido);

    // Then
    assertNotNull(resultado);
    assertEquals("UN001", resultado.getCodigo());
    assertEquals("Unidade Teste", resultado.getNome());
    assertEquals("Descrição da unidade", resultado.getDescricao());
    assertTrue(resultado.getAtiva());

    verify(repository, times(1)).save(any(UnidadeNegocio.class));
  }

  @Test
  @DisplayName("Deve atualizar unidade de negócio existente")
  void deveAtualizarUnidadeNegocioExistente() {
    // Given
    UUID id = UUID.randomUUID();
    dtoValido.setId(id);
    dtoValido.setNome("Unidade Atualizada");
    dtoValido.setDescricao("Nova descrição");

    when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));
    when(repository.save(any(UnidadeNegocio.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    UnidadeNegocioDTO resultado = service.save(dtoValido);

    // Then
    assertNotNull(resultado);
    assertEquals("Unidade Atualizada", resultado.getNome());
    assertEquals("Nova descrição", resultado.getDescricao());

    verify(repository, times(1)).findById(id);
    verify(repository, times(1)).save(any(UnidadeNegocio.class));
  }

  @Test
  @DisplayName("Deve inativar unidade de negócio")
  void deveInativarUnidadeNegocio() {
    // Given
    UUID id = UUID.randomUUID();
    dtoValido.setId(id);
    dtoValido.setAtiva(false);

    when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));
    when(repository.save(any(UnidadeNegocio.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    UnidadeNegocioDTO resultado = service.save(dtoValido);

    // Then
    assertNotNull(resultado);
    assertFalse(resultado.getAtiva());

    verify(repository, times(1)).save(any(UnidadeNegocio.class));
  }

  @Test
  @DisplayName("Deve ativar unidade de negócio")
  void deveAtivarUnidadeNegocio() {
    // Given
    UUID id = UUID.randomUUID();
    entidadeValida.inativar(); // Começa inativa
    dtoValido.setId(id);
    dtoValido.setAtiva(true);

    when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));
    when(repository.save(any(UnidadeNegocio.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    UnidadeNegocioDTO resultado = service.save(dtoValido);

    // Then
    assertNotNull(resultado);
    assertTrue(resultado.getAtiva());

    verify(repository, times(1)).save(any(UnidadeNegocio.class));
  }

  @Test
  @DisplayName("Deve buscar unidade de negócio por ID")
  void deveBuscarUnidadeNegocioPorId() {
    // Given
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.of(entidadeValida));

    // When
    UnidadeNegocioDTO resultado = service.findById(id);

    // Then
    assertNotNull(resultado);
    assertEquals("UN001", resultado.getCodigo());
    assertEquals("Unidade Teste", resultado.getNome());

    verify(repository, times(1)).findById(id);
  }

  @Test
  @DisplayName("Deve deletar unidade de negócio")
  void deveDeletarUnidadeNegocio() {
    // Given
    UUID id = UUID.randomUUID();
    doNothing().when(repository).deleteById(id);

    // When
    UUID resultadoId = service.delete(id);

    // Then
    assertEquals(id, resultadoId);
    verify(repository, times(1)).deleteById(id);
  }

  @Test
  @DisplayName("Deve construir DTO corretamente da entidade")
  void deveConstruirDTOCorretamenteDaEntidade() {
    // When
    UnidadeNegocioDTO dto = service.buildDTOFromEntity(entidadeValida);

    // Then
    assertNotNull(dto);
    assertEquals("UN001", dto.getCodigo());
    assertEquals("Unidade Teste", dto.getNome());
    assertEquals("Descrição da unidade", dto.getDescricao());
    assertTrue(dto.getAtiva());
  }

  @Test
  @DisplayName("Deve construir GridDTO corretamente da entidade")
  void deveConstruirGridDTOCorretamenteDaEntidade() {
    // When
    UnidadeNegocioGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);

    // Then
    assertNotNull(gridDTO);
    assertEquals("UN001", gridDTO.getCodigo());
    assertEquals("Unidade Teste", gridDTO.getNome());
    assertTrue(gridDTO.getAtiva());
  }

  @Test
  @DisplayName("Deve fazer merge de entidade nova com DTO")
  void deveFazerMergeDeEntidadeNovaComDTO() {
    // When
    UnidadeNegocio resultado = service.mergeEntityAndDTO(null, dtoValido);

    // Then
    assertNotNull(resultado);
    assertEquals("UN001", resultado.getCodigo());
    assertEquals("Unidade Teste", resultado.getNome());
    assertEquals("Descrição da unidade", resultado.getDescricao());
  }

  @Test
  @DisplayName("Deve fazer merge de entidade existente com DTO")
  void deveFazerMergeDeEntidadeExistenteComDTO() {
    // Given
    dtoValido.setNome("Nome Atualizado");
    dtoValido.setDescricao("Descrição Atualizada");

    // When
    UnidadeNegocio resultado = service.mergeEntityAndDTO(entidadeValida, dtoValido);

    // Then
    assertNotNull(resultado);
    assertEquals("UN001", resultado.getCodigo()); // Código não muda
    assertEquals("Nome Atualizado", resultado.getNome());
    assertEquals("Descrição Atualizada", resultado.getDescricao());
  }
}
