package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.core.dao.Specifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para PessoaService.
 * Usa Mockito para simular dependências (repository).
 */
@DisplayName("PessoaService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class PessoaServiceTest {

    @Mock
    private PessoaRepository repository;

    @Mock
    private Specifications<Pessoa> specifications;

    @InjectMocks
    private PessoaServiceImpl service;

    private PessoaDTO dtoPessoaFisica;
    private PessoaDTO dtoPessoaJuridica;
    private Pessoa pessoaFisica;
    private Pessoa pessoaJuridica;

    @BeforeEach
    void setup() {
        // Pessoa Física
        dtoPessoaFisica = PessoaDTO.builder()
                .tipoPessoa("FISICA")
                .nome("João da Silva")
                .cpf("12345678909")
                .email("joao@example.com")
                .telefone("11987654321")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .observacoes("Cliente VIP")
                .ativa(true)
                .build();

        pessoaFisica = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("João da Silva")
                .email("joao@example.com")
                .telefone("11987654321")
                .cpf("12345678909")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .build();

        // Pessoa Jurídica (CNPJ válido: 06.158.095/0001-52)
        dtoPessoaJuridica = PessoaDTO.builder()
                .tipoPessoa("JURIDICA")
                .nome("Empresa XYZ Ltda")
                .cnpj("06158095000152")
                .razaoSocial("XYZ Comércio e Serviços Ltda")
                .inscricaoEstadual("123456789")
                .email("contato@xyz.com.br")
                .telefone("1133334444")
                .ativa(true)
                .build();

        pessoaJuridica = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Empresa XYZ Ltda")
                .email("contato@xyz.com.br")
                .telefone("1133334444")
                .cnpj("06158095000152")
                .razaoSocial("XYZ Comércio e Serviços Ltda")
                .inscricaoEstadual("123456789")
                .build();
    }

    @Test
    @DisplayName("Deve criar nova pessoa física")
    void deveCriarNovaPessoaFisica() {
        // Given
        when(repository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PessoaDTO resultado = service.save(dtoPessoaFisica);

        // Then
        assertNotNull(resultado);
        assertEquals("FISICA", resultado.getTipoPessoa());
        assertEquals("João da Silva", resultado.getNome());
        assertEquals("12345678909", resultado.getCpf());
        assertEquals("joao@example.com", resultado.getEmail());
        assertEquals("11987654321", resultado.getTelefone());
        assertEquals(LocalDate.of(1990, 1, 15), resultado.getDataNascimento());
        assertTrue(resultado.getAtiva());

        verify(repository, times(1)).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve criar nova pessoa jurídica")
    void deveCriarNovaPessoaJuridica() {
        // Given
        when(repository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PessoaDTO resultado = service.save(dtoPessoaJuridica);

        // Then
        assertNotNull(resultado);
        assertEquals("JURIDICA", resultado.getTipoPessoa());
        assertEquals("Empresa XYZ Ltda", resultado.getNome());
        assertEquals("06158095000152", resultado.getCnpj());
        assertEquals("XYZ Comércio e Serviços Ltda", resultado.getRazaoSocial());
        assertEquals("123456789", resultado.getInscricaoEstadual());
        assertEquals("contato@xyz.com.br", resultado.getEmail());
        assertTrue(resultado.getAtiva());

        verify(repository, times(1)).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve atualizar pessoa física existente")
    void deveAtualizarPessoaFisicaExistente() {
        // Given
        UUID id = UUID.randomUUID();
        dtoPessoaFisica.setId(id);
        dtoPessoaFisica.setNome("João da Silva Santos");
        dtoPessoaFisica.setEmail("joao.santos@example.com");
        dtoPessoaFisica.setDataNascimento(LocalDate.of(1990, 2, 20));

        when(repository.findById(id)).thenReturn(Optional.of(pessoaFisica));
        when(repository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PessoaDTO resultado = service.save(dtoPessoaFisica);

        // Then
        assertNotNull(resultado);
        assertEquals("João da Silva Santos", resultado.getNome());
        assertEquals("joao.santos@example.com", resultado.getEmail());
        assertEquals(LocalDate.of(1990, 2, 20), resultado.getDataNascimento());

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve atualizar pessoa jurídica existente")
    void deveAtualizarPessoaJuridicaExistente() {
        // Given
        UUID id = UUID.randomUUID();
        dtoPessoaJuridica.setId(id);
        dtoPessoaJuridica.setNome("Empresa XYZ LTDA ME");
        dtoPessoaJuridica.setRazaoSocial("XYZ Comércio LTDA ME");

        when(repository.findById(id)).thenReturn(Optional.of(pessoaJuridica));
        when(repository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PessoaDTO resultado = service.save(dtoPessoaJuridica);

        // Then
        assertNotNull(resultado);
        assertEquals("Empresa XYZ LTDA ME", resultado.getNome());
        assertEquals("XYZ Comércio LTDA ME", resultado.getRazaoSocial());

        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve inativar pessoa")
    void deveInativarPessoa() {
        // Given
        UUID id = UUID.randomUUID();
        dtoPessoaFisica.setId(id);
        dtoPessoaFisica.setAtiva(false);

        when(repository.findById(id)).thenReturn(Optional.of(pessoaFisica));
        when(repository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PessoaDTO resultado = service.save(dtoPessoaFisica);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.getAtiva());

        verify(repository, times(1)).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve ativar pessoa")
    void deveAtivarPessoa() {
        // Given
        UUID id = UUID.randomUUID();
        pessoaFisica.inativar(); // Começa inativa
        dtoPessoaFisica.setId(id);
        dtoPessoaFisica.setAtiva(true);

        when(repository.findById(id)).thenReturn(Optional.of(pessoaFisica));
        when(repository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PessoaDTO resultado = service.save(dtoPessoaFisica);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.getAtiva());

        verify(repository, times(1)).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve adicionar observações em nova pessoa")
    void deveAdicionarObservacoesEmNovaPessoa() {
        // Given
        dtoPessoaFisica.setObservacoes("Cliente preferencial com desconto de 10%");
        when(repository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        PessoaDTO resultado = service.save(dtoPessoaFisica);

        // Then
        assertNotNull(resultado);
        assertEquals("Cliente preferencial com desconto de 10%", resultado.getObservacoes());

        verify(repository, times(1)).save(any(Pessoa.class));
    }

    @Test
    @DisplayName("Deve buscar pessoa física por ID")
    void deveBuscarPessoaFisicaPorId() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(pessoaFisica));

        // When
        PessoaDTO resultado = service.findById(id);

        // Then
        assertNotNull(resultado);
        assertEquals("FISICA", resultado.getTipoPessoa());
        assertEquals("João da Silva", resultado.getNome());
        assertEquals("12345678909", resultado.getCpf());

        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve buscar pessoa jurídica por ID")
    void deveBuscarPessoaJuridicaPorId() {
        // Given
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(pessoaJuridica));

        // When
        PessoaDTO resultado = service.findById(id);

        // Then
        assertNotNull(resultado);
        assertEquals("JURIDICA", resultado.getTipoPessoa());
        assertEquals("Empresa XYZ Ltda", resultado.getNome());
        assertEquals("06158095000152", resultado.getCnpj());
        assertEquals("XYZ Comércio e Serviços Ltda", resultado.getRazaoSocial());

        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Deve deletar pessoa")
    void deveDeletarPessoa() {
        // Given
        UUID id = UUID.randomUUID();
        doNothing().when(repository).deleteById(id);

        // When
        UUID resultadoId = service.delete(id);

        // Then
        assertEquals(id, resultadoId);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve construir DTO corretamente de pessoa física")
    void deveConstruirDTOCorretamenteDePessoaFisica() {
        // When
        PessoaDTO dto = service.buildDTOFromEntity(pessoaFisica);

        // Then
        assertNotNull(dto);
        assertEquals("FISICA", dto.getTipoPessoa());
        assertEquals("João da Silva", dto.getNome());
        assertEquals("12345678909", dto.getCpf());
        assertEquals("joao@example.com", dto.getEmail());
        assertEquals(LocalDate.of(1990, 1, 15), dto.getDataNascimento());
    }

    @Test
    @DisplayName("Deve construir DTO corretamente de pessoa jurídica")
    void deveConstruirDTOCorretamenteDePessoaJuridica() {
        // When
        PessoaDTO dto = service.buildDTOFromEntity(pessoaJuridica);

        // Then
        assertNotNull(dto);
        assertEquals("JURIDICA", dto.getTipoPessoa());
        assertEquals("Empresa XYZ Ltda", dto.getNome());
        assertEquals("06158095000152", dto.getCnpj());
        assertEquals("XYZ Comércio e Serviços Ltda", dto.getRazaoSocial());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente de pessoa física")
    void deveConstruirGridDTOCorretamenteDePessoaFisica() {
        // When
        PessoaGridDTO gridDTO = service.buildGridDTOFromEntity(pessoaFisica);

        // Then
        assertNotNull(gridDTO);
        assertEquals("João da Silva", gridDTO.getNome());
        assertEquals("12345678909", gridDTO.getDocumento());
        assertEquals("FISICA", gridDTO.getTipoPessoa());
        assertTrue(gridDTO.getAtiva());
    }

    @Test
    @DisplayName("Deve construir GridDTO corretamente de pessoa jurídica")
    void deveConstruirGridDTOCorretamenteDePessoaJuridica() {
        // When
        PessoaGridDTO gridDTO = service.buildGridDTOFromEntity(pessoaJuridica);

        // Then
        assertNotNull(gridDTO);
        assertEquals("Empresa XYZ Ltda", gridDTO.getNome());
        assertEquals("06158095000152", gridDTO.getDocumento());
        assertEquals("JURIDICA", gridDTO.getTipoPessoa());
        assertTrue(gridDTO.getAtiva());
    }

    @Test
    @DisplayName("Deve fazer merge de entidade nova pessoa física com DTO")
    void deveFazerMergeDeEntidadeNovaPessoaFisicaComDTO() {
        // When
        Pessoa resultado = service.mergeEntityAndDTO(null, dtoPessoaFisica);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoPessoa.FISICA, resultado.getTipoPessoa());
        assertEquals("João da Silva", resultado.getNome());
        assertEquals("12345678909", resultado.getCpf());
        assertEquals(LocalDate.of(1990, 1, 15), resultado.getDataNascimento());
        assertTrue(resultado.isPessoaFisica());
    }

    @Test
    @DisplayName("Deve fazer merge de entidade nova pessoa jurídica com DTO")
    void deveFazerMergeDeEntidadeNovaPessoaJuridicaComDTO() {
        // When
        Pessoa resultado = service.mergeEntityAndDTO(null, dtoPessoaJuridica);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoPessoa.JURIDICA, resultado.getTipoPessoa());
        assertEquals("Empresa XYZ Ltda", resultado.getNome());
        assertEquals("06158095000152", resultado.getCnpj());
        assertEquals("XYZ Comércio e Serviços Ltda", resultado.getRazaoSocial());
        assertTrue(resultado.isPessoaJuridica());
    }

    @Test
    @DisplayName("Deve fazer merge de entidade existente pessoa física com DTO")
    void deveFazerMergeDeEntidadeExistentePessoaFisicaComDTO() {
        // Given
        dtoPessoaFisica.setNome("João Santos Silva");
        dtoPessoaFisica.setEmail("joao.novo@example.com");

        // When
        Pessoa resultado = service.mergeEntityAndDTO(pessoaFisica, dtoPessoaFisica);

        // Then
        assertNotNull(resultado);
        assertEquals(TipoPessoa.FISICA, resultado.getTipoPessoa());
        assertEquals("João Santos Silva", resultado.getNome());
        assertEquals("joao.novo@example.com", resultado.getEmail());
    }

    @Test
    @DisplayName("Deve lançar exceção para tipo de pessoa inválido")
    void deveLancarExcecaoParaTipoPessoaInvalido() {
        // Given
        PessoaDTO dtoInvalido = PessoaDTO.builder()
                .tipoPessoa("INVALIDO")
                .nome("Teste")
                .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.save(dtoInvalido));

        assertTrue(exception.getMessage().contains("INVALIDO"));
        verify(repository, never()).save(any(Pessoa.class));
    }

}
