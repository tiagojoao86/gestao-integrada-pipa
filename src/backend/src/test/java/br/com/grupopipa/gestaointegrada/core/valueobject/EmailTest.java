package br.com.grupopipa.gestaointegrada.core.valueobject;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Email - Value Object")
class EmailTest {

    @Test
    @DisplayName("Deve criar email válido")
    void deveCriarEmailValido() {
        Email email = new Email("usuario@exemplo.com");

        assertEquals("usuario@exemplo.com", email.getValue());
    }

    @Test
    @DisplayName("Deve converter email para lowercase")
    void deveConverterParaLowercase() {
        Email email = new Email("USUARIO@EXEMPLO.COM");

        assertEquals("usuario@exemplo.com", email.getValue());
    }

    @Test
    @DisplayName("Deve remover espaços em branco")
    void deveRemoverEspacos() {
        Email email = new Email("  usuario@exemplo.com  ");

        assertEquals("usuario@exemplo.com", email.getValue());
    }

    @Test
    @DisplayName("Deve aceitar email com números e caracteres especiais")
    void deveAceitarCaracteresEspeciais() {
        Email email = new Email("usuario+123_test.name@exemplo-test.com.br");

        assertEquals("usuario+123_test.name@exemplo-test.com.br", email.getValue());
    }

    @Test
    @DisplayName("Deve rejeitar email nulo")
    void deveRejeitarEmailNulo() {
        assertThrows(BeanValidationException.class, () -> new Email(null));
    }

    @Test
    @DisplayName("Deve rejeitar email vazio")
    void deveRejeitarEmailVazio() {
        assertThrows(BeanValidationException.class, () -> new Email(""));
        assertThrows(BeanValidationException.class, () -> new Email("   "));
    }

    @Test
    @DisplayName("Deve rejeitar email sem @")
    void deveRejeitarEmailSemArroba() {
        assertThrows(BeanValidationException.class, () -> new Email("usuario.exemplo.com"));
    }

    @Test
    @DisplayName("Deve rejeitar email sem domínio")
    void deveRejeitarEmailSemDominio() {
        assertThrows(BeanValidationException.class, () -> new Email("usuario@"));
    }

    @Test
    @DisplayName("Deve rejeitar email sem extensão")
    void deveRejeitarEmailSemExtensao() {
        assertThrows(BeanValidationException.class, () -> new Email("usuario@exemplo"));
    }

    @Test
    @DisplayName("Deve rejeitar email com formato inválido")
    void deveRejeitarEmailInvalido() {
        assertThrows(BeanValidationException.class, () -> new Email("@exemplo.com"));
        assertThrows(BeanValidationException.class, () -> new Email("usuario@@exemplo.com"));
    }

    @Test
    @DisplayName("Deve comparar emails iguais")
    void deveCompararEmailsIguais() {
        Email email1 = new Email("usuario@exemplo.com");
        Email email2 = new Email("USUARIO@EXEMPLO.COM");

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    @DisplayName("Deve comparar emails diferentes")
    void deveCompararEmailsDiferentes() {
        Email email1 = new Email("usuario1@exemplo.com");
        Email email2 = new Email("usuario2@exemplo.com");

        assertNotEquals(email1, email2);
    }

    @Test
    @DisplayName("Deve retornar email no toString")
    void deveRetornarEmailNoToString() {
        Email email = new Email("usuario@exemplo.com");

        assertEquals("usuario@exemplo.com", email.toString());
    }
}
