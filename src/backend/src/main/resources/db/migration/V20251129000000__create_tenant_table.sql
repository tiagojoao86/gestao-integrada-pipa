-- Migration para criar schema public com tabela de tenants
-- V1_1__create_tenant_table.sql

CREATE TABLE IF NOT EXISTS public.tenant (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(63) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    numero_documento VARCHAR(30),
    schema_name VARCHAR(63) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP,
    plano VARCHAR(50) NOT NULL DEFAULT 'BASIC',
    max_usuarios INTEGER NOT NULL DEFAULT 5,
    observacoes TEXT,
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE', 'TRIAL', 'CANCELLED')),
    CONSTRAINT chk_plano CHECK (plano IN ('BASIC', 'PROFESSIONAL', 'ENTERPRISE', 'CUSTOM'))
);

-- Índices para melhorar performance
CREATE INDEX idx_tenant_tenant_id ON public.tenant(tenant_id);
CREATE INDEX idx_tenant_schema_name ON public.tenant(schema_name);
CREATE INDEX idx_tenant_status ON public.tenant(status);

-- Comentários nas colunas
COMMENT ON TABLE public.tenant IS 'Tabela de tenants (empresas) do sistema multi-tenant';
COMMENT ON COLUMN public.tenant.tenant_id IS 'Identificador único do tenant usado nas requisições';
COMMENT ON COLUMN public.tenant.schema_name IS 'Nome do schema PostgreSQL onde os dados do tenant são armazenados';
COMMENT ON COLUMN public.tenant.status IS 'Status atual do tenant: ACTIVE, SUSPENDED, INACTIVE, TRIAL, CANCELLED';
COMMENT ON COLUMN public.tenant.plano IS 'Plano contratado: BASIC, PROFESSIONAL, ENTERPRISE, CUSTOM';
COMMENT ON COLUMN public.tenant.max_usuarios IS 'Número máximo de usuários permitidos para este tenant';
