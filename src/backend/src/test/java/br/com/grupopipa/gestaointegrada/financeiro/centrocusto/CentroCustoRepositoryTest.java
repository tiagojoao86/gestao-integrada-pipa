package br.com.grupopipa.gestaointegrada.financeiro.centrocusto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.financeiro.entity.CentroCusto;

@DisplayName("CentroCustoRepository - Testes de Integração")
@Transactional
class CentroCustoRepositoryTest extends AbstractIntegrationTest {

  @Autowired private CentroCustoRepository repository;

  @Autowired private UnidadeNegocioRepository unidadeNegocioRepository;

  @Test
  @DisplayName("Deve salvar e recuperar centro de custo")
  void deveSalvarERecuperarCentroCusto() {
    // Criar e persistir UnidadeNegocio primeiro (FK requirement)
    UnidadeNegocio unidade =
        new UnidadeNegocio.Builder()
            .codigo("UNREPO" + java.util.UUID.randomUUID().toString().substring(0, 8))
            .nome("Unidade Repo")
            .cnpj("11222333000181")
            .build();

    UnidadeNegocio unidadeSalva = unidadeNegocioRepository.save(unidade);
    unidadeNegocioRepository.flush();

    // Criar CentroCusto com a entidade UnidadeNegocio
    CentroCusto centro =
        new CentroCusto.Builder()
            .nome("Centro Repo")
            .centroResultado(Boolean.FALSE)
            .unidadeNegocio(unidadeSalva)
            .build();

    CentroCusto salvo = repository.save(centro);
    repository.flush();

    // Verificações
    Optional<CentroCusto> encontrado = repository.findById(salvo.getId());
    assertTrue(encontrado.isPresent());
    assertEquals("Centro Repo", encontrado.get().getNome());
    assertNotNull(encontrado.get().getUnidadeNegocio());
    assertEquals(unidadeSalva.getId(), encontrado.get().getUnidadeNegocio().getId());
  }

  @Test
  @DisplayName("Deve deletar centro de custo")
  void deveDeletarCentroCusto() {
    // Criar e persistir UnidadeNegocio primeiro (FK requirement)
    UnidadeNegocio unidade =
        new UnidadeNegocio.Builder()
            .codigo("UNDEL" + java.util.UUID.randomUUID().toString().substring(0, 8))
            .nome("Unidade Delete")
            .cnpj("11222333000181")
            .build();

    UnidadeNegocio unidadeSalva = unidadeNegocioRepository.save(unidade);
    unidadeNegocioRepository.flush();

    // Criar CentroCusto com a entidade UnidadeNegocio
    CentroCusto centro =
        new CentroCusto.Builder()
            .nome("Centro To Delete")
            .centroResultado(Boolean.FALSE)
            .unidadeNegocio(unidadeSalva)
            .build();

    CentroCusto salvo = repository.save(centro);
    repository.flush();

    // Deletar e verificar
    repository.deleteById(salvo.getId());
    repository.flush();

    Optional<CentroCusto> encontrado = repository.findById(salvo.getId());
    assertFalse(encontrado.isPresent());
  }

  @Test
  @DisplayName("Deve validar constraint unique de nome")
  void deveValidarConstraintUniqueNome() {
    // Criar UnidadeNegocio
    UnidadeNegocio unidade =
        new UnidadeNegocio.Builder()
            .codigo("UNUQ" + java.util.UUID.randomUUID().toString().substring(0, 8))
            .nome("Unidade Unique")
            .cnpj("11222333000181")
            .build();

    UnidadeNegocio unidadeSalva = unidadeNegocioRepository.save(unidade);

    // Criar primeiro CentroCusto
    CentroCusto centro1 =
        new CentroCusto.Builder()
            .nome("Centro Duplicado")
            .centroResultado(Boolean.FALSE)
            .unidadeNegocio(unidadeSalva)
            .build();

    repository.save(centro1);
    repository.flush();

    // Tentar criar segundo com mesmo nome (deve falhar)
    CentroCusto centro2 =
        new CentroCusto.Builder()
            .nome("Centro Duplicado")
            .centroResultado(Boolean.TRUE)
            .unidadeNegocio(unidadeSalva)
            .build();

    assertThrows(
        Exception.class,
        () -> {
          repository.save(centro2);
          repository.flush();
        });
  }
}
