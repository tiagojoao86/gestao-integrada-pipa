package br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.atendimento.convenio.ConvenioRepository;
import br.com.grupopipa.gestaointegrada.atendimento.convenio.entity.Convenio;
import br.com.grupopipa.gestaointegrada.atendimento.conveniocategoria.entity.ConvenioCategoria;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.PessoaRepository;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.TipoPessoa;
import br.com.grupopipa.gestaointegrada.cadastro.pessoa.entity.Pessoa;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;

@DisplayName("ConvenioCategoriaRepository - Testes de Integração")
@Transactional
class ConvenioCategoriaRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ConvenioCategoriaRepository repository;

    @Autowired
    private ConvenioRepository convenioRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    // CNPJs válidos pré-definidos para evitar colisão em testes que criam múltiplas pessoas
    private static final String[] CNPJS_VALIDOS = {
        "06158095000152", "11222333000181", "52611565000109"
    };
    private int cnpjIndex = 0;

    private Convenio criarEPersistirConvenio() {
        String cnpj = CNPJS_VALIDOS[cnpjIndex++];
        Pessoa pessoa = new Pessoa.Builder()
                .tipoPessoa(TipoPessoa.JURIDICA)
                .nome("Operadora " + UUID.randomUUID().toString().substring(0, 8))
                .email("op" + UUID.randomUUID().toString().substring(0, 6) + "@saude.com.br")
                .telefone("1133334444")
                .cnpj(cnpj)
                .razaoSocial("Operadora Saúde Ltda")
                .build();

        Pessoa pessoaSalva = pessoaRepository.save(pessoa);
        pessoaRepository.flush();

        Convenio convenio = new Convenio.Builder()
                .nome("Unimed " + UUID.randomUUID().toString().substring(0, 8))
                .pessoa(pessoaSalva)
                .ativo(true)
                .build();

        Convenio convenioSalvo = convenioRepository.save(convenio);
        convenioRepository.flush();
        return convenioSalvo;
    }

    @Test
    @DisplayName("Deve salvar e recuperar categoria de convênio")
    void deveSalvarERecuperarCategoria() {
        Convenio convenio = criarEPersistirConvenio();

        ConvenioCategoria categoria = new ConvenioCategoria.Builder()
                .convenio(convenio)
                .nome("Básico")
                .codigoAnsPlano("ANS001")
                .ativo(true)
                .build();

        ConvenioCategoria salva = repository.save(categoria);
        repository.flush();

        Optional<ConvenioCategoria> encontrada = repository.findById(salva.getId());

        assertTrue(encontrada.isPresent());
        assertEquals("Básico", encontrada.get().getNome());
        assertEquals("ANS001", encontrada.get().getCodigoAnsPlano());
        assertTrue(encontrada.get().getAtivo());
        assertNotNull(encontrada.get().getConvenio());
        assertEquals(convenio.getId(), encontrada.get().getConvenio().getId());
        assertNotNull(encontrada.get().getCreatedAt());
    }

    @Test
    @DisplayName("Deve salvar categoria sem código ANS do plano")
    void deveSalvarCategoriaSemCodigoAns() {
        Convenio convenio = criarEPersistirConvenio();

        ConvenioCategoria categoria = new ConvenioCategoria.Builder()
                .convenio(convenio)
                .nome("Especial " + UUID.randomUUID().toString().substring(0, 6))
                .codigoAnsPlano(null)
                .ativo(true)
                .build();

        ConvenioCategoria salva = repository.save(categoria);
        repository.flush();

        Optional<ConvenioCategoria> encontrada = repository.findById(salva.getId());
        assertTrue(encontrada.isPresent());
        assertNotNull(encontrada.get().getNome());
    }

    @Test
    @DisplayName("Deve deletar categoria de convênio")
    void deveDeletarCategoria() {
        Convenio convenio = criarEPersistirConvenio();

        ConvenioCategoria categoria = new ConvenioCategoria.Builder()
                .convenio(convenio)
                .nome("Para Deletar")
                .ativo(true)
                .build();

        ConvenioCategoria salva = repository.save(categoria);
        repository.flush();

        repository.deleteById(salva.getId());
        repository.flush();

        Optional<ConvenioCategoria> encontrada = repository.findById(salva.getId());
        assertFalse(encontrada.isPresent());
    }

    @Test
    @DisplayName("Deve validar constraint unique de nome por convênio")
    void deveValidarConstraintUniqueNomePorConvenio() {
        Convenio convenio = criarEPersistirConvenio();

        ConvenioCategoria categoria1 = new ConvenioCategoria.Builder()
                .convenio(convenio)
                .nome("Básico Duplicado")
                .ativo(true)
                .build();

        repository.save(categoria1);
        repository.flush();

        ConvenioCategoria categoria2 = new ConvenioCategoria.Builder()
                .convenio(convenio)
                .nome("Básico Duplicado")
                .codigoAnsPlano("ANS999")
                .ativo(true)
                .build();

        assertThrows(Exception.class, () -> {
            repository.save(categoria2);
            repository.flush();
        });
    }

    @Test
    @DisplayName("Deve permitir mesmo nome em convênios diferentes")
    void devePermitirMesmoNomeEmConveniosDiferentes() {
        Convenio convenio1 = criarEPersistirConvenio();
        Convenio convenio2 = criarEPersistirConvenio();

        ConvenioCategoria categoria1 = new ConvenioCategoria.Builder()
                .convenio(convenio1)
                .nome("Básico")
                .ativo(true)
                .build();

        ConvenioCategoria categoria2 = new ConvenioCategoria.Builder()
                .convenio(convenio2)
                .nome("Básico")
                .ativo(true)
                .build();

        ConvenioCategoria salva1 = repository.save(categoria1);
        ConvenioCategoria salva2 = repository.save(categoria2);
        repository.flush();

        assertTrue(repository.findById(salva1.getId()).isPresent());
        assertTrue(repository.findById(salva2.getId()).isPresent());
    }
}
