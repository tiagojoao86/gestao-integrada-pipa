package br.com.grupopipa.gestaointegrada.financeiro.movimentacao;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import br.com.grupopipa.gestaointegrada.financeiro.enums.FormaPagamento;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoMovimentacao;

/**
 * Testes unitários para MovimentacaoFinanceiraController. Valida os endpoints REST da API de
 * movimentações financeiras.
 */
@DisplayName("MovimentacaoFinanceiraController - Testes Unitários")
@WebMvcTest(
    value = MovimentacaoFinanceiraController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
class MovimentacaoFinanceiraControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private MovimentacaoFinanceiraService service;

  @MockitoBean
  private br.com.grupopipa.gestaointegrada.financeiro.titulo.TituloService tituloService;

  private MovimentacaoFinanceiraDTO dto;
  private UUID movimentacaoId;

  @BeforeEach
  void setUp() {
    movimentacaoId = UUID.randomUUID();

    MovimentacaoTituloDTO movTituloDTO =
        MovimentacaoTituloDTO.builder()
            .id(UUID.randomUUID())
            .descricao("Pagamento fornecedor")
            .build();
    dto =
        MovimentacaoFinanceiraDTO.builder()
            .id(movimentacaoId)
            .titulos(List.of(movTituloDTO))
            .contaBancariaId(UUID.randomUUID())
            .tipo(TipoMovimentacao.PAGAMENTO.name())
            .formaPagamento(FormaPagamento.PIX.name())
            .valor(BigDecimal.valueOf(500.00))
            .data(LocalDate.now())
            .build();
  }

  @Test
  @DisplayName("Deve listar movimentações financeiras paginadas")
  @WithMockUser(authorities = "FINANCEIRO_MOVIMENTACAO_LISTAR")
  void deveListarMovimentacoesPaginadas() throws Exception {
    // Given
    br.com.grupopipa.gestaointegrada.core.dto.PageRequest request =
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
            .page(0)
            .size(10)
            .order(List.of())
            .build();

    MovimentacaoFinanceiraGridDTO gridDTO =
        MovimentacaoFinanceiraGridDTO.builder()
            .id(movimentacaoId)
            .tipo(TipoMovimentacao.PAGAMENTO.name())
            .formaPagamento(FormaPagamento.PIX.name())
            .valor(BigDecimal.valueOf(500.00))
            .data(LocalDate.now())
            .build();

    PageDTO<MovimentacaoFinanceiraGridDTO> pageDTO =
        new PageDTO<>(List.of(gridDTO), PageRequest.of(0, 10), 1L);

    when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(pageDTO);

    // When & Then
    mockMvc
        .perform(
            post("/movimentacao-financeira/query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.content[0].tipo").value(TipoMovimentacao.PAGAMENTO.name()))
        .andExpect(jsonPath("$.body.content[0].formaPagamento").value(FormaPagamento.PIX.name()));

    verify(service, times(1)).list(any(), any(org.springframework.data.domain.Pageable.class));
  }

  @Test
  @DisplayName("Deve criar nova movimentação financeira")
  @WithMockUser(authorities = "FINANCEIRO_MOVIMENTACAO_EDITAR")
  void deveCriarNovaMovimentacao() throws Exception {
    // Given
    when(service.save(any(MovimentacaoFinanceiraDTO.class))).thenReturn(dto);

    // When & Then
    mockMvc
        .perform(
            post("/movimentacao-financeira")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode", is(200)))
        .andExpect(jsonPath("$.body.id", is(movimentacaoId.toString())))
        .andExpect(jsonPath("$.body.tipo", is(TipoMovimentacao.PAGAMENTO.name())));

    verify(service, times(1)).save(any(MovimentacaoFinanceiraDTO.class));
  }

  @Test
  @DisplayName("Deve buscar movimentação financeira por ID")
  @WithMockUser(authorities = "FINANCEIRO_MOVIMENTACAO_VISUALIZAR")
  void deveBuscarMovimentacaoPorId() throws Exception {
    // Given
    when(service.findById(movimentacaoId)).thenReturn(dto);

    // When & Then
    mockMvc
        .perform(
            get("/movimentacao-financeira/find-by-id")
                .param("id", movimentacaoId.toString())
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode", is(200)))
        .andExpect(jsonPath("$.body.id", is(movimentacaoId.toString())));

    verify(service, times(1)).findById(movimentacaoId);
  }

  @Test
  @DisplayName("Deve deletar movimentação financeira")
  @WithMockUser(authorities = "FINANCEIRO_MOVIMENTACAO_DELETAR")
  void deveDeletarMovimentacao() throws Exception {
    // Given
    when(service.delete(movimentacaoId)).thenReturn(movimentacaoId);

    // When & Then
    mockMvc
        .perform(delete("/movimentacao-financeira/{id}", movimentacaoId).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode", is(200)));

    verify(service, times(1)).delete(movimentacaoId);
  }
}
