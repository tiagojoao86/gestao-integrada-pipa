package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para ContaBancariaController.
 * Usa MockMvc para testar os endpoints REST sem subir o servidor.
 */
@DisplayName("ContaBancariaController - Testes Unitários")
@WebMvcTest(value = ContaBancariaController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
class ContaBancariaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ContaBancariaService service;

    private ContaBancariaDTO dtoValido;
    private ContaBancariaGridDTO gridDTO;
    private UUID contaId;

    @BeforeEach
    void setup() {
        contaId = UUID.randomUUID();

        dtoValido = ContaBancariaDTO.builder()
                .id(contaId)
                .nome("Conta Corrente Principal")
                .banco("Banco do Brasil")
                .agencia("1234")
                .numeroConta("12345-6")
                .tipo(TipoConta.CORRENTE.name())
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .build();

        gridDTO = ContaBancariaGridDTO.builder()
                .id(contaId)
                .nome("Conta Corrente Principal")
                .banco("Banco do Brasil")
                .tipo(TipoConta.CORRENTE.name())
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .ativa(true)
                .build();
    }

    @Test
    @DisplayName("Deve listar contas bancárias paginadas com permissão")
    @WithMockUser(authorities = "FINANCEIRO_CONTA_BANCARIA_LISTAR")
    void deveListarContasBancariasPaginadas() throws Exception {
        // Given
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest request = 
            br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
                .page(0)
                .size(10)
                .order(List.of())
                .build();

        PageDTO<ContaBancariaGridDTO> pageDTO = new PageDTO<>(
                List.of(gridDTO),
                PageRequest.of(0, 10),
                1L
        );

        when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(pageDTO);

        // When & Then
        mockMvc.perform(post("/api/conta-bancaria/query")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.content[0].nome", is("Conta Corrente Principal")));

        verify(service, times(1)).list(any(), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Deve criar nova conta bancária com permissão")
    @WithMockUser(authorities = "FINANCEIRO_CONTA_BANCARIA_EDITAR")
    void deveCriarNovaContaBancaria() throws Exception {
        // Given
        when(service.save(any(ContaBancariaDTO.class))).thenReturn(dtoValido);

        // When & Then
        mockMvc.perform(post("/api/conta-bancaria")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.nome", is("Conta Corrente Principal")));

        verify(service, times(1)).save(any(ContaBancariaDTO.class));
    }

    @Test
    @DisplayName("Deve buscar conta bancária por ID com permissão")
    @WithMockUser(authorities = "FINANCEIRO_CONTA_BANCARIA_VISUALIZAR")
    void deveBuscarContaBancariaPorId() throws Exception {
        // Given
        when(service.findById(contaId)).thenReturn(dtoValido);

        // When & Then
        mockMvc.perform(get("/api/conta-bancaria/find-by-id")
                        .param("id", contaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body.nome", is("Conta Corrente Principal")));

        verify(service, times(1)).findById(contaId);
    }

    @Test
    @DisplayName("Deve deletar conta bancária com permissão")
    @WithMockUser(authorities = "FINANCEIRO_CONTA_BANCARIA_DELETAR")
    void deveDeletarContaBancaria() throws Exception {
        // Given
        when(service.delete(contaId)).thenReturn(contaId);

        // When & Then
        mockMvc.perform(delete("/api/conta-bancaria/{id}", contaId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.body").value(contaId.toString()));

        verify(service, times(1)).delete(contaId);
    }
}
