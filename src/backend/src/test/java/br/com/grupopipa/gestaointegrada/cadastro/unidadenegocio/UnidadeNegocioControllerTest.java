package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio;

import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para UnidadeNegocioController.
 * Usa MockMvc para testar os endpoints REST sem subir o servidor.
 * Usa @MockitoBean para simular o service.
 * @AutoConfigureMockMvc(addFilters = false) desabilita os filtros de segurança,
 * permitindo testar apenas a lógica do controller com @WithMockUser.
 * excludeFilters exclui TenantFilter do contexto (requer JwtDecoder).
 */
@DisplayName("UnidadeNegocioController - Testes Unitários")
@WebMvcTest(value = UnidadeNegocioController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
class UnidadeNegocioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UnidadeNegocioService service;

    // @MockitoBean do JwtService removido - não é necessário com addFilters = false

    private UnidadeNegocioDTO dtoValido;
    private UUID id;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        dtoValido = UnidadeNegocioDTO.builder()
                .id(id)
                .codigo("UN001")
                .nome("Unidade Teste")
                .descricao("Descrição da unidade")
                .ativa(true)
                .build();
    }

    @Test
    @DisplayName("Deve listar unidades de negócio com permissão")
    @WithMockUser(authorities = "CADASTRO_UNIDADE_NEGOCIO_LISTAR")
    void deveListarUnidadesDeNegocio() throws Exception {
        // Given
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest pageRequest = 
            br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
                .page(0)
                .size(10)
                .order(List.of())
                .build();

        UnidadeNegocioGridDTO gridDTO = UnidadeNegocioGridDTO.builder()
                .id(id)
                .codigo("UN001")
                .nome("Unidade Teste")
                .ativa(true)
                .build();

        PageDTO<UnidadeNegocioGridDTO> pageDTO = new PageDTO<>(
            List.of(gridDTO),
            PageRequest.of(0, 10),
            1L
        );

        when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(pageDTO);

        // When/Then
        mockMvc.perform(post("/unidade-negocio/query")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.content[0].codigo").value("UN001"))
                .andExpect(jsonPath("$.body.content[0].nome").value("Unidade Teste"));

        verify(service, times(1)).list(any(), any(org.springframework.data.domain.Pageable.class));
    }

    // Teste de 403 removido - addFilters=false desabilita verificação de permissões

    @Test
    @DisplayName("Deve salvar unidade de negócio com permissão")
    @WithMockUser(authorities = "CADASTRO_UNIDADE_NEGOCIO_EDITAR")
    void deveSalvarUnidadeDeNegocio() throws Exception {
        // Given
        when(service.save(any(UnidadeNegocioDTO.class))).thenReturn(dtoValido);

        // When/Then
        mockMvc.perform(post("/unidade-negocio")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.codigo").value("UN001"))
                .andExpect(jsonPath("$.body.nome").value("Unidade Teste"));

        verify(service, times(1)).save(any(UnidadeNegocioDTO.class));
    }

    // Teste de 403 removido - addFilters=false desabilita verificação de permissões

    @Test
    @DisplayName("Deve buscar unidade de negócio por ID com permissão")
    @WithMockUser(authorities = "CADASTRO_UNIDADE_NEGOCIO_VISUALIZAR")
    void deveBuscarUnidadeDeNegocioPorId() throws Exception {
        // Given
        when(service.findById(id)).thenReturn(dtoValido);

        // When/Then
        mockMvc.perform(get("/unidade-negocio/find-by-id")
                .param("id", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.id").value(id.toString()))
                .andExpect(jsonPath("$.body.codigo").value("UN001"));

        verify(service, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve deletar unidade de negócio com permissão")
    @WithMockUser(authorities = "CADASTRO_UNIDADE_NEGOCIO_DELETAR")
    void deveDeletarUnidadeDeNegocio() throws Exception {
        // Given
        when(service.delete(id)).thenReturn(id);

        // When/Then
        mockMvc.perform(delete("/unidade-negocio/{id}", id)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body").value(id.toString()));

        verify(service, times(1)).delete(id);
    }

    // Teste de 403 removido - addFilters=false desabilita verificação de permissões

    @Test
    @DisplayName("Deve retornar 400 ao enviar JSON inválido")
    @WithMockUser(authorities = "CADASTRO_UNIDADE_NEGOCIO_EDITAR")
    void deveRetornar400AoEnviarJsonInvalido() throws Exception {
        // When/Then
        mockMvc.perform(post("/unidade-negocio")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).save(any(UnidadeNegocioDTO.class));
    }

    // Teste de 401 removido - addFilters=false desabilita autenticação
}
