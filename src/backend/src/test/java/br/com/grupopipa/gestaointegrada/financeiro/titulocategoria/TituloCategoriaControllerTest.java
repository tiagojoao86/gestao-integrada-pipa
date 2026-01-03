package br.com.grupopipa.gestaointegrada.financeiro.titulocategoria;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageRequest;

@DisplayName("CategoriaTituloController - Testes Unitários")
@WebMvcTest(value = TituloCategoriaController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
class TituloCategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TituloCategoriaService service;

    private TituloCategoriaDTO dtoValido;
    private TituloCategoriaGridDTO gridDTO;
    private UUID categoriaId;

    @BeforeEach
    void setup() {
        categoriaId = UUID.randomUUID();

        dtoValido = TituloCategoriaDTO.builder()
                .id(categoriaId)
                .nome("Categoria Teste")
                .descricao("Descrição")
                .tipo(TituloCategoriaTipoEnum.DESPESA)
                .build();

        gridDTO = TituloCategoriaGridDTO.builder()
                .id(categoriaId)
                .nome("Categoria Teste")
                .descricao("Descrição")
                .build();
    }

    @Test
    @DisplayName("Deve listar categorias paginadas com permissão")
    void deveListarCategoriasPaginadas() throws Exception {
        PageRequest request = PageRequest.builder().page(0).size(10).order(List.of()).build();

        PageDTO<TituloCategoriaGridDTO> pageDTO = new PageDTO<>(List.of(gridDTO),
                org.springframework.data.domain.PageRequest.of(0, 10), 1L);

        when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(pageDTO);

        mockMvc
                .perform(
                        post("/titulo-categoria/query")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.body.content[0].id", is(categoriaId.toString())))
                .andExpect(jsonPath("$.body.content[0].nome", is("Categoria Teste")));

        verify(service, times(1)).list(any(), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Deve criar nova categoria com permissão")
    void deveCriarNovaCategoria() throws Exception {
        when(service.save(any(TituloCategoriaDTO.class))).thenReturn(dtoValido);

        mockMvc
                .perform(
                        post("/titulo-categoria")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.body.id", is(categoriaId.toString())))
                .andExpect(jsonPath("$.body.nome", is("Categoria Teste")));

        verify(service, times(1)).save(any(TituloCategoriaDTO.class));
    }

    @Test
    @DisplayName("Deve buscar categoria por id com permissão")
    void deveBuscarCategoriaPorId() throws Exception {
        when(service.findById(categoriaId)).thenReturn(dtoValido);

        mockMvc
                .perform(
                        get("/titulo-categoria/find-by-id")
                                .param("id", categoriaId.toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.body.id", is(categoriaId.toString())))
                .andExpect(jsonPath("$.body.nome", is("Categoria Teste")));

        verify(service, times(1)).findById(categoriaId);
    }

    @Test
    @DisplayName("Deve deletar categoria com permissão")
    void deveDeletarCategoria() throws Exception {
        when(service.delete(categoriaId)).thenReturn(categoriaId);

        mockMvc
                .perform(
                        delete("/titulo-categoria/{id}", categoriaId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)));

        verify(service, times(1)).delete(categoriaId);
    }
}
