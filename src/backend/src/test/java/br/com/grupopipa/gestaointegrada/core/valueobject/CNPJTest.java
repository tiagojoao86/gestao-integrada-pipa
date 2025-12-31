package br.com.grupopipa.gestaointegrada.core.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("CNPJ - Value Object")
class CNPJTest {

  @Test
  @DisplayName("Deve criar CNPJ válido")
  void deveCriarCnpjValido() {
    CNPJ cnpj = new CNPJ("11.222.333/0001-81");

    assertEquals("11222333000181", cnpj.getValue());
  }

  @Test
  @DisplayName("Deve criar CNPJ válido apenas com números")
  void deveCriarCnpjApenasNumeros() {
    CNPJ cnpj = new CNPJ("11222333000181");

    assertEquals("11222333000181", cnpj.getValue());
  }

  @Test
  @DisplayName("Deve formatar CNPJ corretamente")
  void deveFormatarCnpjCorretamente() {
    CNPJ cnpj = new CNPJ("11222333000181");

    assertEquals("11.222.333/0001-81", cnpj.getFormatted());
  }

  @Test
  @DisplayName("Deve remover caracteres especiais")
  void deveRemoverCaracteresEspeciais() {
    CNPJ cnpj = new CNPJ("11.222.333/0001-81");

    assertEquals("11222333000181", cnpj.getValue());
  }

  @Test
  @DisplayName("Deve rejeitar CNPJ nulo")
  void deveRejeitarCnpjNulo() {
    assertThrows(BeanValidationException.class, () -> new CNPJ(null));
  }

  @Test
  @DisplayName("Deve rejeitar CNPJ vazio")
  void deveRejeitarCnpjVazio() {
    assertThrows(BeanValidationException.class, () -> new CNPJ(""));
    assertThrows(BeanValidationException.class, () -> new CNPJ("   "));
  }

  @Test
  @DisplayName("Deve rejeitar CNPJ com menos de 14 dígitos")
  void deveRejeitarCnpjComMenosDe14Digitos() {
    assertThrows(BeanValidationException.class, () -> new CNPJ("1122233300018"));
  }

  @Test
  @DisplayName("Deve rejeitar CNPJ com mais de 14 dígitos")
  void deveRejeitarCnpjComMaisDe14Digitos() {
    assertThrows(BeanValidationException.class, () -> new CNPJ("112223330001811"));
  }

  @Test
  @DisplayName("Deve rejeitar CNPJ com dígitos verificadores inválidos")
  void deveRejeitarCnpjComDigitosInvalidos() {
    assertThrows(BeanValidationException.class, () -> new CNPJ("11.222.333/0001-00"));
  }

  @Test
  @DisplayName("Deve rejeitar CNPJ com todos dígitos iguais")
  void deveRejeitarCnpjComDigitosIguais() {
    assertThrows(BeanValidationException.class, () -> new CNPJ("11111111111111"));
    assertThrows(BeanValidationException.class, () -> new CNPJ("00000000000000"));
  }

  @Test
  @DisplayName("Deve aceitar CNPJ real válido")
  void deveAceitarCnpjRealValido() {
    // CNPJ válido do Banco do Brasil
    assertDoesNotThrow(() -> new CNPJ("00.000.000/0001-91"));
  }

  @Test
  @DisplayName("Deve comparar CNPJs iguais")
  void deveCompararCnpjsIguais() {
    CNPJ cnpj1 = new CNPJ("11222333000181");
    CNPJ cnpj2 = new CNPJ("11.222.333/0001-81");

    assertEquals(cnpj1, cnpj2);
    assertEquals(cnpj1.hashCode(), cnpj2.hashCode());
  }

  @Test
  @DisplayName("Deve comparar CNPJs diferentes")
  void deveCompararCnpjsDiferentes() {
    CNPJ cnpj1 = new CNPJ("11222333000181");
    CNPJ cnpj2 = new CNPJ("00.000.000/0001-91");

    assertNotEquals(cnpj1, cnpj2);
  }

  @Test
  @DisplayName("Deve retornar CNPJ formatado no toString")
  void deveRetornarCnpjFormatadoNoToString() {
    CNPJ cnpj = new CNPJ("11222333000181");

    assertEquals("11.222.333/0001-81", cnpj.toString());
  }
}
