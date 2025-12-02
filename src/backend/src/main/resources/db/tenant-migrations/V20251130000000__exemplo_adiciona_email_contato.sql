-- Migration de exemplo para demonstrar o TenantMigrationRunner
-- Esta migration será aplicada automaticamente em TODOS os tenants
-- na próxima inicialização do sistema

-- Adiciona coluna email_contato na tabela usuario
-- (apenas exemplo, pode ser removida se não for necessária)

ALTER TABLE usuario 
ADD COLUMN IF NOT EXISTS email_contato VARCHAR(200);

COMMENT ON COLUMN usuario.email_contato IS 'Email de contato do usuário para recuperação de senha';

-- Criar índice para busca rápida por email
CREATE INDEX IF NOT EXISTS idx_usuario_email_contato ON usuario(email_contato);
