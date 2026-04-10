package br.com.grupopipa.gestaointegrada.atendimento.procedimento;

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

import br.com.grupopipa.gestaointegrada.atendimento.procedimento.dto.ProcedimentoDTO;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.dto.ProcedimentoGridDTO;
import br.com.grupopipa.gestaointegrada.core.dto.FilterDTO;
import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;

@DisplayName("ProcedimentoController - Testes Unitários")
@WebMvcTest(
    value = ProcedimentoController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter")
)
@AutoConfigureMockMvc(addFilters = false)
class ProcedimentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProcedimentoService service;

    private ProcedimentoDTO dto;
    private UUID id;

    @BeforeEach
    void setup() {
        id = UUID.randomUUID();

        dto = ProcedimentoDTO.builder()
                .id(id)
                .codigo("PROC-001")
                .descricao("Sessão de Terapia ABA")
                .codigoTiss("10101012")
                .codigoTuss("20102022")
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Deve listar procedimentos com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_PROCEDIMENTO_LISTAR")
    void deveListarProcedimentos() throws Exception {
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest pageRequest =
                br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
                        .page(0)
                        .size(10)
                        .order(List.of())
                        .build();

        ProcedimentoGridDTO gridDTO = ProcedimentoGridDTO.builder()
                .id(id)
                .codigo("PROC-001")
                .descricao("Sessão de Terapia ABA")
                .codigoTiss("10101012")
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .build();

        PageDTO<ProcedimentoGridDTO> pageDTO =
                new PageDTO<>(List.of(gridDTO), PageRequest.of(0, 10), 1L);

        when(service.list(nullable(FilterDTO.class), any(Pageable.class))).thenReturn(pageDTO);

        mockMvc.perform(post("/procedimento/query")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.content").isArray())
                .andExpect(jsonPath("$.body.content[0].codigo").value("PROC-001"))
                .andExpect(jsonPath("$.body.content[0].descricao").value("Sessão de Terapia ABA"))
                .andExpect(jsonPath("$.body.totalElements").value(1));

        verify(service, times(1)).list(nullable(FilterDTO.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve criar novo procedimento com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_PROCEDIMENTO_EDITAR")
    void deveCriarNovoProcedimento() throws Exception {
        ProcedimentoDTO novoProcedimento = ProcedimentoDTO.builder()
                .codigo("PROC-001")
                .descricao("Sessão de Terapia ABA")
                .codigoTiss("10101012")
                .ativo(true)
                .build();

        when(service.save(any(ProcedimentoDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/procedimento")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoProcedimento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.codigo").value("PROC-001"))
                .andExpect(jsonPath("$.body.descricao").value("Sessão de Terapia ABA"));

        verify(service, times(1)).save(any(ProcedimentoDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar procedimento existente com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_PROCEDIMENTO_EDITAR")
    void deveAtualizarProcedimento() throws Exception {
        dto.setCodigo("PROC-001-UPD");

        when(service.save(any(ProcedimentoDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/procedimento")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.codigo").value("PROC-001-UPD"));

        verify(service, times(1)).save(any(ProcedimentoDTO.class));
    }

    @Test
    @DisplayName("Deve buscar procedimento por ID com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_PROCEDIMENTO_VISUALIZAR")
    void deveBuscarProcedimentoPorId() throws Exception {
        when(service.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/procedimento/find-by-id").param("id", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.id").value(id.toString()))
                .andExpect(jsonPath("$.body.codigo").value("PROC-001"));

        verify(service, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve deletar procedimento com permissão")
    @WithMockUser(authorities = "ATENDIMENTO_PROCEDIMENTO_DELETAR")
    void deveDeletarProcedimento() throws Exception {
        when(service.delete(id)).thenReturn(id);

        mockMvc.perform(delete("/procedimento/{id}", id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body").value(id.toString()));

        verify(service, times(1)).delete(id);
    }
}
