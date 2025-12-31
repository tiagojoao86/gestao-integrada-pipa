package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.grupopipa.gestaointegrada.core.dto.PageDTO;

/**
 * Testes unitários para PessoaController. Usa MockMvc para testar os endpoints REST sem subir o
 * servidor. Usa @MockitoBean para simular o service. @AutoConfigureMockMvc(addFilters = false)
 * desabilita os filtros de segurança, permitindo testar apenas a lógica do controller
 * com @WithMockUser. excludeFilters exclui TenantFilter do contexto (requer JwtDecoder).
 */
@DisplayName("PessoaController - Testes Unitários")
@WebMvcTest(
    value = PessoaController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TenantFilter"))
@AutoConfigureMockMvc(addFilters = false)
class PessoaControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private PessoaService service;

  private PessoaDTO dtoPessoaFisica;
  private PessoaDTO dtoPessoaJuridica;
  private UUID id;

  @BeforeEach
  void setup() {
    id = UUID.randomUUID();

    // Pessoa Física
    dtoPessoaFisica =
        PessoaDTO.builder()
            .id(id)
            .tipoPessoa("FISICA")
            .nome("João da Silva")
            .cpf("12345678909")
            .email("joao@example.com")
            .telefone("11987654321")
            .dataNascimento(LocalDate.of(1990, 1, 15))
            .ativa(true)
            .createdAt(LocalDateTime.now())
            .build();

    // Pessoa Jurídica
    dtoPessoaJuridica =
        PessoaDTO.builder()
            .id(UUID.randomUUID())
            .tipoPessoa("JURIDICA")
            .nome("Empresa XYZ Ltda")
            .cnpj("52611565000109")
            .razaoSocial("XYZ Comércio e Serviços Ltda")
            .inscricaoEstadual("123456789")
            .email("contato@xyz.com.br")
            .telefone("1133334444")
            .ativa(true)
            .createdAt(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("Deve listar pessoas com permissão")
  @WithMockUser(authorities = "CADASTRO_PESSOA_LISTAR")
  void deveListarPessoas() throws Exception {
    // Given
    br.com.grupopipa.gestaointegrada.core.dto.PageRequest pageRequest =
        br.com.grupopipa.gestaointegrada.core.dto.PageRequest.builder()
            .page(0)
            .size(10)
            .order(List.of())
            .build();

    PessoaGridDTO gridDTO1 =
        PessoaGridDTO.builder()
            .id(id)
            .nome("João da Silva")
            .documento("123.456.789-09")
            .tipoPessoa("FISICA")
            .ativa(true)
            .createdAt(LocalDateTime.now())
            .build();

    PessoaGridDTO gridDTO2 =
        PessoaGridDTO.builder()
            .id(UUID.randomUUID())
            .nome("Empresa XYZ Ltda")
            .documento("12.345.678/0001-90")
            .tipoPessoa("JURIDICA")
            .ativa(true)
            .createdAt(LocalDateTime.now())
            .build();

    PageDTO<PessoaGridDTO> pageDTO =
        new PageDTO<>(List.of(gridDTO1, gridDTO2), PageRequest.of(0, 10), 2L);

    when(service.list(any(), any())).thenReturn(pageDTO);

    // When & Then
    mockMvc
        .perform(
            post("/pessoa/query")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.content").isArray())
        .andExpect(jsonPath("$.body.content[0].nome").value("João da Silva"))
        .andExpect(jsonPath("$.body.content[0].documento").value("123.456.789-09"))
        .andExpect(jsonPath("$.body.content[0].tipoPessoa").value("FISICA"))
        .andExpect(jsonPath("$.body.content[1].nome").value("Empresa XYZ Ltda"))
        .andExpect(jsonPath("$.body.content[1].tipoPessoa").value("JURIDICA"))
        .andExpect(jsonPath("$.body.totalElements").value(2));

    verify(service, times(1)).list(any(), any());
  }

  @Test
  @DisplayName("Deve criar nova pessoa física com permissão")
  @WithMockUser(authorities = "CADASTRO_PESSOA_EDITAR")
  void deveCriarNovaPessoaFisica() throws Exception {
    // Given
    PessoaDTO novaPessoa =
        PessoaDTO.builder()
            .tipoPessoa("FISICA")
            .nome("Maria Santos")
            .cpf("98765432100")
            .email("maria@example.com")
            .telefone("11999887766")
            .dataNascimento(LocalDate.of(1995, 5, 20))
            .ativa(true)
            .build();

    PessoaDTO pessoaSalva =
        PessoaDTO.builder()
            .id(UUID.randomUUID())
            .tipoPessoa("FISICA")
            .nome("Maria Santos")
            .cpf("98765432100")
            .email("maria@example.com")
            .telefone("11999887766")
            .dataNascimento(LocalDate.of(1995, 5, 20))
            .ativa(true)
            .createdAt(LocalDateTime.now())
            .build();

    when(service.save(any(PessoaDTO.class))).thenReturn(pessoaSalva);

    // When & Then
    mockMvc
        .perform(
            post("/pessoa")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(novaPessoa)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.nome").value("Maria Santos"))
        .andExpect(jsonPath("$.body.cpf").value("98765432100"))
        .andExpect(jsonPath("$.body.tipoPessoa").value("FISICA"));

    verify(service, times(1)).save(any(PessoaDTO.class));
  }

  @Test
  @DisplayName("Deve criar nova pessoa jurídica com permissão")
  @WithMockUser(authorities = "CADASTRO_PESSOA_EDITAR")
  void deveCriarNovaPessoaJuridica() throws Exception {
    // Given
    when(service.save(any(PessoaDTO.class))).thenReturn(dtoPessoaJuridica);

    // When & Then
    mockMvc
        .perform(
            post("/pessoa")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoPessoaJuridica)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.nome").value("Empresa XYZ Ltda"))
        .andExpect(jsonPath("$.body.cnpj").value("52611565000109"))
        .andExpect(jsonPath("$.body.tipoPessoa").value("JURIDICA"))
        .andExpect(jsonPath("$.body.razaoSocial").value("XYZ Comércio e Serviços Ltda"));

    verify(service, times(1)).save(any(PessoaDTO.class));
  }

  @Test
  @DisplayName("Deve atualizar pessoa existente com permissão")
  @WithMockUser(authorities = "CADASTRO_PESSOA_EDITAR")
  void deveAtualizarPessoa() throws Exception {
    // Given
    dtoPessoaFisica.setNome("João da Silva Santos");
    dtoPessoaFisica.setEmail("joao.santos@example.com");

    when(service.save(any(PessoaDTO.class))).thenReturn(dtoPessoaFisica);

    // When & Then
    mockMvc
        .perform(
            post("/pessoa")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoPessoaFisica)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.nome").value("João da Silva Santos"))
        .andExpect(jsonPath("$.body.email").value("joao.santos@example.com"));

    verify(service, times(1)).save(any(PessoaDTO.class));
  }

  @Test
  @DisplayName("Deve buscar pessoa por ID com permissão")
  @WithMockUser(authorities = "CADASTRO_PESSOA_VISUALIZAR")
  void deveBuscarPessoaPorId() throws Exception {
    // Given
    when(service.findById(id)).thenReturn(dtoPessoaFisica);

    // When & Then
    mockMvc
        .perform(get("/pessoa/find-by-id").param("id", id.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body.id").value(id.toString()))
        .andExpect(jsonPath("$.body.nome").value("João da Silva"))
        .andExpect(jsonPath("$.body.cpf").value("12345678909"));

    verify(service, times(1)).findById(id);
  }

  @Test
  @DisplayName("Deve deletar pessoa com permissão")
  @WithMockUser(authorities = "CADASTRO_PESSOA_DELETAR")
  void deveDeletarPessoa() throws Exception {
    // Given
    when(service.delete(id)).thenReturn(id);

    // When & Then
    mockMvc
        .perform(delete("/pessoa/{id}", id).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.body").value(id.toString()));

    verify(service, times(1)).delete(id);
  }
}
