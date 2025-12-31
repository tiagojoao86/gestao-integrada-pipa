package br.com.grupopipa.gestaointegrada.core.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("CPF - Value Object")
class CPFTest {

  @Test
  @DisplayName("Deve criar CPF válido com 11 dígitos numéricos")
  void deveCriarCpfValido() {
    String cpfValido = "12345678909"; // CPF válido com dígitos verificadores corretos

    assertDoesNotThrow(() -> new CPF(cpfValido));
  }

  @Test
  @DisplayName("Deve criar CPF válido removendo formatação")
  void deveCriarCpfValidoComFormatacao() {
    String cpfFormatado = "123.456.789-09";

    CPF cpf = assertDoesNotThrow(() -> new CPF(cpfFormatado));
    assertEquals("12345678909", cpf.getValue());
  }

  @Test
  @DisplayName("Deve formatar CPF corretamente")
  void deveFormatarCpfCorretamente() {
    CPF cpf = new CPF("12345678909");

    assertEquals("123.456.789-09", cpf.getFormatted());
  }

  @Test
  @DisplayName("Deve rejeitar CPF nulo")
  void deveRejeitarCpfNulo() {
    assertThrows(BeanValidationException.class, () -> new CPF(null));
  }

  @Test
  @DisplayName("Deve rejeitar CPF vazio")
  void deveRejeitarCpfVazio() {
    assertThrows(BeanValidationException.class, () -> new CPF(""));
  }

  @Test
  @DisplayName("Deve rejeitar CPF com menos de 11 dígitos")
  void deveRejeitarCpfComMenosDe11Digitos() {
    assertThrows(BeanValidationException.class, () -> new CPF("123456789"));
  }

  @Test
  @DisplayName("Deve rejeitar CPF com mais de 11 dígitos")
  void deveRejeitarCpfComMaisDe11Digitos() {
    assertThrows(BeanValidationException.class, () -> new CPF("123456789012"));
  }

  @Test
  @DisplayName("Deve rejeitar CPF com dígitos verificadores inválidos")
  void deveRejeitarCpfComDigitosInvalidos() {
    assertThrows(BeanValidationException.class, () -> new CPF("12345678900")); // Dígitos incorretos
  }

  @Test
  @DisplayName("Deve rejeitar CPF com todos os dígitos iguais")
  void deveRejeitarCpfComDigitosIguais() {
    assertThrows(BeanValidationException.class, () -> new CPF("11111111111"));
    assertThrows(BeanValidationException.class, () -> new CPF("00000000000"));
  }

  @Test
  @DisplayName("Deve comparar CPFs iguais corretamente")
  void deveCompararCpfsIguais() {
    CPF cpf1 = new CPF("12345678909");
    CPF cpf2 = new CPF("123.456.789-09");

    assertEquals(cpf1, cpf2);
    assertEquals(cpf1.hashCode(), cpf2.hashCode());
  }

  @Test
  @DisplayName("Deve comparar CPFs diferentes corretamente")
  void deveCompararCpfsDiferentes() {
    CPF cpf1 = new CPF("12345678909");
    CPF cpf2 = new CPF("98765432100");

    assertNotEquals(cpf1, cpf2);
  }
}
