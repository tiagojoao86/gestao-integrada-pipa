package br.com.grupopipa.gestaointegrada.financeiro.titulo;

import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;
import br.com.grupopipa.gestaointegrada.financeiro.enums.StatusTitulo;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoTitulo;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes unitários para TituloController - CRUD básico.
 * Usa MockMvc para testar os endpoints REST sem subir o servidor.
 */
@DisplayName("TituloController - Testes Unitários")
@WebMvcTest(value = TituloController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
class TituloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TituloService service;

    private TituloDTO dtoValido;
    private TituloGridDTO gridDTO;
    private UUID tituloId;

    @BeforeEach
    void setup() {
        tituloId = UUID.randomUUID();

        dtoValido = TituloDTO.builder()
                .id(tituloId)
                .tipo(TipoTitulo.A_PAGAR.name())
                .status(StatusTitulo.ABERTO.name())
                .descricao("Pagamento fornecedor")
                .pessoaId(UUID.randomUUID())
                .pessoaNome("Fornecedor Teste")
                .planoContasId(UUID.randomUUID())
                .planoContasDescricao("Fornecedores")
                .valorOriginal(BigDecimal.valueOf(1000.00))
                .dataEmissao(LocalDate.now())
                .dataVencimento(LocalDate.now().plusDays(30))
                .build();

        gridDTO = TituloGridDTO.builder()
                .id(tituloId)
                .tipo(TipoTitulo.A_PAGAR.name())
                .status(StatusTitulo.ABERTO.name())
                .descricao("Pagamento fornecedor")
                .pessoaNome("Fornecedor Teste")
                .valorOriginal(BigDecimal.valueOf(1000.00))
                .saldo(BigDecimal.valueOf(1000.00))
                .dataVencimento(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("Deve listar títulos paginados com permissão")
    @WithMockUser(authorities = "FINANCEIRO_TITULO_LISTAR")
    void deveListarTitulosPaginados() throws Exception {
        // Given
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest request = 
            br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
                .page(0)
                .size(10)
                .order(List.of())
                .build();

        PageDTO<TituloGridDTO> pageDTO = new PageDTO<>(
                List.of(gridDTO),
                PageRequest.of(0, 10),
                1L
        );

        when(service.list(any(), any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(pageDTO);

        // When & Then
        mockMvc.perform(post("/titulo/query")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.body.content[0].id", is(tituloId.toString())))
                .andExpect(jsonPath("$.body.content[0].tipo", is(TipoTitulo.A_PAGAR.name())));

        verify(service, times(1)).list(any(), any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("Deve criar novo título com permissão")
    @WithMockUser(authorities = "FINANCEIRO_TITULO_EDITAR")
    void deveCriarNovoTitulo() throws Exception {
        // Given
        when(service.save(any(TituloDTO.class))).thenReturn(dtoValido);

        // When & Then
        mockMvc.perform(post("/titulo")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.body.id", is(tituloId.toString())))
                .andExpect(jsonPath("$.body.descricao", is("Pagamento fornecedor")));

        verify(service, times(1)).save(any(TituloDTO.class));
    }

    @Test
    @DisplayName("Deve buscar título por ID com permissão")
    @WithMockUser(authorities = "FINANCEIRO_TITULO_VISUALIZAR")
    void deveBuscarTituloPorId() throws Exception {
        // Given
        when(service.findById(tituloId)).thenReturn(dtoValido);

        // When & Then
        mockMvc.perform(get("/titulo/find-by-id")
                        .param("id", tituloId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)))
                .andExpect(jsonPath("$.body.id", is(tituloId.toString())))
                .andExpect(jsonPath("$.body.tipo", is(TipoTitulo.A_PAGAR.name())))
                .andExpect(jsonPath("$.body.status", is(StatusTitulo.ABERTO.name())));

        verify(service, times(1)).findById(tituloId);
    }

    @Test
    @DisplayName("Deve deletar título com permissão")
    @WithMockUser(authorities = "FINANCEIRO_TITULO_DELETAR")
    void deveDeletarTitulo() throws Exception {
        // Given
        when(service.delete(tituloId)).thenReturn(tituloId);

        // When & Then
        mockMvc.perform(delete("/titulo/{id}", tituloId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode", is(200)));

        verify(service, times(1)).delete(tituloId);
    }

}
