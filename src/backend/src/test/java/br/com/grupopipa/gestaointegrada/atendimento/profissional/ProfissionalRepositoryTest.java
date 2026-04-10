package br.com.grupopipa.gestaointegrada.atendimento.profissional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.profissional.entity.Profissional;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;

@DisplayName("ProfissionalRepository - Testes de Integração")
@Transactional
class ProfissionalRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ProfissionalRepository repository;

    @Autowired
    private PessoaRepository pessoaRepository;

    private Pessoa criarEPersistirPessoa() {
        // CPF válido: 12345678909
        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.FISICA)
                .nome("Profissional Teste " + UUID.randomUUID().toString().substring(0, 8))
                .email("prof" + UUID.randomUUID().toString().substring(0, 6) + "@example.com")
                .telefone("11987654321")
                .cpf("12345678909")
                .dataNascimento(LocalDate.of(1985, 5, 10))
                .build();

        Pessoa salva = pessoaRepository.save(pessoa);
        pessoaRepository.flush();
        return salva;
    }

    @Test
    @DisplayName("Deve salvar e recuperar profissional")
    void deveSalvarERecuperarProfissional() {
        Pessoa pessoa = criarEPersistirPessoa();

        Profissional profissional = new Profissional.Builder()
                .pessoa(pessoa)
                .conselho("CRP")
                .codigoConselho("CRP-06/12345")
                .tipoRemuneracao(TipoRemuneracao.CLT)
                .banco("Nubank")
                .conta("12345-6")
                .ativo(true)
                .build();

        Profissional salvo = repository.save(profissional);
        repository.flush();

        Optional<Profissional> encontrado = repository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("CRP", encontrado.get().getConselho());
        assertEquals("CRP-06/12345", encontrado.get().getCodigoConselho());
        assertEquals(TipoRemuneracao.CLT, encontrado.get().getTipoRemuneracao());
        assertEquals("Nubank", encontrado.get().getBanco());
        assertTrue(encontrado.get().getAtivo());
        assertNotNull(encontrado.get().getPessoa());
        assertEquals(pessoa.getId(), encontrado.get().getPessoa().getId());
        assertNotNull(encontrado.get().getCreatedAt());
    }

    @Test
    @DisplayName("Deve salvar profissional sem dados bancários opcionais")
    void deveSalvarProfissionalSemDadosBancarios() {
        Pessoa pessoa = criarEPersistirPessoa();

        Profissional profissional = new Profissional.Builder()
                .pessoa(pessoa)
                .conselho("CRM")
                .codigoConselho("CRM-SP/98765")
                .tipoRemuneracao(TipoRemuneracao.PJ)
                .ativo(true)
                .build();

        Profissional salvo = repository.save(profissional);
        repository.flush();

        Optional<Profissional> encontrado = repository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("CRM", encontrado.get().getConselho());
        assertEquals(TipoRemuneracao.PJ, encontrado.get().getTipoRemuneracao());
        assertNotNull(encontrado.get().getPessoa());
    }

    @Test
    @DisplayName("Deve deletar profissional")
    void deveDeletarProfissional() {
        Pessoa pessoa = criarEPersistirPessoa();

        Profissional profissional = new Profissional.Builder()
                .pessoa(pessoa)
                .conselho("CRP")
                .codigoConselho("CRP-06/00001")
                .tipoRemuneracao(TipoRemuneracao.CLT)
                .ativo(true)
                .build();

        Profissional salvo = repository.save(profissional);
        repository.flush();

        repository.deleteById(salvo.getId());
        repository.flush();

        Optional<Profissional> encontrado = repository.findById(salvo.getId());
        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("Deve salvar profissional inativo")
    void deveSalvarProfissionalInativo() {
        Pessoa pessoa = criarEPersistirPessoa();

        Profissional profissional = new Profissional.Builder()
                .pessoa(pessoa)
                .conselho("CREFONO")
                .codigoConselho("CREFONO-3R/55555")
                .tipoRemuneracao(TipoRemuneracao.HORA)
                .ativo(false)
                .build();

        Profissional salvo = repository.save(profissional);
        repository.flush();

        Optional<Profissional> encontrado = repository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertFalse(encontrado.get().getAtivo());
    }
}
