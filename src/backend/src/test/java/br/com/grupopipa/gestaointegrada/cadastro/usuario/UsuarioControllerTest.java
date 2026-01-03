package br.com.grupopipa.gestaointegrada.cadastro.usuario;

import static org.hamcrest.Matchers.hasSize;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilDTO;
import br.com.grupopipa.gestaointegrada.cadastro.perfil.PerfilService;
import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;

/**
 * Testes unitários para UsuarioController. Usa MockMvc para testar os endpoints
 * REST sem subir o
 * servidor. Usa @MockitoBean para simular o
 * service. @AutoConfigureMockMvc(addFilters = false)
 * desabilita os filtros de segurança, permitindo testar apenas a lógica do
 * controller
 * com @WithMockUser. excludeFilters exclui TenantFilter do contexto (requer
 * JwtDecoder).
 */
@DisplayName("UsuarioController - Testes Unitários")
@WebMvcTest(value = UsuarioController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService service;

    @MockitoBean
    private PerfilService perfilService;

    private UsuarioDTO dtoValido;
    private UsuarioGridDTO gridDTO;
    private UUID usuarioId;

    @BeforeEach
    void setup() {
        usuarioId = UUID.randomUUID();

        PerfilDTO perfilDTO = PerfilDTO.builder().id(UUID.randomUUID()).nome("Administrador").build();

        dtoValido = UsuarioDTO.builder()
                .id(usuarioId)
                .nome("João Silva")
                .login("joao.silva")
                .perfis(List.of(perfilDTO))
                .build();

        gridDTO = UsuarioGridDTO.builder().id(usuarioId).nome("João Silva").login("joao.silva").build();
    }

    @Test
    @DisplayName("Deve listar usuários paginados com permissão")
    @WithMockUser(authorities = "CADASTRO_USUARIO_LISTAR")
    void deveListarUsuariosPaginados() throws Exception {
        // Given
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest request = br.com.grupopipa.gestaointegrada.core.dto.PageRequest
                .builder()
                .page(0)
                .size(10)
                .order(List.of())
                .build();

        PageDTO<UsuarioGridDTO> pageDTO = new PageDTO<>(List.of(gridDTO), PageRequest.of(0, 10), 1L);

        when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(pageDTO);

        // When & Then
        mockMvc
                .perform(
                        post("/usuario/query")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.content[0].nome", is("João Silva")))
                .andExpect(jsonPath("$.body.content[0].login", is("joao.silva")));

        verify(service, times(1)).list(any(), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Deve criar novo usuário com permissão")
    @WithMockUser(authorities = "CADASTRO_USUARIO_EDITAR")
    void deveCriarNovoUsuario() throws Exception {
        // Given
        UsuarioDTO novoDTO = UsuarioDTO.builder().nome("Maria Santos").login("maria.santos").senha("senha123").build();

        when(service.save(any(UsuarioDTO.class))).thenReturn(dtoValido);

        // When & Then
        mockMvc
                .perform(
                        post("/usuario")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(novoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.nome", is("João Silva")))
                .andExpect(jsonPath("$.body.login", is("joao.silva")))
                .andExpect(jsonPath("$.body.perfis", hasSize(1)));

        verify(service, times(1)).save(any(UsuarioDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar usuário existente com permissão")
    @WithMockUser(authorities = "CADASTRO_USUARIO_EDITAR")
    void deveAtualizarUsuarioExistente() throws Exception {
        // Given
        dtoValido.setNome("João Silva Atualizado");
        when(service.save(any(UsuarioDTO.class))).thenReturn(dtoValido);

        // When & Then
        mockMvc
                .perform(
                        post("/usuario")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.nome", is("João Silva Atualizado")));

        verify(service, times(1)).save(any(UsuarioDTO.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com permissão")
    @WithMockUser(authorities = "CADASTRO_USUARIO_VISUALIZAR")
    void deveBuscarUsuarioPorId() throws Exception {
        // Given
        when(service.findById(usuarioId)).thenReturn(dtoValido);

        // When & Then
        mockMvc
                .perform(get("/usuario/find-by-id").param("id", usuarioId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.nome", is("João Silva")))
                .andExpect(jsonPath("$.body.login", is("joao.silva")))
                .andExpect(jsonPath("$.body.perfis", hasSize(1)));

        verify(service, times(1)).findById(usuarioId);
    }

    @Test
    @DisplayName("Deve deletar usuário com permissão")
    @WithMockUser(authorities = "CADASTRO_USUARIO_DELETAR")
    void deveDeletarUsuario() throws Exception {
        // Given
        when(service.delete(usuarioId)).thenReturn(usuarioId);

        // When & Then
        mockMvc
                .perform(delete("/usuario/{id}", usuarioId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body").value(usuarioId.toString()));

        verify(service, times(1)).delete(usuarioId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver usuários")
    @WithMockUser(authorities = "CADASTRO_USUARIO_LISTAR")
    void deveRetornarListaVaziaQuandoNaoHouverUsuarios() throws Exception {
        // Given
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest request = br.com.grupopipa.gestaointegrada.core.dto.PageRequest
                .builder()
                .page(0)
                .size(10)
                .order(List.of())
                .build();

        PageDTO<UsuarioGridDTO> pageDTO = new PageDTO<>(List.of(), PageRequest.of(0, 10), 0L);

        when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(pageDTO);

        // When & Then
        mockMvc
                .perform(
                        post("/usuario/query")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.content").isEmpty());

        verify(service, times(1)).list(any(), any(org.springframework.data.domain.Pageable.class));
    }
}
