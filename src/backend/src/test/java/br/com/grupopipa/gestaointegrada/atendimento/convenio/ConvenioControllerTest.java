package br.com.grupopipa.gestaointegrada.atendimento.convenio;

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

import br.com.grupopipa.gestaointegrada.atendimento.convenio.dto.ConvenioDTO;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.dto.ConvenioGridDTO;
import br.com.grupopipa.gestaointegrada.core.dto.FilterDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;

@DisplayName("ConvenioController - Testes Unitários")
@WebMvcTest(
    value = ConvenioController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter")
)
@AutoConfigureMockMvc(addFilters = false)
class ConvenioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConvenioService service;

    private ConvenioDTO dto;
    private UUID id;
    private UUID pessoaId;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        pessoaId = UUID.randomUUID();

        dto = ConvenioDTO.builder()
                .id(id)
                .nome("Unimed")
                .pessoaId(pessoaId)
                .pessoaNome("Plano Saúde Ltda")
                .registroAns("123456")
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve listar convênios com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_CONVENIO_LISTAR")
    void deveListarConvenios() throws Exception {
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest pageRequest =
                br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
                        .page(0)
                        .size(10)
                        .order(List.of())
                        .build();

        ConvenioGridDTO gridDTO = ConvenioGridDTO.builder()
                .id(id)
                .nome("Unimed")
                .pessoaNome("Plano Saúde Ltda")
                .registroAns("123456")
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .build();

        PageDTO<ConvenioGridDTO> pageDTO =
                new PageDTO<>(List.of(gridDTO), PageRequest.of(0, 10), 1L);

        when(service.list(nullable(FilterDTO.class), any(Pageable.class))).thenReturn(pageDTO);

        mockMvc.perform(post("/convenio/query")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.content").isArray())
                .andExpect(jsonPath("$.body.content[0].nome").value("Unimed"))
                .andExpect(jsonPath("$.body.content[0].pessoaNome").value("Plano Saúde Ltda"))
                .andExpect(jsonPath("$.body.content[0].registroAns").value("123456"))
                .andExpect(jsonPath("$.body.totalElements").value(1));

        verify(service, times(1)).list(nullable(FilterDTO.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve criar novo convênio com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_CONVENIO_EDITAR")
    void deveCriarNovoConvenio() throws Exception {
        ConvenioDTO novoConvenio = ConvenioDTO.builder()
                .nome("Unimed")
                .pessoaId(pessoaId)
                .registroAns("123456")
                .ativo(true)
                .build();

        when(service.save(any(ConvenioDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/convenio")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoConvenio)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.nome").value("Unimed"))
                .andExpect(jsonPath("$.body.pessoaNome").value("Plano Saúde Ltda"))
                .andExpect(jsonPath("$.body.registroAns").value("123456"));

        verify(service, times(1)).save(any(ConvenioDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar convênio existente com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_CONVENIO_EDITAR")
    void deveAtualizarConvenio() throws Exception {
        dto.setNome("Amil");
        dto.setRegistroAns("654321");

        when(service.save(any(ConvenioDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/convenio")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.nome").value("Amil"))
                .andExpect(jsonPath("$.body.registroAns").value("654321"));

        verify(service, times(1)).save(any(ConvenioDTO.class));
    }

    @Test
    @DisplayName("Deve buscar convênio por ID com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_CONVENIO_VISUALIZAR")
    void deveBuscarConvenioPorId() throws Exception {
        when(service.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/convenio/find-by-id").param("id", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.id").value(id.toString()))
                .andExpect(jsonPath("$.body.nome").value("Unimed"))
                .andExpect(jsonPath("$.body.registroAns").value("123456"))
                .andExpect(jsonPath("$.body.pessoaNome").value("Plano Saúde Ltda"));

        verify(service, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve deletar convênio com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_CONVENIO_DELETAR")
    void deveDeletarConvenio() throws Exception {
        when(service.delete(id)).thenReturn(id);

        mockMvc.perform(delete("/convenio/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body").value(id.toString()));

        verify(service, times(1)).delete(id);
    }

}
