package br.com.grupopipa.gestaointegrada.core.valueobject;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money - Value Object")
class MoneyTest {

    @Test
    @DisplayName("Deve criar Money com valor válido")
    void deveCriarMoneyComValorValido() {
        Money money = Money.of(new BigDecimal("100.50"));

        assertEquals(new BigDecimal("100.50"), money.getValue());
    }

    @Test
    @DisplayName("Deve criar Money com zero")
    void deveCriarMoneyComZero() {
        Money money = Money.zero();

        assertEquals(new BigDecimal("0.00"), money.getValue());
    }

    @Test
    @DisplayName("Deve rejeitar Money nulo")
    void deveRejeitarMoneyNulo() {
        assertThrows(BeanValidationException.class, () -> Money.of((BigDecimal) null));
    }

    @Test
    @DisplayName("Deve adicionar valores corretamente")
    void deveAdicionarValoresCorretamente() {
        Money money1 = Money.of(new BigDecimal("100.00"));
        Money money2 = Money.of(new BigDecimal("50.50"));

        Money resultado = money1.add(money2);

        assertEquals(new BigDecimal("150.50"), resultado.getValue());
    }

    @Test
    @DisplayName("Deve subtrair valores corretamente")
    void deveSubtrairValoresCorretamente() {
        Money money1 = Money.of(new BigDecimal("100.00"));
        Money money2 = Money.of(new BigDecimal("30.50"));

        Money resultado = money1.subtract(money2);

        assertEquals(new BigDecimal("69.50"), resultado.getValue());
    }

    @Test
    @DisplayName("Deve multiplicar por fator corretamente")
    void deveMultiplicarPorFatorCorretamente() {
        Money money = Money.of(new BigDecimal("100.00"));

        Money resultado = money.multiply(new BigDecimal("1.5"));

        assertEquals(new BigDecimal("150.00"), resultado.getValue());
    }

    @Test
    @DisplayName("Deve verificar se é positivo")
    void deveVerificarSeEhPositivo() {
        Money positivo = Money.of(new BigDecimal("100.00"));
        Money negativo = Money.of(new BigDecimal("-50.00"));
        Money zero = Money.zero();

        assertTrue(positivo.isPositive());
        assertFalse(negativo.isPositive());
        assertFalse(zero.isPositive());
    }

    @Test
    @DisplayName("Deve verificar se é negativo")
    void deveVerificarSeEhNegativo() {
        Money positivo = Money.of(new BigDecimal("100.00"));
        Money negativo = Money.of(new BigDecimal("-50.00"));
        Money zero = Money.zero();

        assertFalse(positivo.isNegative());
        assertTrue(negativo.isNegative());
        assertFalse(zero.isNegative());
    }

    @Test
    @DisplayName("Deve verificar se é zero")
    void deveVerificarSeEhZero() {
        Money positivo = Money.of(new BigDecimal("100.00"));
        Money zero = Money.zero();

        assertFalse(positivo.isZero());
        assertTrue(zero.isZero());
    }

    @Test
    @DisplayName("Deve comparar valores corretamente")
    void deveCompararValoresCorretamente() {
        Money money1 = Money.of(new BigDecimal("100.00"));
        Money money2 = Money.of(new BigDecimal("50.00"));
        Money money3 = Money.of(new BigDecimal("100.00"));

        assertTrue(money1.isGreaterThan(money2));
        assertFalse(money1.isGreaterThan(money3));
        assertTrue(money2.isLessThan(money1));
        assertFalse(money1.isLessThan(money3));
    }

    @Test
    @DisplayName("Deve comparar Money iguais corretamente")
    void deveCompararMoneyIguais() {
        Money money1 = Money.of(new BigDecimal("100.00"));
        Money money2 = Money.of(new BigDecimal("100.00"));

        assertEquals(money1, money2);
        assertEquals(money1.hashCode(), money2.hashCode());
    }

    @Test
    @DisplayName("Deve manter imutabilidade em operações")
    void deveManterImutabilidadeEmOperacoes() {
        Money original = Money.of(new BigDecimal("100.00"));
        Money outro = Money.of(new BigDecimal("50.00"));

        original.add(outro);

        // Original não deve ser modificado
        assertEquals(new BigDecimal("100.00"), original.getValue());
    }

    @Test
    @DisplayName("Deve arredondar para 2 casas decimais")
    void deveArredondarParaDuasCasasDecimais() {
        Money money = Money.of(new BigDecimal("100.126"));

        assertEquals(new BigDecimal("100.13"), money.getValue());
    }
}
