package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.test.util.ReflectionTestUtils;

import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import br.com.grupopipa.gestaointegrada.financeiro.entity.TituloCategoria;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaTituloService - Testes Unitários")
class TituloCategoriaServiceTest {

    @Mock
    private TituloCategoriaRepository repository;

    @Mock
    private Specifications<TituloCategoria> specifications;

    @InjectMocks
    private TituloCategoriaServiceImpl service;

    private TituloCategoriaDTO dto;
    private TituloCategoria entity;
    private UUID categoriaId;
    private UUID agrupadorId;

    @BeforeEach
    void setup() {
        categoriaId = UUID.randomUUID();
        agrupadorId = UUID.randomUUID();

        dto = TituloCategoriaDTO.builder()
                .codigo("001")
                .nome("Categoria Teste")
                .descricao("Descrição categoria")
                .tipo(TituloCategoriaTipoEnum.RECEITA)
                .build();

        entity = new TituloCategoria.Builder()
                .codigo("001")
                .nome("Categoria Teste")
                .descricao("Descrição categoria")
                .tipo(TituloCategoriaTipoEnum.RECEITA)
                .build();
    }

    @Test
    @DisplayName("Deve criar nova categoria")
    void deveCriarNovaCategoria() {
        when(repository.save(any(TituloCategoria.class))).thenReturn(entity);

        TituloCategoriaDTO resultado = service.save(dto);

        assertNotNull(resultado);
        assertEquals("Categoria Teste", resultado.getNome());
        verify(repository, times(1)).save(any(TituloCategoria.class));
    }

    @Test
    @DisplayName("Deve buscar categoria por id")
    void deveBuscarCategoriaPorId() {
        when(repository.findById(categoriaId)).thenReturn(Optional.of(entity));

        TituloCategoriaDTO resultado = service.findById(categoriaId);

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
        TituloCategoriaDTO dto = service.buildDTOFromEntity(entity);

        assertNotNull(dto);
        assertEquals("Categoria Teste", dto.getNome());
    }

    @Test
    @DisplayName("Deve construir GridDTO da entidade")
    void deveConstruirGridDTODaEntidade() {
        TituloCategoriaGridDTO gridDTO = service.buildGridDTOFromEntity(entity);

        assertNotNull(gridDTO);
        assertEquals("001", gridDTO.getCodigo());
        assertEquals("Categoria Teste", gridDTO.getNome());
    }

    @Test
    @DisplayName("Deve criar categoria com agrupador")
    void deveCriarCategoriaComAgrupador() {
        // Setup agrupador
        TituloCategoria agrupador = new TituloCategoria.Builder()
                .codigo("001")
                .nome("Despesas Operacionais")
                .descricao("Agrupador")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .build();
        ReflectionTestUtils.setField(agrupador, "id", agrupadorId);

        TituloCategoriaDTO dtoComAgrupador = TituloCategoriaDTO.builder()
                .codigo("001.001")
                .nome("Material de Escritório")
                .descricao("Sub-categoria")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .agrupadorId(agrupadorId)
                .build();

        TituloCategoria entityComAgrupador = new TituloCategoria.Builder()
                .codigo("001.001")
                .nome("Material de Escritório")
                .descricao("Sub-categoria")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .agrupador(agrupador)
                .build();

        when(repository.findById(agrupadorId)).thenReturn(Optional.of(agrupador));
        when(repository.save(any(TituloCategoria.class))).thenReturn(entityComAgrupador);

        TituloCategoriaDTO resultado = service.save(dtoComAgrupador);

        assertNotNull(resultado);
        assertEquals("Material de Escritório", resultado.getNome());
        assertEquals(agrupadorId, resultado.getAgrupadorId());
        verify(repository, times(1)).findById(agrupadorId);
        verify(repository, times(1)).save(any(TituloCategoria.class));
    }

    @Test
    @DisplayName("Deve construir DTO com agrupador da entidade")
    void deveConstruirDTOComAgrupadorDaEntidade() {
        TituloCategoria agrupador = new TituloCategoria.Builder()
                .codigo("002")
                .nome("Receitas de Serviços")
                .descricao("Agrupador")
                .tipo(TituloCategoriaTipoEnum.RECEITA)
                .build();
        ReflectionTestUtils.setField(agrupador, "id", UUID.randomUUID());

        TituloCategoria categoriaComAgrupador = new TituloCategoria.Builder()
                .codigo("002.001")
                .nome("Consultoria")
                .descricao("Receita de consultoria")
                .tipo(TituloCategoriaTipoEnum.RECEITA)
                .agrupador(agrupador)
                .build();

        TituloCategoriaDTO dto = service.buildDTOFromEntity(categoriaComAgrupador);

        assertNotNull(dto);
        assertEquals("002.001", dto.getCodigo());
        assertEquals("Consultoria", dto.getNome());
        assertNotNull(dto.getAgrupadorId());
        assertEquals("Receitas de Serviços", dto.getAgrupadorNome());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar agrupador inexistente")
    void deveLancarExcecaoAoBuscarAgrupadorInexistente() {
        TituloCategoriaDTO dtoComAgrupadorInvalido = TituloCategoriaDTO.builder()
                .codigo("003.001")
                .nome("Categoria Teste")
                .descricao("Descrição")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .agrupadorId(UUID.randomUUID())
                .build();

        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.save(dtoComAgrupadorInvalido));
    }
}
