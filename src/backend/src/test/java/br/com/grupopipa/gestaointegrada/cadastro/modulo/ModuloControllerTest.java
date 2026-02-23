package br.com.grupopipa.gestaointegrada.cadastro.modulo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.grupopipa.gestaointegrada.core.dto.FilterDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;

/**
 * Testes unitários para ModuloController. Usa MockMvc para testar os endpoints
 * REST sem subir o
 * servidor. Usa @MockitoBean para simular o service.
 *
 * <p>
 * Nota: Modulo é read-only, não suporta save/delete.
 */
@DisplayName("ModuloController - Testes Unitários")
@WebMvcTest(value = ModuloController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
class ModuloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ModuloService service;

    private ModuloDTO moduloCadastro;
    private ModuloDTO moduloFinanceiro;
    private UUID id;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();

        moduloCadastro = ModuloDTO.builder()
                .id(id)
                .chave("CADASTRO_PESSOA")
                .nome("Cadastro de Pessoas")
                .grupoEnum(GrupoModuloEnum.CADASTROS)
                .build();

        moduloFinanceiro = ModuloDTO.builder()
                .id(UUID.randomUUID())
                .chave("FINANCEIRO_TITULO")
                .nome("Títulos Financeiros")
                .grupoEnum(GrupoModuloEnum.FINANCEIRO)
                .build();
    }

    @Test
    @DisplayName("Deve listar todos os módulos com permissão")
    @WithMockUser(authorities = "CADASTRO_PERFIL_LISTAR")
    void deveListarTodosModulos() throws Exception {
        // Given
        when(service.findAllSimple()).thenReturn(List.of(moduloCadastro, moduloFinanceiro));

        // When & Then
        mockMvc
                .perform(get("/modulo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body[0].chave").value("CADASTRO_PESSOA"))
                .andExpect(jsonPath("$.body[0].nome").value("Cadastro de Pessoas"))
                .andExpect(jsonPath("$.body[0].grupoEnum").value("CADASTROS"))
                .andExpect(jsonPath("$.body[1].chave").value("FINANCEIRO_TITULO"))
                .andExpect(jsonPath("$.body[1].grupoEnum").value("FINANCEIRO"));

        verify(service, times(1)).findAllSimple();
    }

    @Test
    @DisplayName("Deve listar módulos agrupados com permissão")
    @WithMockUser(authorities = "CADASTRO_PERFIL_LISTAR")
    void deveListarModulosAgrupados() throws Exception {
        // Given
        when(service.findAllSimple()).thenReturn(List.of(moduloCadastro, moduloFinanceiro));

        // When & Then
        mockMvc
                .perform(get("/modulo/grouped"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.CADASTROS").isArray())
                .andExpect(jsonPath("$.body.CADASTROS[0].chave").value("CADASTRO_PESSOA"))
                .andExpect(jsonPath("$.body.FINANCEIRO").isArray())
                .andExpect(jsonPath("$.body.FINANCEIRO[0].chave").value("FINANCEIRO_TITULO"));

        verify(service, times(1)).findAllSimple();
    }

    @Test
    @DisplayName("Deve listar módulos paginados com permissão")
    @WithMockUser(authorities = "CADASTRO_PERFIL_LISTAR")
    void deveListarModulosPaginados() throws Exception {
        // Given
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest pageRequest = br.com.grupopipa.gestaointegrada.core.dto.PageRequest
                .builder()
                .page(0)
                .size(10)
                .order(List.of())
                .build();

        ModuloGridDTO gridDTO1 = ModuloGridDTO.builder().id(id).nome("Cadastro de Pessoas").build();

        ModuloGridDTO gridDTO2 = ModuloGridDTO.builder().id(UUID.randomUUID()).nome("Títulos Financeiros").build();

        PageDTO<ModuloGridDTO> pageDTO = new PageDTO<>(List.of(gridDTO1, gridDTO2), PageRequest.of(0, 10), 2L);

        when(service.list(nullable(FilterDTO.class), any(Pageable.class))).thenReturn(pageDTO);

        // When & Then
        mockMvc
                .perform(
                        post("/modulo/query")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(pageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.content").isArray())
                .andExpect(jsonPath("$.body.content[0].nome").value("Cadastro de Pessoas"))
                .andExpect(jsonPath("$.body.content[1].nome").value("Títulos Financeiros"))
                .andExpect(jsonPath("$.body.totalElements").value(2));

        verify(service, times(1)).list(nullable(FilterDTO.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve buscar módulo por ID com permissão")
    @WithMockUser(authorities = "CADASTRO_PERFIL_VISUALIZAR")
    void deveBuscarModuloPorId() throws Exception {
        // Given
        when(service.findById(id)).thenReturn(moduloCadastro);

        // When & Then
        mockMvc
                .perform(get("/modulo/find-by-id").param("id", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.id").value(id.toString()))
                .andExpect(jsonPath("$.body.chave").value("CADASTRO_PESSOA"))
                .andExpect(jsonPath("$.body.nome").value("Cadastro de Pessoas"));

        verify(service, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver módulos")
    @WithMockUser(authorities = "CADASTRO_PERFIL_LISTAR")
    void deveRetornarListaVaziaQuandoNaoHouverModulos() throws Exception {
        // Given
        when(service.findAllSimple()).thenReturn(List.of());

        // When & Then
        mockMvc
                .perform(get("/modulo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body").isArray())
                .andExpect(jsonPath("$.body").isEmpty());

        verify(service, times(1)).findAllSimple();
    }

    @Test
    @DisplayName("Deve listar módulos agrupados com grupo UNDEFINED quando não tiver grupo")
    @WithMockUser(authorities = "CADASTRO_PERFIL_LISTAR")
    void deveListarModulosAgrupadosComUndefined() throws Exception {
        // Given
        ModuloDTO moduloSemGrupo = ModuloDTO.builder()
                .id(UUID.randomUUID())
                .chave("MODULO_SEM_GRUPO")
                .nome("Módulo Sem Grupo")
                .grupoEnum(null)
                .build();

        when(service.findAllSimple()).thenReturn(List.of(moduloCadastro, moduloSemGrupo));

        // When & Then
        mockMvc
                .perform(get("/modulo/grouped"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.CADASTROS").isArray())
                .andExpect(jsonPath("$.body.UNDEFINED").isArray())
                .andExpect(jsonPath("$.body.UNDEFINED[0].chave").value("MODULO_SEM_GRUPO"));

        verify(service, times(1)).findAllSimple();
    }
}
