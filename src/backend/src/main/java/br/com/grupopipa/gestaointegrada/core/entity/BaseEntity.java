package br.com.grupopipa.gestaointegrada.core.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.f4b6a3.uuid.UuidCreator;

import br.com.grupopipa.gestaointegrada.core.audit.CustomAuditingEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

@MappedSuperclass
@Getter
@EntityListeners(CustomAuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
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

    public BaseEntity() {
    }

    /**
     * Gera um UUID v7 (time-ordered) antes de persistir a entidade.
     * UUID v7 garante ordenação cronológica e melhor performance em índices.
     */
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

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
