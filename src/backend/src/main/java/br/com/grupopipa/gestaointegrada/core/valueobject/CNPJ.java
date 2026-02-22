package br.com.grupopipa.gestaointegrada.core.valueobject;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Value Object para CNPJ com validação de dígitos verificadores */
@Embeddable
public class CNPJ implements Serializable {

    @Column(name = "cnpj", length = 14)
    private String value;

    protected CNPJ() {
    }

    public CNPJ(String value) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (value == null || value.isBlank()) {
            violations.add(new BeanValidationMessage("cnpj", "O campo 'cnpj' é obrigatório."));
            throw new BeanValidationException(violations);
        }

        String cnpjNumeros = value.replaceAll("[^0-9]", "");

        if (cnpjNumeros.length() != 14) {
            violations.add(new BeanValidationMessage("cnpj", "CNPJ deve ter 14 dígitos."));
            throw new BeanValidationException(violations);
        }

        if (!validarDigitos(cnpjNumeros)) {
            violations.add(new BeanValidationMessage("cnpj", "CNPJ inválido."));
            throw new BeanValidationException(violations);
        }

        this.value = cnpjNumeros;
    }

    private boolean validarDigitos(String cnpj) {
        // CNPJs conhecidos inválidos (todos dígitos iguais)
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        int[] numeros = cnpj.chars().map(c -> c - '0').toArray();
        int[] pesos1 = { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
        int[] pesos2 = { 6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };

        // Primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += numeros[i] * pesos1[i];
        }
        int dig1 = 11 - (soma % 11);
        if (dig1 >= 10) {
            dig1 = 0;
        }
        if (numeros[12] != dig1) {
            return false;
        }

        // Segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += numeros[i] * pesos2[i];
        }
        int dig2 = 11 - (soma % 11);
        if (dig2 >= 10) {
            dig2 = 0;
        }
        return numeros[13] == dig2;
    }

    public String getValue() {
        return value;
    }

    public String getFormatted() {
        return value.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CNPJ)) {
            return false;
        }
        CNPJ cnpj = (CNPJ) o;
        return Objects.equals(value, cnpj.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return getFormatted();
    }
}
