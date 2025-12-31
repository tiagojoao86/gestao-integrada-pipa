package br.com.grupopipa.gestaointegrada.financeiro.contabancaria;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.UnidadeNegocioRepository;
import br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity.UnidadeNegocio;
import br.com.grupopipa.gestaointegrada.core.valueobject.Money;
import br.com.grupopipa.gestaointegrada.financeiro.entity.ContaBancaria;
import br.com.grupopipa.gestaointegrada.financeiro.enums.TipoConta;

/** Testes unitários para ContaBancariaService. Testa a lógica de negócio de contas bancárias. */
@DisplayName("ContaBancariaService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
class ContaBancariaServiceTest {

  @Mock private ContaBancariaRepository repository;

  @Mock private UnidadeNegocioRepository unidadeNegocioRepository;

  @InjectMocks private ContaBancariaServiceImpl service;

  private ContaBancariaDTO dtoValido;
  private ContaBancaria entidadeValida;
  private UUID contaId;

  @BeforeEach
  void setup() {
    contaId = UUID.randomUUID();

    UnidadeNegocio unidadeNegocio =
        new UnidadeNegocio.Builder()
            .codigo("UN001")
            .nome("Unidade Teste")
            .cnpj("11222333000181")
            .build();

    lenient()
        .when(unidadeNegocioRepository.findById(any()))
        .thenReturn(Optional.of(unidadeNegocio));

    dtoValido =
        ContaBancariaDTO.builder()
            .nome("Conta Corrente Principal")
            .banco("Banco do Brasil")
            .agencia("1234")
            .numeroConta("12345-6")
            .tipo(TipoConta.CORRENTE.name())
            .saldoInicial(BigDecimal.valueOf(1000.00))
            .unidadeNegocioId(unidadeNegocio.getId())
            .build();

    entidadeValida =
        new ContaBancaria.Builder()
            .nome("Conta Corrente Principal")
            .tipo(TipoConta.CORRENTE)
            .banco("Banco do Brasil")
            .agencia("1234")
            .numeroConta("12345-6")
            .saldoInicial(Money.of(BigDecimal.valueOf(1000.00)))
            .unidadeNegocio(unidadeNegocio)
            .build();
  }

  @Test
  @DisplayName("Deve criar nova conta bancária")
  void deveCriarNovaContaBancaria() {
    // Given
    when(repository.save(any(ContaBancaria.class))).thenReturn(entidadeValida);

    // When
    ContaBancariaDTO resultado = service.save(dtoValido);

    // Then
    assertNotNull(resultado);
    assertEquals("Conta Corrente Principal", resultado.getNome());
    assertEquals(TipoConta.CORRENTE.name(), resultado.getTipo());
    verify(repository, times(1)).save(any(ContaBancaria.class));
  }

  @Test
  @DisplayName("Deve buscar conta bancária por ID")
  void deveBuscarContaBancariaPorId() {
    // Given
    when(repository.findById(contaId)).thenReturn(Optional.of(entidadeValida));

    // When
    ContaBancariaDTO resultado = service.findById(contaId);

    // Then
    assertNotNull(resultado);
    assertEquals("Conta Corrente Principal", resultado.getNome());
    verify(repository, times(1)).findById(contaId);
  }

  @Test
  @DisplayName("Deve deletar conta bancária")
  void deveDeletarContaBancaria() {
    // Given
    doNothing().when(repository).deleteById(contaId);

    // When
    UUID resultado = service.delete(contaId);

    // Then
    assertEquals(contaId, resultado);
    verify(repository, times(1)).deleteById(contaId);
  }

  @Test
  @DisplayName("Deve construir DTO corretamente da entidade")
  void deveConstruirDTOCorretamenteDaEntidade() {
    // When
    ContaBancariaDTO dto = service.buildDTOFromEntity(entidadeValida);

    // Then
    assertNotNull(dto);
    assertEquals("Conta Corrente Principal", dto.getNome());
    assertEquals("Banco do Brasil", dto.getBanco());
    assertEquals("1234", dto.getAgencia());
    assertEquals("12345-6", dto.getNumeroConta());
    assertEquals(TipoConta.CORRENTE.name(), dto.getTipo());
  }

  @Test
  @DisplayName("Deve construir GridDTO corretamente da entidade")
  void deveConstruirGridDTOCorretamenteDaEntidade() {
    // When
    ContaBancariaGridDTO gridDTO = service.buildGridDTOFromEntity(entidadeValida);

    // Then
    assertNotNull(gridDTO);
    assertEquals("Conta Corrente Principal", gridDTO.getNome());
    assertEquals("Banco do Brasil", gridDTO.getBanco());
    assertEquals(TipoConta.CORRENTE.name(), gridDTO.getTipo());
  }
}
