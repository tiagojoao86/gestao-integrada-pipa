package br.com.grupopipa.gestaointegrada.cadastro.perfil;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;

/**
 * Testes unitários para PerfilController. Usa MockMvc para testar os endpoints REST sem subir o
 * servidor. Usa @MockitoBean para simular o service. @AutoConfigureMockMvc(addFilters = false)
 * desabilita os filtros de segurança, permitindo testar apenas a lógica do controller
 * com @WithMockUser. excludeFilters exclui TenantFilter do contexto (requer JwtDecoder).
 */
@DisplayName("PerfilController - Testes Unitários")
@WebMvcTest(
    value = PerfilController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
class PerfilControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private PerfilService service;

  private PerfilDTO dtoValido;
  private PerfilGridDTO gridDTO;
  private UUID perfilId;

  @BeforeEach
  void setup() {
    perfilId = UUID.randomUUID();

    PerfilModuloDTO permissao = new PerfilModuloDTO();
    permissao.setModuloId(UUID.randomUUID());
    permissao.setModuloNome("Cadastro de Pessoas");
    permissao.setPodeListar(true);
    permissao.setPodeVisualizar(true);
    permissao.setPodeEditar(true);
    permissao.setPodeDeletar(false);

    dtoValido =
        PerfilDTO.builder()
            .id(perfilId)
            .nome("Administrador")
            .permissoes(List.of(permissao))
            .build();

    gridDTO = PerfilGridDTO.builder().id(perfilId).nome("Administrador").build();
  }

  @Test
  @DisplayName("Deve listar perfis paginados com permissão")
  @WithMockUser(authorities = "CADASTRO_PERFIL_LISTAR")
  void deveListarPerfisPaginados() throws Exception {
    // Given
    br.com.grupopipa.gestaointegrada.core.dto.PageRequest request =
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
            .page(0)
            .size(10)
            .order(List.of())
            .build();

    PageDTO<PerfilGridDTO> pageDTO = new PageDTO<>(List.of(gridDTO), PageRequest.of(0, 10), 1L);

    when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(pageDTO);

    // When & Then
    mockMvc
        .perform(
            post("/perfil/query")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.content[0].nome", is("Administrador")));

    verify(service, times(1)).list(any(), any(org.springframework.data.domain.Pageable.class));
  }

  @Test
  @DisplayName("Deve criar novo perfil com permissão")
  @WithMockUser(authorities = "CADASTRO_PERFIL_EDITAR")
  void deveCriarNovoPerfil() throws Exception {
    // Given
    PerfilDTO novoDTO = PerfilDTO.builder().nome("Operador").build();

    when(service.save(any(PerfilDTO.class))).thenReturn(dtoValido);

    // When & Then
    mockMvc
        .perform(
            post("/perfil")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novoDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.nome", is("Administrador")))
        .andExpect(jsonPath("$.body.permissoes", hasSize(1)));

    verify(service, times(1)).save(any(PerfilDTO.class));
  }

  @Test
  @DisplayName("Deve atualizar perfil existente com permissão")
  @WithMockUser(authorities = "CADASTRO_PERFIL_EDITAR")
  void deveAtualizarPerfilExistente() throws Exception {
    // Given
    dtoValido.setNome("Administrador Atualizado");
    when(service.save(any(PerfilDTO.class))).thenReturn(dtoValido);

    // When & Then
    mockMvc
        .perform(
            post("/perfil")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoValido)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.nome", is("Administrador Atualizado")));

    verify(service, times(1)).save(any(PerfilDTO.class));
  }

  @Test
  @DisplayName("Deve buscar perfil por ID com permissão")
  @WithMockUser(authorities = "CADASTRO_PERFIL_VISUALIZAR")
  void deveBuscarPerfilPorId() throws Exception {
    // Given
    when(service.findById(perfilId)).thenReturn(dtoValido);

    // When & Then
    mockMvc
        .perform(get("/perfil/find-by-id").param("id", perfilId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.nome", is("Administrador")))
        .andExpect(jsonPath("$.body.permissoes", hasSize(1)));

    verify(service, times(1)).findById(perfilId);
  }

  @Test
  @DisplayName("Deve deletar perfil com permissão")
  @WithMockUser(authorities = "CADASTRO_PERFIL_DELETAR")
  void deveDeletarPerfil() throws Exception {
    // Given
    when(service.delete(perfilId)).thenReturn(perfilId);

    // When & Then
    mockMvc
        .perform(delete("/perfil/{id}", perfilId).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body").value(perfilId.toString()));

    verify(service, times(1)).delete(perfilId);
  }

  @Test
  @DisplayName("Deve retornar lista vazia quando não houver perfis")
  @WithMockUser(authorities = "CADASTRO_PERFIL_LISTAR")
  void deveRetornarListaVaziaQuandoNaoHouverPerfis() throws Exception {
    // Given
    br.com.grupopipa.gestaointegrada.core.dto.PageRequest request =
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
            .page(0)
            .size(10)
            .order(List.of())
            .build();

    PageDTO<PerfilGridDTO> pageDTO = new PageDTO<>(List.of(), PageRequest.of(0, 10), 0L);

    when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(pageDTO);

    // When & Then
    mockMvc
        .perform(
            post("/perfil/query")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.content").isEmpty());

    verify(service, times(1)).list(any(), any(org.springframework.data.domain.Pageable.class));
  }
}
