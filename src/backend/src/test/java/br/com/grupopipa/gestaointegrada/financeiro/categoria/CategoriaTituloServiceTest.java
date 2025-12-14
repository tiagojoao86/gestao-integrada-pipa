package br.com.grupopipa.gestaointegrada.financeiro.categoria;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CategoriaTitulo;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaTituloService - Testes Unitários")
class CategoriaTituloServiceTest {

    @Mock
    private CategoriaTituloRepository repository;

    @Mock
    private Specifications<CategoriaTitulo> specifications;

    @InjectMocks
    private CategoriaTituloServiceImpl service;

    private CategoriaTituloDTO dto;
    private CategoriaTitulo entity;
    private UUID categoriaId;

    @BeforeEach
    void setup() {
        categoriaId = UUID.randomUUID();

        dto = CategoriaTituloDTO.builder()
                .nome("Categoria Teste")
                .descricao("Descrição categoria")
                .build();

        entity = new CategoriaTitulo.Builder()
                .nome("Categoria Teste")
                .descricao("Descrição categoria")
                .build();
    }

    @Test
    @DisplayName("Deve criar nova categoria")
    void deveCriarNovaCategoria() {
        when(repository.save(any(CategoriaTitulo.class))).thenReturn(entity);

        CategoriaTituloDTO resultado = service.save(dto);

        assertNotNull(resultado);
        assertEquals("Categoria Teste", resultado.getNome());
        verify(repository, times(1)).save(any(CategoriaTitulo.class));
    }

    @Test
    @DisplayName("Deve buscar categoria por id")
    void deveBuscarCategoriaPorId() {
        when(repository.findById(categoriaId)).thenReturn(Optional.of(entity));

        CategoriaTituloDTO resultado = service.findById(categoriaId);

        assertNotNull(resultado);
        assertEquals("Categoria Teste", resultado.getNome());
        verify(repository, times(1)).findById(categoriaId);
    }

    @Test
    @DisplayName("Deve deletar categoria")
    void deveDeletarCategoria() {
        doNothing().when(repository).deleteById(categoriaId);

        UUID resultado = service.delete(categoriaId);

        assertEquals(categoriaId, resultado);
        verify(repository, times(1)).deleteById(categoriaId);
    }

    @Test
    @DisplayName("Deve construir DTO da entidade")
    void deveConstruirDTODaEntidade() {
        CategoriaTituloDTO dto = service.buildDTOFromEntity(entity);

        assertNotNull(dto);
        assertEquals("Categoria Teste", dto.getNome());
    }

    @Test
    @DisplayName("Deve construir GridDTO da entidade")
    void deveConstruirGridDTODaEntidade() {
        CategoriaTituloGridDTO gridDTO = service.buildGridDTOFromEntity(entity);

        assertNotNull(gridDTO);
        assertEquals("Categoria Teste", gridDTO.getNome());
    }
}
