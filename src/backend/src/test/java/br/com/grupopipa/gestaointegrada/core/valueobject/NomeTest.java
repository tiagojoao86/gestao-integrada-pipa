package br.com.grupopipa.gestaointegrada.core.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("Nome - Value Object")
class NomeTest {

  @Test
  @DisplayName("Deve criar nome válido")
  void deveCriarNomeValido() {
    Nome nome = Nome.of("João Silva");

    assertEquals("João Silva", nome.getValue());
  }

  @Test
  @DisplayName("Deve remover espaços em branco nas extremidades")
  void deveRemoverEspacos() {
    Nome nome = Nome.of("  João Silva  ");

    assertEquals("João Silva", nome.getValue());
  }

  @Test
  @DisplayName("Deve aceitar nome com acentos e caracteres especiais")
  void deveAceitarNomeComAcentos() {
    Nome nome = Nome.of("José da Silva Júnior");

    assertEquals("José da Silva Júnior", nome.getValue());
  }

  @Test
  @DisplayName("Deve rejeitar nome nulo")
  void deveRejeitarNomeNulo() {
    assertThrows(BeanValidationException.class, () -> Nome.of(null));
  }

  @Test
  @DisplayName("Deve rejeitar nome vazio")
  void deveRejeitarNomeVazio() {
    assertThrows(BeanValidationException.class, () -> Nome.of(""));
    assertThrows(BeanValidationException.class, () -> Nome.of("   "));
  }

  @Test
  @DisplayName("Deve rejeitar nome muito longo")
  void deveRejeitarNomeMuitoLongo() {
    String nomeLongo = "a".repeat(256);
    assertThrows(BeanValidationException.class, () -> Nome.of(nomeLongo));
  }

  @Test
  @DisplayName("Deve aceitar nome no limite de 255 caracteres")
  void deveAceitarNomeNoLimite() {
    String nome255 = "a".repeat(255);
    assertDoesNotThrow(() -> Nome.of(nome255));
  }

  @Test
  @DisplayName("Deve comparar nomes iguais")
  void deveCompararNomesIguais() {
    Nome nome1 = Nome.of("João Silva");
    Nome nome2 = Nome.of("  João Silva  ");

    assertEquals(nome1, nome2);
    assertEquals(nome1.hashCode(), nome2.hashCode());
  }

  @Test
  @DisplayName("Deve comparar nomes diferentes")
  void deveCompararNomesDiferentes() {
    Nome nome1 = Nome.of("João Silva");
    Nome nome2 = Nome.of("Maria Silva");

    assertNotEquals(nome1, nome2);
  }
}
