package br.com.grupopipa.gestaointegrada.core.valueobject;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;

@DisplayName("PhoneNumber - Value Object")
class PhoneNumberTest {

  @Test
  @DisplayName("Deve criar telefone celular válido (11 dígitos)")
  void deveCriarTelefoneCelularValido() {
    PhoneNumber phone = new PhoneNumber("11987654321");

    assertEquals("11987654321", phone.getValue());
  }

  @Test
  @DisplayName("Deve criar telefone fixo válido (10 dígitos)")
  void deveCriarTelefoneFixoValido() {
    PhoneNumber phone = new PhoneNumber("1133334444");

    assertEquals("1133334444", phone.getValue());
  }

  @Test
  @DisplayName("Deve criar telefone com formatação")
  void deveCriarTelefoneComFormatacao() {
    PhoneNumber phone = new PhoneNumber("(11) 98765-4321");

    assertEquals("11987654321", phone.getValue());
  }

  @Test
  @DisplayName("Deve formatar telefone celular corretamente")
  void deveFormatarTelefoneCelular() {
    PhoneNumber phone = new PhoneNumber("11987654321");

    assertEquals("(11) 98765-4321", phone.getFormatted());
  }

  @Test
  @DisplayName("Deve formatar telefone fixo corretamente")
  void deveFormatarTelefoneFixo() {
    PhoneNumber phone = new PhoneNumber("1133334444");

    assertEquals("(11) 3333-4444", phone.getFormatted());
  }

  @Test
  @DisplayName("Deve remover caracteres especiais")
  void deveRemoverCaracteresEspeciais() {
    PhoneNumber phone = new PhoneNumber("(11) 98765-4321");

    assertEquals("11987654321", phone.getValue());
  }

  @Test
  @DisplayName("Deve rejeitar telefone nulo")
  void deveRejeitarTelefoneNulo() {
    assertThrows(BeanValidationException.class, () -> new PhoneNumber(null));
  }

  @Test
  @DisplayName("Deve rejeitar telefone vazio")
  void deveRejeitarTelefoneVazio() {
    assertThrows(BeanValidationException.class, () -> new PhoneNumber(""));
    assertThrows(BeanValidationException.class, () -> new PhoneNumber("   "));
  }

  @Test
  @DisplayName("Deve rejeitar telefone com menos de 10 dígitos")
  void deveRejeitarTelefoneComMenosDe10Digitos() {
    assertThrows(BeanValidationException.class, () -> new PhoneNumber("119876543"));
  }

  @Test
  @DisplayName("Deve rejeitar telefone com mais de 11 dígitos")
  void deveRejeitarTelefoneComMaisDe11Digitos() {
    assertThrows(BeanValidationException.class, () -> new PhoneNumber("119876543210"));
  }

  @Test
  @DisplayName("Deve comparar telefones iguais")
  void deveCompararTelefonesIguais() {
    PhoneNumber phone1 = new PhoneNumber("11987654321");
    PhoneNumber phone2 = new PhoneNumber("(11) 98765-4321");

    assertEquals(phone1, phone2);
    assertEquals(phone1.hashCode(), phone2.hashCode());
  }

  @Test
  @DisplayName("Deve comparar telefones diferentes")
  void deveCompararTelefonesDiferentes() {
    PhoneNumber phone1 = new PhoneNumber("11987654321");
    PhoneNumber phone2 = new PhoneNumber("11987654322");

    assertNotEquals(phone1, phone2);
  }

  @Test
  @DisplayName("Deve retornar telefone formatado no toString")
  void deveRetornarTelefoneFormatadoNoToString() {
    PhoneNumber phone = new PhoneNumber("11987654321");

    assertEquals("(11) 98765-4321", phone.toString());
  }
}
