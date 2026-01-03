package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;

/**
 * Testes de integração para PessoaRepository. Valida a persistência de Pessoa
 * com modelo flat
 * (TipoPessoa).
 */
@DisplayName("PessoaRepository - Testes de Integração")
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PessoaRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private PessoaRepository repository;

    @Test
    @DisplayName("Deve salvar e recuperar pessoa física")
    void deveSalvarERecuperarPessoaFisica() {
        // Given
        LocalDate dataNascimento = LocalDate.of(1990, 5, 15);

        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("João Silva")
                .email("joao@exemplo.com")
                .telefone("11987654321")
                .cpf("12345678909")
                .dataNascimento(dataNascimento)
                .build();

        // When
        Pessoa pessoaSalva = repository.save(pessoa);

        // Then
        assertNotNull(pessoaSalva.getId());
        assertEquals(TipoPessoa.FISICA, pessoaSalva.getTipoPessoa());
        assertEquals("João Silva", pessoaSalva.getNome());
        assertEquals("12345678909", pessoaSalva.getCpf());
        assertEquals("joao@exemplo.com", pessoaSalva.getEmail());
        assertEquals("11987654321", pessoaSalva.getTelefone());
        assertEquals(dataNascimento, pessoaSalva.getDataNascimento());
        assertTrue(pessoaSalva.isAtiva());
        assertTrue(pessoaSalva.isPessoaFisica());
        assertNotNull(pessoaSalva.getCreatedAt());
    }

    @Test
    @DisplayName("Deve salvar e recuperar pessoa jurídica")
    void deveSalvarERecuperarPessoaJuridica() {
        // Given
        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Empresa XYZ Ltda")
                .email("contato@empresa.com")
                .telefone("1133334444")
                .cnpj("11222333000181")
                .razaoSocial("Empresa XYZ Comércio Ltda")
                .inscricaoEstadual("123456789")
                .build();

        // When
        Pessoa pessoaSalva = repository.save(pessoa);

        // Then
        assertNotNull(pessoaSalva.getId());
        assertEquals(TipoPessoa.JURIDICA, pessoaSalva.getTipoPessoa());
        assertEquals("Empresa XYZ Ltda", pessoaSalva.getNome());
        assertEquals("11222333000181", pessoaSalva.getCnpj());
        assertEquals("Empresa XYZ Comércio Ltda", pessoaSalva.getRazaoSocial());
        assertEquals("123456789", pessoaSalva.getInscricaoEstadual());
        assertTrue(pessoaSalva.isAtiva());
        assertTrue(pessoaSalva.isPessoaJuridica());
        assertNotNull(pessoaSalva.getCreatedAt());
    }

    @Test
    @DisplayName("Deve atualizar dados da pessoa física")
    void deveAtualizarDadosDaPessoaFisica() {
        // Given
        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("Carlos Souza")
                .email("carlos@exemplo.com")
                .telefone("11987654323")
                .cpf("11144477735")
                .build();
        Pessoa pessoaSalva = repository.save(pessoa);

        // When
        pessoaSalva.atualizar(
                "Carlos Souza Jr",
                "carlos.novo@exemplo.com",
                "11987654999",
                "11144477735",
                null,
                null,
                null,
                null);
        repository.save(pessoaSalva);

        // Then
        Pessoa pessoaAtualizada = repository.findById(pessoaSalva.getId()).orElseThrow();
        assertEquals("Carlos Souza Jr", pessoaAtualizada.getNome());
        assertEquals("carlos.novo@exemplo.com", pessoaAtualizada.getEmail());
        assertEquals("11987654999", pessoaAtualizada.getTelefone());
    }

    @Test
    @DisplayName("Deve atualizar dados da pessoa jurídica")
    void deveAtualizarDadosDaPessoaJuridica() {
        // Given
        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Empresa ABC")
                .email("abc@empresa.com")
                .telefone("1144445555")
                .cnpj("11222333000181")
                .razaoSocial("ABC Comércio Ltda")
                .build();
        Pessoa pessoaSalva = repository.save(pessoa);

        // When
        pessoaSalva.atualizar(
                "Empresa ABC Atualizada",
                "novo@empresa.com",
                "1155556666",
                null,
                null,
                "11222333000181",
                "ABC Comércio e Serviços Ltda",
                "987654321");
        repository.save(pessoaSalva);

        // Then
        Pessoa pessoaAtualizada = repository.findById(pessoaSalva.getId()).orElseThrow();
        assertEquals("Empresa ABC Atualizada", pessoaAtualizada.getNome());
        assertEquals("novo@empresa.com", pessoaAtualizada.getEmail());
        assertEquals("ABC Comércio e Serviços Ltda", pessoaAtualizada.getRazaoSocial());
        assertEquals("987654321", pessoaAtualizada.getInscricaoEstadual());
    }

    @Test
    @DisplayName("Deve inativar pessoa")
    void deveInativarPessoa() {
        // Given
        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("Ana Costa")
                .email("ana@exemplo.com")
                .telefone("11987654324")
                .cpf("79687636068")
                .build();
        Pessoa pessoaSalva = repository.save(pessoa);

        // When
        pessoaSalva.inativar();
        repository.save(pessoaSalva);

        // Then
        Pessoa pessoaInativa = repository.findById(pessoaSalva.getId()).orElseThrow();
        assertFalse(pessoaInativa.isAtiva());
    }

    @Test
    @DisplayName("Deve adicionar observações")
    void deveAdicionarObservacoes() {
        // Given
        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("Pedro Lima")
                .email("pedro@exemplo.com")
                .telefone("11987654325")
                .cpf("41780831048")
                .build();
        Pessoa pessoaSalva = repository.save(pessoa);

        // When
        pessoaSalva.adicionarObservacao("Primeira observação");
        pessoaSalva.adicionarObservacao("Segunda observação");
        repository.save(pessoaSalva);

        // Then
        Pessoa pessoaComObservacoes = repository.findById(pessoaSalva.getId()).orElseThrow();
        assertTrue(pessoaComObservacoes.getObservacoes().contains("Primeira observação"));
        assertTrue(pessoaComObservacoes.getObservacoes().contains("Segunda observação"));
    }

    @Test
    @DisplayName("Deve garantir unicidade do CPF")
    void deveGarantirUnicidadeDoCpf() {
        // Given
        String cpf = "39644439058";
        Pessoa pessoa1 = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("Pessoa Um")
                .email("pessoa1@exemplo.com")
                .telefone("11987654326")
                .cpf(cpf)
                .build();
        repository.save(pessoa1);

        // When/Then
        Pessoa pessoa2 = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("Pessoa Dois")
                .email("pessoa2@exemplo.com")
                .telefone("11987654327")
                .cpf(cpf)
                .build();

        assertThrows(
                Exception.class,
                () -> {
                    repository.save(pessoa2);
                    repository.flush();
                });
    }

    @Test
    @DisplayName("Deve garantir unicidade do CNPJ")
    void deveGarantirUnicidadeDoCnpj() {
        // Given
        String cnpj = "11222333000181";
        Pessoa pessoa1 = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Empresa Um")
                .email("empresa1@exemplo.com")
                .telefone("1133334444")
                .cnpj(cnpj)
                .razaoSocial("Empresa Um Ltda")
                .build();
        repository.save(pessoa1);

        // When/Then
        Pessoa pessoa2 = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Empresa Dois")
                .email("empresa2@exemplo.com")
                .telefone("1144445555")
                .cnpj(cnpj)
                .razaoSocial("Empresa Dois Ltda")
                .build();

        assertThrows(
                Exception.class,
                () -> {
                    repository.save(pessoa2);
                    repository.flush();
                });
    }
}
