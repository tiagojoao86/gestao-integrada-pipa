package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.config.AbstractIntegrationTest;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;

/**
 * Testes de integração para ContaBancariaRepository. Valida a persistência e consultas de contas
 * bancárias.
 */
@DisplayName("ContaBancariaRepository - Testes de Integração")
@Transactional
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ContaBancariaRepositoryTest extends AbstractIntegrationTest {

  @Autowired private UnidadeNegocioRepository unidadeNegocioRepository;

  @Autowired private ContaBancariaRepository repository;

  private UnidadeNegocio criarUnidadeNegocio(String codigoSufixo) {
    return new UnidadeNegocio.Builder()
        .codigo("UN" + codigoSufixo + java.util.UUID.randomUUID().toString().substring(0, 8))
        .nome("Unidade Teste " + codigoSufixo)
        .cnpj("11222333000181")
        .build();
  }

  @Test
  @DisplayName("Deve salvar e recuperar conta bancária")
  void deveSalvarERecuperarContaBancaria() {
    UnidadeNegocio unidadeNegocio = criarUnidadeNegocio("001");
    unidadeNegocioRepository.save(unidadeNegocio);
    // Given
    ContaBancaria conta =
        new ContaBancaria.Builder()
            .nome("Conta Corrente Principal")
            .tipo(TipoConta.CORRENTE)
            .banco("Banco do Brasil")
            .agencia("1234")
            .numeroConta("12345-6")
            .saldoInicial(Money.of(BigDecimal.valueOf(5000.00)))
            .unidadeNegocio(unidadeNegocio)
            .build();

    // When
    ContaBancaria contaSalva = repository.save(conta);

    // Then
    assertNotNull(contaSalva.getId());
    assertEquals("Conta Corrente Principal", contaSalva.getNome());
    assertEquals(TipoConta.CORRENTE, contaSalva.getTipo());
    assertEquals("Banco do Brasil", contaSalva.getBanco());
    assertEquals("1234", contaSalva.getAgencia());
    assertEquals("12345-6", contaSalva.getNumeroConta());
    assertNotNull(contaSalva.getCreatedAt());
  }

  @Test
  @DisplayName("Deve buscar conta bancária por ID")
  void deveBuscarContaBancariaPorId() {
    UnidadeNegocio unidadeNegocio = criarUnidadeNegocio("002");
    unidadeNegocioRepository.save(unidadeNegocio);
    // Given
    ContaBancaria conta =
        new ContaBancaria.Builder()
            .nome("Conta Poupança")
            .tipo(TipoConta.POUPANCA)
            .saldoInicial(Money.zero())
            .unidadeNegocio(unidadeNegocio)
            .build();
    ContaBancaria contaSalva = repository.save(conta);

    // When
    Optional<ContaBancaria> resultado = repository.findById(contaSalva.getId());

    // Then
    assertTrue(resultado.isPresent());
    assertEquals("Conta Poupança", resultado.get().getNome());
    assertEquals(TipoConta.POUPANCA, resultado.get().getTipo());
  }

  @Test
  @DisplayName("Deve deletar conta bancária")
  void deveDeletarContaBancaria() {
    UnidadeNegocio unidadeNegocio = criarUnidadeNegocio("003");
    unidadeNegocioRepository.save(unidadeNegocio);
    // Given
    ContaBancaria conta =
        new ContaBancaria.Builder()
            .nome("Conta Temporária")
            .tipo(TipoConta.CORRENTE)
            .saldoInicial(Money.zero())
            .unidadeNegocio(unidadeNegocio)
            .build();
    ContaBancaria contaSalva = repository.save(conta);

    // When
    repository.delete(contaSalva);
    Optional<ContaBancaria> resultado = repository.findById(contaSalva.getId());

    // Then
    assertFalse(resultado.isPresent());
  }

  @Test
  @DisplayName("Deve validar campos obrigatórios")
  void deveValidarCamposObrigatorios() {
    UnidadeNegocio unidadeNegocio = criarUnidadeNegocio("004");
    unidadeNegocioRepository.save(unidadeNegocio);
    // Given
    ContaBancaria conta =
        new ContaBancaria.Builder()
            .nome("Conta Validação")
            .tipo(TipoConta.CORRENTE)
            .saldoInicial(Money.of(BigDecimal.valueOf(1000.00)))
            .unidadeNegocio(unidadeNegocio)
            .build();

    // When
    ContaBancaria contaSalva = repository.save(conta);

    // Then
    assertNotNull(contaSalva.getId());
    assertNotNull(contaSalva.getNome());
    assertNotNull(contaSalva.getTipo());
    assertNotNull(contaSalva.getCreatedAt());
  }
}
