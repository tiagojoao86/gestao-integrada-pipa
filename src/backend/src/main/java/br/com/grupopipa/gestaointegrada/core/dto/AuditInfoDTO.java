package br.com.grupopipa.gestaointegrada.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para informações de auditoria de uma entidade.
 * Contém dados de criação, atualização e exclusão (soft delete).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditInfoDTO {
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String deletedBy;
    private LocalDateTime deletedAt;
}
