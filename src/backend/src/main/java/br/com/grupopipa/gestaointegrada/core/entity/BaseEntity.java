package br.com.grupopipa.gestaointegrada.core.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.UuidGenerator;

import br.com.grupopipa.gestaointegrada.core.audit.CustomAuditingEntityListener;

import lombok.Getter;

@MappedSuperclass
@Getter
@EntityListeners(CustomAuditingEntityListener.class)
public abstract class BaseEntity {

  @Id
  @GeneratedValue
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "created_by", nullable = false, updatable = false)
  private String createdBy;

  @Column(name = "updated_by")
  private String updatedBy;

  @Column(name = "deleted", nullable = false)
  private Boolean deleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  private String deletedBy;

  public BaseEntity() {}

  /**
   * Marca a entidade como excluída (soft delete)
   *
   * @param deletedBy usuário que realizou a exclusão
   */
  public void markAsDeleted(String deletedBy) {
    this.deleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
  }

  /**
   * Verifica se a entidade foi excluída logicamente
   *
   * @return true se foi excluída
   */
  public boolean isDeleted() {
    return Boolean.TRUE.equals(deleted);
  }

  /** Restaura uma entidade excluída logicamente */
  public void restore() {
    this.deleted = false;
    this.deletedAt = null;
    this.deletedBy = null;
  }

  /**
   * Define o campo createdBy. Usado pelo CustomAuditingEntityListener.
   *
   * @param createdBy usuário que criou a entidade
   */
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   * Define o campo createdAt. Usado pelo CustomAuditingEntityListener.
   *
   * @param createdAt data/hora de criação
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Define o campo updatedBy. Usado pelo CustomAuditingEntityListener.
   *
   * @param updatedBy usuário que atualizou a entidade
   */
  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  /**
   * Define o campo updatedAt. Usado pelo CustomAuditingEntityListener.
   *
   * @param updatedAt data/hora de atualização
   */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
