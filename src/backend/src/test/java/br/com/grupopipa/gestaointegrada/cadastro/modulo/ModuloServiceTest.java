package br.com.grupopipa.gestaointegrada.cadastro.modulo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.grupopipa.gestaointegrada.cadastro.modulo.entity.ModuloEntity;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;

/**
 * Testes unitários para ModuloService. Usa Mockito para simular dependências
 * (repository).
 *
 * <p>
 * Nota: Modulo é read-only, não suporta save/delete.
 */
@DisplayName("ModuloService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class ModuloServiceTest {

    @Mock
    private ModuloRepository repository;

    @Mock
    private Specifications<ModuloEntity> specifications;

    @InjectMocks
    private ModuloServiceImpl service;

    @Test
    @DisplayName("Deve listar todos os módulos")
    void deveListarTodosModulos() {
        // Given
        ModuloEntity modulo1 = mock(ModuloEntity.class);
        ModuloEntity modulo2 = mock(ModuloEntity.class);

        when(modulo1.getId()).thenReturn(UUID.randomUUID());
        when(modulo1.getChave()).thenReturn("CADASTRO_PESSOA");
        when(modulo1.getNome()).thenReturn("Cadastro de Pessoas");
        when(modulo1.getGrupo()).thenReturn(GrupoModuloEnum.CADASTROS);

        when(modulo2.getId()).thenReturn(UUID.randomUUID());
        when(modulo2.getChave()).thenReturn("FINANCEIRO_TITULO");
        when(modulo2.getNome()).thenReturn("Títulos Financeiros");
        when(modulo2.getGrupo()).thenReturn(GrupoModuloEnum.FINANCEIRO);

        when(repository.findAll()).thenReturn(List.of(modulo1, modulo2));

        // When
        List<ModuloDTO> resultado = service.findAllSimple();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("CADASTRO_PESSOA", resultado.get(0).getChave());
        assertEquals("Cadastro de Pessoas", resultado.get(0).getNome());
        assertEquals(GrupoModuloEnum.CADASTROS, resultado.get(0).getGrupoEnum());
        assertEquals("FINANCEIRO_TITULO", resultado.get(1).getChave());
        assertEquals(GrupoModuloEnum.FINANCEIRO, resultado.get(1).getGrupoEnum());

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve buscar módulo por ID")
    void deveBuscarModuloPorId() {
        // Given
        UUID id = UUID.randomUUID();
        ModuloEntity modulo = mock(ModuloEntity.class);

        when(modulo.getId()).thenReturn(id);
        when(modulo.getChave()).thenReturn("CADASTRO_UNIDADE_NEGOCIO");
        when(modulo.getNome()).thenReturn("Unidades de Negócio");
        when(modulo.getGrupo()).thenReturn(GrupoModuloEnum.CADASTROS);

        when(repository.findById(id)).thenReturn(Optional.of(modulo));

        // When
        ModuloDTO resultado = service.findById(id);

        // Then
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("CADASTRO_UNIDADE_NEGOCIO", resultado.getChave());
        assertEquals("Unidades de Negócio", resultado.getNome());
        assertEquals(GrupoModuloEnum.CADASTROS, resultado.getGrupoEnum());

        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve construir DTO corretamente da entidade")
    void deveConstruirDTOCorretamenteDaEntidade() {
        // Given
        ModuloEntity modulo = mock(ModuloEntity.class);
        UUID id = UUID.randomUUID();

        when(modulo.getId()).thenReturn(id);
        when(modulo.getChave()).thenReturn("FINANCEIRO_PLANO_CONTAS");
        when(modulo.getNome()).thenReturn("Plano de Contas");
        when(modulo.getGrupo()).thenReturn(GrupoModuloEnum.FINANCEIRO);

        // When
        ModuloDTO dto = service.buildDTOFromEntity(modulo);

        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("FINANCEIRO_PLANO_CONTAS", dto.getChave());
        assertEquals("Plano de Contas", dto.getNome());
        assertEquals(GrupoModuloEnum.FINANCEIRO, dto.getGrupoEnum());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente da entidade")
    void deveConstruirGridDTOCorretamenteDaEntidade() {
        // Given
        ModuloEntity modulo = mock(ModuloEntity.class);
        UUID id = UUID.randomUUID();

        when(modulo.getId()).thenReturn(id);
        when(modulo.getNome()).thenReturn("Cadastro de Pessoas");

        // When
        ModuloGridDTO gridDTO = service.buildGridDTOFromEntity(modulo);

        // Then
        assertNotNull(gridDTO);
        assertEquals(id, gridDTO.getId());
        assertEquals("Cadastro de Pessoas", gridDTO.getNome());
    }

    @Test
    @DisplayName("Deve salvar módulo - deve lançar UnsupportedOperationException")
    void deveLancarExcecaoAoSalvar() {
        // Given
        ModuloDTO dto = ModuloDTO.builder()
                .chave("TESTE")
                .nome("Teste")
                .grupoEnum(GrupoModuloEnum.CADASTROS)
                .build();

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> service.save(dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver módulos")
    void deveRetornarListaVaziaQuandoNaoHouverModulos() {
        // Given
        when(repository.findAll()).thenReturn(List.of());

        // When
        List<ModuloDTO> resultado = service.findAllSimple();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve construir DTO com grupo null")
    void deveConstruirDTOComGrupoNull() {
        // Given
        ModuloEntity modulo = mock(ModuloEntity.class);
        UUID id = UUID.randomUUID();

        when(modulo.getId()).thenReturn(id);
        when(modulo.getChave()).thenReturn("MODULO_SEM_GRUPO");
        when(modulo.getNome()).thenReturn("Módulo Sem Grupo");
        when(modulo.getGrupo()).thenReturn(null);

        // When
        ModuloDTO dto = service.buildDTOFromEntity(modulo);

        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("MODULO_SEM_GRUPO", dto.getChave());
        assertNull(dto.getGrupoEnum());
    }
}
