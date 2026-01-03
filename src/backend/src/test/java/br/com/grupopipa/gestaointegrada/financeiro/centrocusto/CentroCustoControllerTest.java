package br.com.grupopipa.gestaointegrada.financeiro.centrocusto;

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

@DisplayName("CentroCustoController - Testes Unitários")
@WebMvcTest(value = CentroCustoController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
class CentroCustoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CentroCustoService service;

    private CentroCustoDTO dtoValido;
    private CentroCustoGridDTO gridDTO;
    private UUID centroId;

    @BeforeEach
    void setup() {
        centroId = UUID.randomUUID();

        dtoValido = CentroCustoDTO.builder()
                .id(centroId)
                .nome("Centro Teste")
                .centroResultado(Boolean.FALSE)
                .build();

        gridDTO = CentroCustoGridDTO.builder()
                .id(centroId)
                .nome("Centro Teste")
                .centroResultado(Boolean.FALSE)
                .build();
    }

    @Test
    @DisplayName("Deve listar centros paginados com permissão")
    void deveListarCentrosPaginados() throws Exception {
        PageRequest request = PageRequest.builder().page(0).size(10).order(List.of()).build();

        PageDTO<CentroCustoGridDTO> pageDTO = new PageDTO<>(List.of(gridDTO),
                org.springframework.data.domain.PageRequest.of(0, 10), 1L);

        when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(pageDTO);

        mockMvc
                .perform(
                        post("/centro-custo/query")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.body.content[0].id", is(centroId.toString())))
                .andExpect(jsonPath("$.body.content[0].nome", is("Centro Teste")));

        verify(service, times(1)).list(any(), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Deve criar novo centro com permissão")
    void deveCriarNovoCentro() throws Exception {
        when(service.save(any(CentroCustoDTO.class))).thenReturn(dtoValido);

        mockMvc
                .perform(
                        post("/centro-custo")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.body.id", is(centroId.toString())))
                .andExpect(jsonPath("$.body.nome", is("Centro Teste")));

        verify(service, times(1)).save(any(CentroCustoDTO.class));
    }

    @Test
    @DisplayName("Deve buscar centro por id com permissão")
    void deveBuscarCentroPorId() throws Exception {
        when(service.findById(centroId)).thenReturn(dtoValido);

        mockMvc
                .perform(
                        get("/centro-custo/find-by-id")
                                .param("id", centroId.toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.body.id", is(centroId.toString())))
                .andExpect(jsonPath("$.body.nome", is("Centro Teste")));

        verify(service, times(1)).findById(centroId);
    }

    @Test
    @DisplayName("Deve deletar centro com permissão")
    void deveDeletarCentro() throws Exception {
        when(service.delete(centroId)).thenReturn(centroId);

        mockMvc
                .perform(
                        delete("/centro-custo/{id}", centroId)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)));

        verify(service, times(1)).delete(centroId);
    }
}
