package br.com.grupopipa.gestaointegrada.core.valueobject;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Value Object para CPF com validação de dígitos verificadores */
@Embeddable
public class CPF implements Serializable {

    @Column(name = "cpf", length = 11)
    private String value;

    protected CPF() {
    }

    public CPF(String value) {
        Set<BeanValidationMessage> violations = new HashSet<>();

        if (value == null || value.isBlank()) {
            violations.add(new BeanValidationMessage("validation.cpf.required", "O campo 'CPF' é obrigatório."));
            throw new BeanValidationException(violations);
        }

        String cpfNumeros = value.replaceAll("[^0-9]", "");

        if (cpfNumeros.length() != 11) {
            violations.add(new BeanValidationMessage("validation.cpf.length", "CPF deve ter 11 dígitos."));
            throw new BeanValidationException(violations);
        }

        if (!validarDigitos(cpfNumeros)) {
            violations.add(new BeanValidationMessage("validation.cpf.invalid", "CPF inválido."));
            throw new BeanValidationException(violations);
        }

        this.value = cpfNumeros;
    }

    private boolean validarDigitos(String cpf) {
        // CPFs conhecidos inválidos (todos dígitos iguais)
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int[] numeros = cpf.chars().map(c -> c - '0').toArray();

        // Validar primeiro dígito
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += numeros[i] * (10 - i);
        }
        int dig1 = 11 - (soma % 11);
        if (dig1 >= 10) {
            dig1 = 0;
        }
        if (numeros[9] != dig1) {
            return false;
        }

        // Validar segundo dígito
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += numeros[i] * (11 - i);
        }
        int dig2 = 11 - (soma % 11);
        if (dig2 >= 10) {
            dig2 = 0;
        }
        return numeros[10] == dig2;
    }

    public String getValue() {
        return value;
    }

    public String getFormatted() {
        return value.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CPF)) {
            return false;
        }
        CPF cpf = (CPF) o;
        return Objects.equals(value, cpf.value);
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
