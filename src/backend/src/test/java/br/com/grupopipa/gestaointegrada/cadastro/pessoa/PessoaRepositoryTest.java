package br.com.grupopipa.gestaointegrada.cadastro.pessoa;

import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.PessoaFisica;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.core.valueobject.CPF;
import br.com.grupopipa.gestaointegrada.core.valueobject.Email;
import br.com.grupopipa.gestaointegrada.core.valueobject.PhoneNumber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração para PessoaRepository.
 * Valida a persistência de Pessoa Física com herança JOINED.
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
        CPF cpf = new CPF("12345678909");
        Email email = new Email("joao@exemplo.com");
        PhoneNumber telefone = new PhoneNumber("11987654321");
        LocalDate dataNascimento = LocalDate.of(1990, 5, 15);
        
        PessoaFisica pessoa = new PessoaFisica(
            "João Silva",
            email,
            telefone,
            cpf,
            dataNascimento
        );

        // When
        PessoaFisica pessoaSalva = (PessoaFisica) repository.save(pessoa);

        // Then
        assertNotNull(pessoaSalva.getId());
        assertEquals("João Silva", pessoaSalva.getNome());
        assertEquals(cpf, pessoaSalva.getCpf());
        assertEquals(email, pessoaSalva.getEmail());
        assertEquals(telefone, pessoaSalva.getTelefone());
        assertEquals(dataNascimento, pessoaSalva.getDataNascimento());
        assertTrue(pessoaSalva.isAtiva());
        assertNotNull(pessoaSalva.getCreatedAt());
    }

    @Test
    @DisplayName("Deve calcular idade corretamente")
    void deveCalcularIdadeCorretamente() {
        // Given
        LocalDate dataNascimento = LocalDate.now().minusYears(25);
        PessoaFisica pessoa = new PessoaFisica(
            "Maria Santos",
            new Email("maria@exemplo.com"),
            new PhoneNumber("11987654322"),
            new CPF("98765432100"),
            dataNascimento
        );

        // When
        PessoaFisica pessoaSalva = (PessoaFisica) repository.save(pessoa);
        Integer idade = pessoaSalva.calcularIdade();

        // Then
        assertEquals(25, idade);
        assertTrue(pessoaSalva.isMaiorIdade());
    }

    @Test
    @DisplayName("Deve atualizar dados da pessoa")
    void deveAtualizarDadosDaPessoa() {
        // Given
        PessoaFisica pessoa = new PessoaFisica(
            "Carlos Souza",
            new Email("carlos@exemplo.com"),
            new PhoneNumber("11987654323"),
            new CPF("11144477735") // CPF válido
        );
        PessoaFisica pessoaSalva = (PessoaFisica) repository.save(pessoa);

        // When
        Email novoEmail = new Email("carlos.novo@exemplo.com");
        PhoneNumber novoTelefone = new PhoneNumber("11987654999");
        pessoaSalva.atualizar("Carlos Souza Jr", novoEmail, novoTelefone);
        repository.save(pessoaSalva);

        // Then
        PessoaFisica pessoaAtualizada = (PessoaFisica) repository.findById(pessoaSalva.getId()).orElseThrow();
        assertEquals("Carlos Souza Jr", pessoaAtualizada.getNome());
        assertEquals(novoEmail, pessoaAtualizada.getEmail());
        assertEquals(novoTelefone, pessoaAtualizada.getTelefone());
    }

    @Test
    @DisplayName("Deve inativar pessoa")
    void deveInativarPessoa() {
        // Given
        PessoaFisica pessoa = new PessoaFisica(
            "Ana Costa",
            new Email("ana@exemplo.com"),
            new PhoneNumber("11987654324"),
            new CPF("79687636068") // CPF válido
        );
        PessoaFisica pessoaSalva = (PessoaFisica) repository.save(pessoa);

        // When
        pessoaSalva.inativar();
        repository.save(pessoaSalva);

        // Then
        PessoaFisica pessoaInativa = (PessoaFisica) repository.findById(pessoaSalva.getId()).orElseThrow();
        assertFalse(pessoaInativa.isAtiva());
    }

    @Test
    @DisplayName("Deve adicionar observações")
    void deveAdicionarObservacoes() {
        // Given
        PessoaFisica pessoa = new PessoaFisica(
            "Pedro Lima",
            new Email("pedro@exemplo.com"),
            new PhoneNumber("11987654325"),
            new CPF("41780831048") // CPF válido
        );
        PessoaFisica pessoaSalva = (PessoaFisica) repository.save(pessoa);

        // When
        pessoaSalva.adicionarObservacao("Primeira observação");
        pessoaSalva.adicionarObservacao("Segunda observação");
        repository.save(pessoaSalva);

        // Then
        PessoaFisica pessoaComObservacoes = (PessoaFisica) repository.findById(pessoaSalva.getId()).orElseThrow();
        assertTrue(pessoaComObservacoes.getObservacoes().contains("Primeira observação"));
        assertTrue(pessoaComObservacoes.getObservacoes().contains("Segunda observação"));
    }

    @Test
    @DisplayName("Deve garantir unicidade do CPF")
    void deveGarantirUnicidadeDoCpf() {
        // Given - Este teste DEVE gerar SQL Error 23505 (constraint violation)
        CPF cpf = new CPF("39644439058"); // CPF válido
        PessoaFisica pessoa1 = new PessoaFisica(
            "Pessoa Um",
            new Email("pessoa1@exemplo.com"),
            new PhoneNumber("11987654326"),
            cpf
        );
        repository.save(pessoa1);

        // When/Then
        PessoaFisica pessoa2 = new PessoaFisica(
            "Pessoa Dois",
            new Email("pessoa2@exemplo.com"),
            new PhoneNumber("11987654327"),
            cpf
        );

        assertThrows(Exception.class, () -> {
            repository.save(pessoa2);
            repository.flush();
        });
    }
}
