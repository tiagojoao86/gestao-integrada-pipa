package br.com.grupopipa.gestaointegrada.core.valueobject;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;

/** Value Object para Telefone brasileiro */
@Embeddable
public class PhoneNumber implements Serializable {

  @Column(name = "telefone", length = 11)
  private String value;

  protected PhoneNumber() {}

  public PhoneNumber(String value) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    if (value == null || value.isBlank()) {
      violations.add(new BeanValidationMessage("telefone", "Telefone não pode ser vazio"));
      throw new BeanValidationException(violations);
    }

    String numeros = value.replaceAll("[^0-9]", "");

    if (numeros.length() < 10 || numeros.length() > 11) {
      violations.add(new BeanValidationMessage("telefone", "Telefone deve ter 10 ou 11 dígitos"));
      throw new BeanValidationException(violations);
    }

    this.value = numeros;
  }

  public String getValue() {
    return value;
  }

  public String getFormatted() {
    if (value.length() == 11) {
      // (XX) 9XXXX-XXXX
      return value.replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
    } else {
      // (XX) XXXX-XXXX
      return value.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PhoneNumber)) return false;
    PhoneNumber that = (PhoneNumber) o;
    return Objects.equals(value, that.value);
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
