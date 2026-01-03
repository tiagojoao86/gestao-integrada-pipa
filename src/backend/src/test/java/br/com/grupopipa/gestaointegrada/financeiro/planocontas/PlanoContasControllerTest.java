package br.com.grupopipa.gestaointegrada.financeiro.planocontas;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoPlanoContas;

/**
 * Testes de integração para PlanoContasController. Valida os endpoints REST do
 * plano de contas.
 */
@WebMvcTest(value = PlanoContasController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PlanoContasController - Testes de Integração")
class PlanoContasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlanoContasService service;

    private PlanoContasDTO planoContasDTO;
    private PlanoContasGridDTO planoContasGridDTO;
    private String codigo;

    @BeforeEach
    void setUp() {
        codigo = (UUID.randomUUID().toString() + System.nanoTime()).replace("-", "").substring(0, 18);
        planoContasDTO = PlanoContasDTO.builder()
                .id(UUID.randomUUID())
                .codigo(codigo)
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA.name())
                .ativo(true)
                .analitico(false)
                .nivel(1)
                .build();

        planoContasGridDTO = PlanoContasGridDTO.builder()
                .id(planoContasDTO.getId())
                .codigo(codigo)
                .descricao("Receitas")
                .tipo(TipoPlanoContas.RECEITA.name())
                .ativo(true)
                .analitico(false)
                .build();
    }

    @Test
    @WithMockUser(authorities = "CADASTRO_PLANO_CONTAS_LISTAR")
    @DisplayName("Deve listar planos de contas paginados")
    void deveListarPlanosContasPaginados() throws Exception {
        // Given
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest pageRequest = br.com.grupopipa.gestaointegrada.core.dto.PageRequest
                .builder()
                .page(0)
                .size(10)
                .order(Collections.emptyList())
                .build();

        br.com.grupopipa.gestaointegrada.core.dto.PageDTO<PlanoContasGridDTO> pageDTO = new br.com.grupopipa.gestaointegrada.core.dto.PageDTO<>(
                Collections.singletonList(planoContasGridDTO),
                org.springframework.data.domain.PageRequest.of(0, 10),
                1L);

        when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(pageDTO);

        // When & Then
        mockMvc
                .perform(
                        post("/plano-contas/query")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(pageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.content").isArray());
    }

    @Test
    @WithMockUser(authorities = "CADASTRO_PLANO_CONTAS_EDITAR")
    @DisplayName("Deve criar novo plano de contas")
    void deveCriarNovoPlanoContas() throws Exception {
        // Given
        when(service.save(any(PlanoContasDTO.class))).thenReturn(planoContasDTO);

        // When & Then
        mockMvc
                .perform(
                        post("/plano-contas")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(planoContasDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.codigo").value(codigo))
                .andExpect(jsonPath("$.body.descricao").value("Receitas"));
    }

    @Test
    @WithMockUser(authorities = "CADASTRO_PLANO_CONTAS_VISUALIZAR")
    @DisplayName("Deve buscar plano de contas por ID")
    void deveBuscarPlanoContasPorId() throws Exception {
        // Given
        UUID id = planoContasDTO.getId();
        when(service.findById(id)).thenReturn(planoContasDTO);

        // When & Then
        mockMvc
                .perform(
                        get("/plano-contas/find-by-id")
                                .param("id", id.toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.id").value(id.toString()))
                .andExpect(jsonPath("$.body.descricao").value("Receitas"));
    }

    @Test
    @WithMockUser(authorities = "CADASTRO_PLANO_CONTAS_DELETAR")
    @DisplayName("Deve deletar plano de contas")
    void deveDeletarPlanoContas() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(service.delete(id)).thenReturn(id);

        // When & Then
        mockMvc
                .perform(
                        delete("/plano-contas/{id}", id).with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }
}
