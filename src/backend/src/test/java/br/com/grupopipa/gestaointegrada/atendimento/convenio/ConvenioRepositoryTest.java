package br.com.grupopipa.gestaointegrada.atendimento.convenio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;

@DisplayName("ConvenioRepository - Testes de Integração")
@Transactional
class ConvenioRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ConvenioRepository repository;

    @Autowired
    private PessoaRepository pessoaRepository;

    private Pessoa criarEPersistirPessoa() {
        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Operadora Saúde " + UUID.randomUUID().toString().substring(0, 8))
                .email("op" + UUID.randomUUID().toString().substring(0, 6) + "@saude.com.br")
                .telefone("1133334444")
                .cnpj("06158095000152")
                .razaoSocial("Operadora Saúde Ltda")
                .build();

        Pessoa salva = pessoaRepository.save(pessoa);
        pessoaRepository.flush();
        return salva;
    }

    @Test
    @DisplayName("Deve salvar e recuperar convênio")
    void deveSalvarERecuperarConvenio() {
        Pessoa pessoa = criarEPersistirPessoa();

        Convenio convenio = new Convenio.Builder()
                .nome("Unimed " + UUID.randomUUID().toString().substring(0, 8))
                .pessoa(pessoa)
                .registroAns("123456")
                .ativo(true)
                .build();

        Convenio salvo = repository.save(convenio);
        repository.flush();

        Optional<Convenio> encontrado = repository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertNotNull(encontrado.get().getNome());
        assertEquals("123456", encontrado.get().getRegistroAns());
        assertTrue(encontrado.get().getAtivo());
        assertNotNull(encontrado.get().getPessoa());
        assertEquals(pessoa.getId(), encontrado.get().getPessoa().getId());
        assertNotNull(encontrado.get().getCreatedAt());
    }

    @Test
    @DisplayName("Deve salvar convênio sem registro ANS")
    void deveSalvarConvenioSemRegistroAns() {
        Pessoa pessoa = criarEPersistirPessoa();

        Convenio convenio = new Convenio.Builder()
                .nome("Bradesco Saúde " + UUID.randomUUID().toString().substring(0, 8))
                .pessoa(pessoa)
                .registroAns(null)
                .ativo(true)
                .build();

        Convenio salvo = repository.save(convenio);
        repository.flush();

        Optional<Convenio> encontrado = repository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertNull(encontrado.get().getRegistroAns());
    }

    @Test
    @DisplayName("Deve deletar convênio")
    void deveDeletarConvenio() {
        Pessoa pessoa = criarEPersistirPessoa();

        Convenio convenio = new Convenio.Builder()
                .nome("Amil " + UUID.randomUUID().toString().substring(0, 8))
                .pessoa(pessoa)
                .ativo(true)
                .build();

        Convenio salvo = repository.save(convenio);
        repository.flush();

        repository.deleteById(salvo.getId());
        repository.flush();

        Optional<Convenio> encontrado = repository.findById(salvo.getId());
        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("Deve validar constraint unique de registro ANS")
    void deveValidarConstraintUniqueRegistroAns() {
        Pessoa pessoa = criarEPersistirPessoa();
        String registroAns = "REG" + UUID.randomUUID().toString().substring(0, 6);

        Convenio convenio1 = new Convenio.Builder()
                .nome("Convenio Um " + UUID.randomUUID().toString().substring(0, 8))
                .pessoa(pessoa)
                .registroAns(registroAns)
                .ativo(true)
                .build();

        repository.save(convenio1);
        repository.flush();

        Convenio convenio2 = new Convenio.Builder()
                .nome("Convenio Dois " + UUID.randomUUID().toString().substring(0, 8))
                .pessoa(pessoa)
                .registroAns(registroAns)
                .ativo(true)
                .build();

        assertThrows(Exception.class, () -> {
            repository.save(convenio2);
            repository.flush();
        });
    }

    @Test
    @DisplayName("Deve permitir múltiplos convênios sem registro ANS")
    void devePermitirMultiplosConveniosSemRegistroAns() {
        Pessoa pessoa = criarEPersistirPessoa();

        Convenio convenio1 = new Convenio.Builder()
                .nome("Convenio Sem ANS 1 " + UUID.randomUUID().toString().substring(0, 8))
                .pessoa(pessoa)
                .registroAns(null)
                .ativo(true)
                .build();

        Convenio convenio2 = new Convenio.Builder()
                .nome("Convenio Sem ANS 2 " + UUID.randomUUID().toString().substring(0, 8))
                .pessoa(pessoa)
                .registroAns(null)
                .ativo(true)
                .build();

        Convenio salvo1 = repository.save(convenio1);
        Convenio salvo2 = repository.save(convenio2);
        repository.flush();

        assertTrue(repository.findById(salvo1.getId()).isPresent());
        assertTrue(repository.findById(salvo2.getId()).isPresent());
    }
}
