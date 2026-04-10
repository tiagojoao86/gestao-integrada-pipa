package br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.codigoconvenio.entity.CodigoConvenio;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.ProcedimentoRepository;
import br.com.grupopipa.gestaointegrada.atendimento.procedimento.entity.Procedimento;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;

@DisplayName("CodigoConvenioRepository - Testes de Integração")
@Transactional
class CodigoConvenioRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CodigoConvenioRepository repository;

    @Autowired
    private ConvenioRepository convenioRepository;

    @Autowired
    private ProcedimentoRepository procedimentoRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    private Convenio criarEPersistirConvenio() {
        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Operadora " + UUID.randomUUID().toString().substring(0, 8))
                .email("op" + UUID.randomUUID().toString().substring(0, 6) + "@saude.com.br")
                .telefone("1133334444")
                .cnpj("06158095000152")
                .razaoSocial("Operadora Saúde Ltda")
                .build();
        pessoaRepository.save(pessoa);
        pessoaRepository.flush();

        Convenio convenio = new Convenio.Builder()
                .nome("Unimed " + UUID.randomUUID().toString().substring(0, 8))
                .pessoa(pessoa)
                .ativo(true)
                .build();
        Convenio salvo = convenioRepository.save(convenio);
        convenioRepository.flush();
        return salvo;
    }

    private Procedimento criarEPersistirProcedimento() {
        Procedimento procedimento = new Procedimento.Builder()
                .codigo("PROC-" + UUID.randomUUID().toString().substring(0, 8))
                .descricao("Sessão de Terapia ABA")
                .ativo(true)
                .build();
        Procedimento salvo = procedimentoRepository.save(procedimento);
        procedimentoRepository.flush();
        return salvo;
    }

    @Test
    @DisplayName("Deve salvar e recuperar código de convênio")
    void deveSalvarERecuperarCodigo() {
        Convenio convenio = criarEPersistirConvenio();
        Procedimento procedimento = criarEPersistirProcedimento();

        CodigoConvenio codigo = new CodigoConvenio.Builder()
                .convenio(convenio)
                .procedimento(procedimento)
                .codigo("12345")
                .build();

        CodigoConvenio salvo = repository.save(codigo);
        repository.flush();

        Optional<CodigoConvenio> encontrado = repository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("12345", encontrado.get().getCodigo());
        assertNotNull(encontrado.get().getConvenio());
        assertNotNull(encontrado.get().getProcedimento());
        assertEquals(convenio.getId(), encontrado.get().getConvenio().getId());
        assertEquals(procedimento.getId(), encontrado.get().getProcedimento().getId());
    }

    @Test
    @DisplayName("Deve listar códigos por convênio")
    void deveListarCodigosPorConvenio() {
        Convenio convenio = criarEPersistirConvenio();
        Procedimento proc1 = criarEPersistirProcedimento();
        Procedimento proc2 = criarEPersistirProcedimento();

        repository.save(new CodigoConvenio.Builder().convenio(convenio).procedimento(proc1).codigo("AAA").build());
        repository.save(new CodigoConvenio.Builder().convenio(convenio).procedimento(proc2).codigo("BBB").build());
        repository.flush();

        List<CodigoConvenio> codigos = repository.findAllByConvenioId(convenio.getId());
        assertEquals(2, codigos.size());
    }

    @Test
    @DisplayName("Deve deletar todos os códigos de um convênio")
    void deveDeletarTodosCodigosDoConvenio() {
        Convenio convenio = criarEPersistirConvenio();
        Procedimento proc1 = criarEPersistirProcedimento();
        Procedimento proc2 = criarEPersistirProcedimento();

        repository.save(new CodigoConvenio.Builder().convenio(convenio).procedimento(proc1).codigo("X1").build());
        repository.save(new CodigoConvenio.Builder().convenio(convenio).procedimento(proc2).codigo("X2").build());
        repository.flush();

        repository.deleteAllByConvenioId(convenio.getId());
        repository.flush();

        List<CodigoConvenio> codigos = repository.findAllByConvenioId(convenio.getId());
        assertTrue(codigos.isEmpty());
    }

    @Test
    @DisplayName("Deve validar constraint unique de convenio+procedimento")
    void deveValidarConstraintUniqueConvioProcedimento() {
        Convenio convenio = criarEPersistirConvenio();
        Procedimento procedimento = criarEPersistirProcedimento();

        repository.save(new CodigoConvenio.Builder()
                .convenio(convenio).procedimento(procedimento).codigo("COD1").build());
        repository.flush();

        assertThrows(Exception.class, () -> {
            repository.save(new CodigoConvenio.Builder()
                    .convenio(convenio).procedimento(procedimento).codigo("COD2").build());
            repository.flush();
        });
    }

    @Test
    @DisplayName("Deve deletar código de convênio")
    void deveDeletarCodigo() {
        Convenio convenio = criarEPersistirConvenio();
        Procedimento procedimento = criarEPersistirProcedimento();

        CodigoConvenio codigo = new CodigoConvenio.Builder()
                .convenio(convenio).procedimento(procedimento).codigo("DEL").build();
        CodigoConvenio salvo = repository.save(codigo);
        repository.flush();

        repository.deleteById(salvo.getId());
        repository.flush();

        assertFalse(repository.findById(salvo.getId()).isPresent());
    }
}
