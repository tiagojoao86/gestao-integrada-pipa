package br.com.grupopipa.gestaointegrada.cadastro.setor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.cadastro.setor.entity.Setor;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.financeiro.centrocusto.CentroCustoRepository;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CentroCusto;

@DisplayName("SetorRepository - Testes de Integração")
@Transactional
class SetorRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private SetorRepository repository;

    @Autowired
    private CentroCustoRepository centroCustoRepository;

    @Autowired
    private UnidadeNegocioRepository unidadeNegocioRepository;

    @Test
    @DisplayName("Deve salvar e recuperar setor")
    void deveSalvarERecuperarSetor() {
        // Criar e persistir UnidadeNegocio e CentroCusto primeiro (FK requirement)
        UnidadeNegocio unidade = new UnidadeNegocio.Builder()
                .codigo("UNREPO" + java.util.UUID.randomUUID().toString().substring(0, 8))
                .nome("Unidade Setor Repo")
                .cnpj("11222333000181")
                .build();

        UnidadeNegocio unidadeSalva = unidadeNegocioRepository.save(unidade);
        unidadeNegocioRepository.flush();

        CentroCusto centroCusto = new CentroCusto.Builder()
                .nome(
                        "Centro Custo Setor Repo " + java.util.UUID.randomUUID().toString().substring(0, 8))
                .centroResultado(Boolean.FALSE)
                .unidadeNegocio(unidadeSalva)
                .build();

        CentroCusto centroCustoSalvo = centroCustoRepository.save(centroCusto);
        centroCustoRepository.flush();

        // Criar Setor com a entidade CentroCusto
        Setor setor = new Setor.Builder()
                .nome("Setor Repo")
                .descricao("Descrição do setor repo")
                .centroCusto(centroCustoSalvo)
                .build();

        Setor salvo = repository.save(setor);
        repository.flush();

        // Verificações
        Optional<Setor> encontrado = repository.findById(salvo.getId());
        assertTrue(encontrado.isPresent());
        assertEquals("Setor Repo", encontrado.get().getNome());
        assertEquals("Descrição do setor repo", encontrado.get().getDescricao());
        assertNotNull(encontrado.get().getCentroCusto());
        assertEquals(centroCustoSalvo.getId(), encontrado.get().getCentroCusto().getId());
    }

    @Test
    @DisplayName("Deve deletar setor")
    void deveDeletarSetor() {
        // Criar e persistir UnidadeNegocio e CentroCusto primeiro (FK requirement)
        UnidadeNegocio unidade = new UnidadeNegocio.Builder()
                .codigo("UNDEL" + java.util.UUID.randomUUID().toString().substring(0, 8))
                .nome("Unidade Delete Setor")
                .cnpj("11222333000181")
                .build();

        UnidadeNegocio unidadeSalva = unidadeNegocioRepository.save(unidade);
        unidadeNegocioRepository.flush();

        CentroCusto centroCusto = new CentroCusto.Builder()
                .nome("Centro Custo Delete " + java.util.UUID.randomUUID().toString().substring(0, 8))
                .centroResultado(Boolean.FALSE)
                .unidadeNegocio(unidadeSalva)
                .build();

        CentroCusto centroCustoSalvo = centroCustoRepository.save(centroCusto);
        centroCustoRepository.flush();

        // Criar Setor
        Setor setor = new Setor.Builder().nome("Setor To Delete").centroCusto(centroCustoSalvo).build();

        Setor salvo = repository.save(setor);
        repository.flush();

        // Deletar e verificar
        repository.deleteById(salvo.getId());
        repository.flush();

        Optional<Setor> encontrado = repository.findById(salvo.getId());
        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("Deve validar constraint unique de nome")
    void deveValidarConstraintUniqueNome() {
        // Criar UnidadeNegocio e CentroCusto
        UnidadeNegocio unidade = new UnidadeNegocio.Builder()
                .codigo("UNUQ" + java.util.UUID.randomUUID().toString().substring(0, 8))
                .nome("Unidade Unique Setor")
                .cnpj("11222333000181")
                .build();

        UnidadeNegocio unidadeSalva = unidadeNegocioRepository.save(unidade);

        CentroCusto centroCusto = new CentroCusto.Builder()
                .nome("Centro Custo Unique " + java.util.UUID.randomUUID().toString().substring(0, 8))
                .centroResultado(Boolean.FALSE)
                .unidadeNegocio(unidadeSalva)
                .build();

        CentroCusto centroCustoSalvo = centroCustoRepository.save(centroCusto);

        // Criar primeiro Setor
        Setor setor1 = new Setor.Builder().nome("Setor Duplicado").centroCusto(centroCustoSalvo).build();

        repository.save(setor1);
        repository.flush();

        // Tentar criar segundo com mesmo nome (deve falhar)
        Setor setor2 = new Setor.Builder()
                .nome("Setor Duplicado")
                .descricao("Outra descrição")
                .centroCusto(centroCustoSalvo)
                .build();

        assertThrows(
                Exception.class,
                () -> {
                    repository.save(setor2);
                    repository.flush();
                });
    }
}
