package br.com.grupopipa.gestaointegrada.atendimento.profissional;

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

import br.com.grupopipa.gestaointegrada.atendimento.profissional.dto.ProfissionalDTO;
import br.com.grupopipa.gestaointegrada.atendimento.profissional.dto.ProfissionalGridDTO;
import br.com.grupopipa.gestaointegrada.core.dto.FilterDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;

@DisplayName("ProfissionalController - Testes Unitários")
@WebMvcTest(
    value = ProfissionalController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter")
)
@AutoConfigureMockMvc(addFilters = false)
class ProfissionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfissionalService service;

    private ProfissionalDTO dto;
    private UUID id;
    private UUID pessoaId;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();
        pessoaId = UUID.randomUUID();

        dto = ProfissionalDTO.builder()
                .id(id)
                .pessoaId(pessoaId)
                .pessoaNome("Ana Paula Ferreira")
                .conselho("CRP")
                .codigoConselho("CRP-06/12345")
                .tipoRemuneracao("CLT")
                .banco("Nubank")
                .conta("12345-6")
                .chavePix("ana@example.com")
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve listar profissionais com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_PROFISSIONAL_LISTAR")
    void deveListarProfissionais() throws Exception {
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest pageRequest =
                br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
                        .page(0)
                        .size(10)
                        .order(List.of())
                        .build();

        ProfissionalGridDTO gridDTO = ProfissionalGridDTO.builder()
                .id(id)
                .pessoaNome("Ana Paula Ferreira")
                .conselho("CRP")
                .codigoConselho("CRP-06/12345")
                .tipoRemuneracao("CLT")
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .build();

        PageDTO<ProfissionalGridDTO> pageDTO =
                new PageDTO<>(List.of(gridDTO), PageRequest.of(0, 10), 1L);

        when(service.list(nullable(FilterDTO.class), any(Pageable.class))).thenReturn(pageDTO);

        mockMvc.perform(post("/profissional/query")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.content").isArray())
                .andExpect(jsonPath("$.body.content[0].pessoaNome").value("Ana Paula Ferreira"))
                .andExpect(jsonPath("$.body.content[0].conselho").value("CRP"))
                .andExpect(jsonPath("$.body.content[0].tipoRemuneracao").value("CLT"))
                .andExpect(jsonPath("$.body.totalElements").value(1));

        verify(service, times(1)).list(nullable(FilterDTO.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve criar novo profissional com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_PROFISSIONAL_EDITAR")
    void deveCriarNovoProfissional() throws Exception {
        ProfissionalDTO novoProfissional = ProfissionalDTO.builder()
                .pessoaId(pessoaId)
                .conselho("CRP")
                .codigoConselho("CRP-06/12345")
                .tipoRemuneracao("CLT")
                .ativo(true)
                .build();

        when(service.save(any(ProfissionalDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/profissional")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoProfissional)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.pessoaNome").value("Ana Paula Ferreira"))
                .andExpect(jsonPath("$.body.conselho").value("CRP"))
                .andExpect(jsonPath("$.body.tipoRemuneracao").value("CLT"));

        verify(service, times(1)).save(any(ProfissionalDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar profissional existente com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_PROFISSIONAL_EDITAR")
    void deveAtualizarProfissional() throws Exception {
        dto.setConselho("CRM");
        dto.setCodigoConselho("CRM-SP/99999");

        when(service.save(any(ProfissionalDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/profissional")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.conselho").value("CRM"))
                .andExpect(jsonPath("$.body.codigoConselho").value("CRM-SP/99999"));

        verify(service, times(1)).save(any(ProfissionalDTO.class));
    }

    @Test
    @DisplayName("Deve buscar profissional por ID com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_PROFISSIONAL_VISUALIZAR")
    void deveBuscarProfissionalPorId() throws Exception {
        when(service.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/profissional/find-by-id").param("id", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.id").value(id.toString()))
                .andExpect(jsonPath("$.body.pessoaNome").value("Ana Paula Ferreira"))
                .andExpect(jsonPath("$.body.conselho").value("CRP"))
                .andExpect(jsonPath("$.body.banco").value("Nubank"));

        verify(service, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve deletar profissional com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_PROFISSIONAL_DELETAR")
    void deveDeletarProfissional() throws Exception {
        when(service.delete(id)).thenReturn(id);

        mockMvc.perform(delete("/profissional/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body").value(id.toString()));

        verify(service, times(1)).delete(id);
    }

}
