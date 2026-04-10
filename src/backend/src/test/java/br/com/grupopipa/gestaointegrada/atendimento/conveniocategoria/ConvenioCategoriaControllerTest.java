package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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

import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto.ConvenioCategoriaDTO;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.dto.ConvenioCategoriaGridDTO;
import br.com.grupopipa.gestaointegrada.core.dto.FilterDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;

@DisplayName("ConvenioCategoriaController - Testes Unitários")
@WebMvcTest(
    value = ConvenioCategoriaController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter")
)
@AutoConfigureMockMvc(addFilters = false)
class ConvenioCategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConvenioCategoriaService service;

    private ConvenioCategoriaDTO dto;
    private UUID id;
    private UUID convenioId;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        convenioId = UUID.randomUUID();

        dto = ConvenioCategoriaDTO.builder()
                .id(id)
                .convenioId(convenioId)
                .convenioNome("Unimed")
                .nome("Básico")
                .codigoAnsPlano("ANS001")
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve listar categorias com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_CONVENIO_CATEGORIA_LISTAR")
    void deveListarCategorias() throws Exception {
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest pageRequest =
                br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
                        .page(0)
                        .size(10)
                        .order(List.of())
                        .build();

        ConvenioCategoriaGridDTO gridDTO = ConvenioCategoriaGridDTO.builder()
                .id(id)
                .convenioNome("Unimed")
                .nome("Básico")
                .codigoAnsPlano("ANS001")
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .build();

        PageDTO<ConvenioCategoriaGridDTO> pageDTO =
                new PageDTO<>(List.of(gridDTO), PageRequest.of(0, 10), 1L);

        when(service.list(nullable(FilterDTO.class), any(Pageable.class))).thenReturn(pageDTO);

        mockMvc.perform(post("/convenio-categoria/query")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.content").isArray())
                .andExpect(jsonPath("$.body.content[0].convenioNome").value("Unimed"))
                .andExpect(jsonPath("$.body.content[0].nome").value("Básico"))
                .andExpect(jsonPath("$.body.content[0].codigoAnsPlano").value("ANS001"))
                .andExpect(jsonPath("$.body.totalElements").value(1));

        verify(service, times(1)).list(nullable(FilterDTO.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve criar nova categoria com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_CONVENIO_CATEGORIA_EDITAR")
    void deveCriarNovaCategoria() throws Exception {
        ConvenioCategoriaDTO novaCategoria = ConvenioCategoriaDTO.builder()
                .convenioId(convenioId)
                .nome("Básico")
                .codigoAnsPlano("ANS001")
                .ativo(true)
                .build();

        when(service.save(any(ConvenioCategoriaDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/convenio-categoria")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novaCategoria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.nome").value("Básico"))
                .andExpect(jsonPath("$.body.convenioNome").value("Unimed"))
                .andExpect(jsonPath("$.body.codigoAnsPlano").value("ANS001"));

        verify(service, times(1)).save(any(ConvenioCategoriaDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar categoria existente com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_CONVENIO_CATEGORIA_EDITAR")
    void deveAtualizarCategoria() throws Exception {
        dto.setNome("Especial");

        when(service.save(any(ConvenioCategoriaDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/convenio-categoria")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.nome").value("Especial"));

        verify(service, times(1)).save(any(ConvenioCategoriaDTO.class));
    }

    @Test
    @DisplayName("Deve buscar categoria por ID com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_CONVENIO_CATEGORIA_VISUALIZAR")
    void deveBuscarCategoriaPorId() throws Exception {
        when(service.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/convenio-categoria/find-by-id").param("id", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.id").value(id.toString()))
                .andExpect(jsonPath("$.body.nome").value("Básico"))
                .andExpect(jsonPath("$.body.convenioNome").value("Unimed"));

        verify(service, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve deletar categoria com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_CONVENIO_CATEGORIA_DELETAR")
    void deveDeletarCategoria() throws Exception {
        when(service.delete(id)).thenReturn(id);

        mockMvc.perform(delete("/convenio-categoria/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body").value(id.toString()));

        verify(service, times(1)).delete(id);
    }
}
