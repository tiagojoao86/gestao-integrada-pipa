package br.com.grupopipa.gestaointegrada.cadastro.unidadenegocio.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationException;
import br.com.grupopipa.gestaointegrada.core.exception.beanvalidation.BeanValidationMessage;
import br.com.grupopipa.gestaointegrada.core.validation.ValidationUtils;
import br.com.grupopipa.gestaointegrada.core.valueobject.CNPJ;
import br.com.grupopipa.gestaointegrada.core.valueobject.Nome;

/** Entidade para Unidade de Negócio (centro de custo/receita) */
@Entity
@Table(
    name = "unidade_negocio",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_unidade_negocio_codigo", columnNames = "codigo")
    })
public class UnidadeNegocio extends BaseEntity {

  @Column(name = "codigo", nullable = false, length = 20)
  private String codigo;

  @Embedded private Nome nome;

  @Column(name = "descricao", columnDefinition = "TEXT")
  private String descricao;

  @Embedded private CNPJ cnpj;

  @Column(name = "ativa", nullable = false)
  private Boolean ativa = true;

  private UnidadeNegocio(String codigo, Nome nome, String descricao, CNPJ cnpj) {
    this.codigo = codigo;
    this.nome = nome;
    this.descricao = descricao;
    this.cnpj = cnpj;
  }

  protected UnidadeNegocio() {}

  private static class ValidatedData {
    final String codigo;
    final Nome nome;
    final String descricao;
    final CNPJ cnpj;

    ValidatedData(String codigo, Nome nome, String descricao, CNPJ cnpj) {
      this.codigo = codigo;
      this.nome = nome;
      this.descricao = descricao;
      this.cnpj = cnpj;
    }
  }

  private static ValidatedData validate(
      String codigo, String nomeStr, String descricao, String cnpjStr) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    if (codigo == null || codigo.isBlank()) {
      violations.add(new BeanValidationMessage("codigo", "Código é obrigatório"));
    } else if (codigo.length() > 20) {
      violations.add(
          new BeanValidationMessage("codigo", "Código deve ter no máximo 20 caracteres"));
    }

    Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
    CNPJ cnpj = null;
    if (cnpjStr != null && !cnpjStr.isBlank()) {
      cnpj = ValidationUtils.validateAndGet(() -> new CNPJ(cnpjStr), violations);
    }

    if (!violations.isEmpty()) {
      throw new BeanValidationException("unidadeNegocio", violations);
    }

    return new ValidatedData(codigo, nome, descricao, cnpj);
  }

  public void atualizar(String nomeStr, String descricao, String cnpjStr) {
    Set<BeanValidationMessage> violations = new HashSet<>();

    Nome nome = ValidationUtils.validateAndGet(() -> Nome.of(nomeStr), violations);
    CNPJ cnpj = null;
    if (cnpjStr != null && !cnpjStr.isBlank()) {
      cnpj = ValidationUtils.validateAndGet(() -> new CNPJ(cnpjStr), violations);
    }

    if (!violations.isEmpty()) {
      throw new BeanValidationException("unidadeNegocio", violations);
    }

    this.nome = nome;
    this.descricao = descricao;
    this.cnpj = cnpj;
  }

  public void ativar() {
    this.ativa = true;
  }

  public void inativar() {
    this.ativa = false;
  }

  // Getters
  public String getCodigo() {
    return codigo;
  }

  public String getNome() {
    return nome != null ? nome.getValue() : null;
  }

  public String getDescricao() {
    return descricao;
  }

  public String getCnpj() {
    return cnpj != null ? cnpj.getValue() : null;
  }

  public Boolean getAtiva() {
    return ativa;
  }

  public boolean isAtiva() {
    return ativa != null && ativa;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UnidadeNegocio)) return false;
    if (!super.equals(o)) return false;
    UnidadeNegocio that = (UnidadeNegocio) o;
    return Objects.equals(codigo, that.codigo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), codigo);
  }

  @Override
  public String toString() {
    return codigo + " - " + getNome();
  }

  public static class Builder {
    private String codigo;
    private String nome;
    private String descricao;
    private String cnpj;

    public Builder codigo(String codigo) {
      this.codigo = codigo;
      return this;
    }

    public Builder nome(String nome) {
      this.nome = nome;
      return this;
    }

    public Builder descricao(String descricao) {
      this.descricao = descricao;
      return this;
    }

    public Builder cnpj(String cnpj) {
      this.cnpj = cnpj;
      return this;
    }

    public UnidadeNegocio build() {
      ValidatedData data = validate(this.codigo, this.nome, this.descricao, this.cnpj);
      return new UnidadeNegocio(data.codigo, data.nome, data.descricao, data.cnpj);
    }
  }
}
