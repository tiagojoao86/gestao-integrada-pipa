package br.com.grupopipa.gestaointegrada.core.audit;

import java.time.LocalDateTime;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import br.com.grupopipa.gestaointegrada.core.Session;
import br.com.grupopipa.gestaointegrada.core.entity.BaseEntity;

/**
 * Listener customizado de auditoria. Na criação, preenche todos os campos de auditoria. Na
 * atualização, verifica se é soft delete (deleted=true) e preserva updatedBy/updatedAt nesse caso.
 */
public class CustomAuditingEntityListener {

  @PrePersist
  public void touchForCreate(Object target) {
    if (target instanceof BaseEntity) {
      BaseEntity entity = (BaseEntity) target;
      String currentUser = Session.getUsuarioUsername();
      LocalDateTime now = LocalDateTime.now();

      // Define os campos de criação
      entity.setCreatedBy(currentUser);
      entity.setCreatedAt(now);

      // Define os campos de atualização na criação
      entity.setUpdatedBy(currentUser);
      entity.setUpdatedAt(now);
    }
  }

  @PreUpdate
  public void touchForUpdate(Object target) {
    if (target instanceof BaseEntity) {
      BaseEntity entity = (BaseEntity) target;

      // Se está sendo feito soft delete (deleted=true), não atualiza updatedBy/updatedAt
      // Isso preserva quem fez a última atualização REAL antes da exclusão
      if (Boolean.TRUE.equals(entity.getDeleted())) {
        // Não faz nada - preserva os valores existentes de updatedBy/updatedAt
        return;
      }

      // Atualização normal - atualiza os campos de auditoria
      String currentUser = Session.getUsuarioUsername();
      LocalDateTime now = LocalDateTime.now();

      entity.setUpdatedBy(currentUser);
      entity.setUpdatedAt(now);
    }
  }
}
