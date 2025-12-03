package br.com.grupopipa.gestaointegrada.core.valueobject;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Login - Value Object")
class LoginTest {

    @Test
    @DisplayName("Deve criar login válido")
    void deveCriarLoginValido() {
        Login login = Login.of("usuario123");
        
        assertEquals("usuario123", login.getValue());
    }

    @Test
    @DisplayName("Deve remover espaços em branco nas extremidades")
    void deveRemoverEspacos() {
        Login login = Login.of("  usuario123  ");
        
        assertEquals("usuario123", login.getValue());
    }

    @Test
    @DisplayName("Deve aceitar login com números e caracteres especiais")
    void deveAceitarCaracteresEspeciais() {
        Login login = Login.of("usuario_123.teste");
        
        assertEquals("usuario_123.teste", login.getValue());
    }

    @Test
    @DisplayName("Deve rejeitar login nulo")
    void deveRejeitarLoginNulo() {
        assertThrows(BeanValidationException.class, () -> Login.of(null));
    }

    @Test
    @DisplayName("Deve rejeitar login vazio")
    void deveRejeitarLoginVazio() {
        assertThrows(BeanValidationException.class, () -> Login.of(""));
        assertThrows(BeanValidationException.class, () -> Login.of("   "));
    }

    @Test
    @DisplayName("Deve rejeitar login muito longo")
    void deveRejeitarLoginMuitoLongo() {
        String loginLongo = "a".repeat(101);
        assertThrows(BeanValidationException.class, () -> Login.of(loginLongo));
    }

    @Test
    @DisplayName("Deve aceitar login no limite de 100 caracteres")
    void deveAceitarLoginNoLimite() {
        String login100 = "a".repeat(100);
        assertDoesNotThrow(() -> Login.of(login100));
    }

    @Test
    @DisplayName("Deve comparar logins iguais")
    void deveCompararLoginsIguais() {
        Login login1 = Login.of("usuario123");
        Login login2 = Login.of("  usuario123  ");
        
        assertEquals(login1, login2);
        assertEquals(login1.hashCode(), login2.hashCode());
    }

    @Test
    @DisplayName("Deve comparar logins diferentes")
    void deveCompararLoginsDiferentes() {
        Login login1 = Login.of("usuario123");
        Login login2 = Login.of("usuario456");
        
        assertNotEquals(login1, login2);
    }
}
