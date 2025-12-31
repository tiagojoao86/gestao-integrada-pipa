package br.com.grupopipa.gestaointegrada.core.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("Chave - Value Object")
class ChaveTest {

  @Test
  @DisplayName("Deve criar chave válida")
  void deveCriarChaveValida() {
    Chave chave = Chave.of("CHAVE_CONFIG_001");

    assertEquals("CHAVE_CONFIG_001", chave.getValue());
  }

  @Test
  @DisplayName("Deve remover espaços em branco nas extremidades")
  void deveRemoverEspacos() {
    Chave chave = Chave.of("  CHAVE_CONFIG_001  ");

    assertEquals("CHAVE_CONFIG_001", chave.getValue());
  }

  @Test
  @DisplayName("Deve aceitar chave com caracteres especiais")
  void deveAceitarCaracteresEspeciais() {
    Chave chave = Chave.of("config.sistema.valor-01");

    assertEquals("config.sistema.valor-01", chave.getValue());
  }

  @Test
  @DisplayName("Deve rejeitar chave nula")
  void deveRejeitarChaveNula() {
    assertThrows(BeanValidationException.class, () -> Chave.of(null));
  }

  @Test
  @DisplayName("Deve rejeitar chave vazia")
  void deveRejeitarChaveVazia() {
    assertThrows(BeanValidationException.class, () -> Chave.of(""));
    assertThrows(BeanValidationException.class, () -> Chave.of("   "));
  }

  @Test
  @DisplayName("Deve rejeitar chave muito longa")
  void deveRejeitarChaveMuitoLonga() {
    String chaveLonga = "a".repeat(256);
    assertThrows(BeanValidationException.class, () -> Chave.of(chaveLonga));
  }

  @Test
  @DisplayName("Deve aceitar chave no limite de 255 caracteres")
  void deveAceitarChaveNoLimite() {
    String chave255 = "a".repeat(255);
    assertDoesNotThrow(() -> Chave.of(chave255));
  }

  @Test
  @DisplayName("Deve comparar chaves iguais")
  void deveCompararChavesIguais() {
    Chave chave1 = Chave.of("CHAVE_CONFIG_001");
    Chave chave2 = Chave.of("  CHAVE_CONFIG_001  ");

    assertEquals(chave1, chave2);
    assertEquals(chave1.hashCode(), chave2.hashCode());
  }

  @Test
  @DisplayName("Deve comparar chaves diferentes")
  void deveCompararChavesDiferentes() {
    Chave chave1 = Chave.of("CHAVE_CONFIG_001");
    Chave chave2 = Chave.of("CHAVE_CONFIG_002");

    assertNotEquals(chave1, chave2);
  }
}
