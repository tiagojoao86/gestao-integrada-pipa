package br.com.grupopipa.gestaointegrada.tenant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import br.com.grupopipa.gestaointegrada.tenant.enums.TenantPlano;
import br.com.grupopipa.gestaointegrada.tenant.enums.TenantStatus;

/**
 * Entidade Tenant armazenada no schema public
 * Representa uma empresa/organização que usa o sistema
 */
@Entity
@Table(name = "tenant", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador único do tenant (usado no schema name)
     * Exemplo: "empresa_abc", "solar_xyz"
     */
    @Column(name = "tenant_id", nullable = false, unique = true, length = 63)
    private String tenantId;

    /**
     * Nome da empresa/organização
     */
    @Column(nullable = false)
    private String nome;

    /**
     * CNPJ da empresa
     */
    @Column(name = "numero_documento", length = 30)
    private String numeroDocumento;

    /**
     * Nome do schema no PostgreSQL
     * Gerado automaticamente: "tenant_" + tenantId
     */
    @Column(name = "schema_name", nullable = false, unique = true, length = 63)
    private String schemaName;

    /**
     * Status do tenant
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.ACTIVE;

    /**
     * Data de criação
     */
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    /**
     * Data de expiração da assinatura
     */
    @Column(name = "data_expiracao")
    private LocalDateTime dataExpiracao;

    /**
     * Plano contratado
     */
    @Enumerated(EnumType.STRING)
    private TenantPlano plano = TenantPlano.BASIC;

    /**
     * Número máximo de usuários permitidos
     */
    @Column(name = "max_usuarios", nullable = false)
    private Integer maxUsuarios = 5;

    /**
     * Observações
     */
    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @PrePersist
    public void prePersist() {
        if (schemaName == null) {
            schemaName = "tenant_" + tenantId.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        }
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }
}
